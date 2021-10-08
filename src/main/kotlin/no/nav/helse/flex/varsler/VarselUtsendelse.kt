package no.nav.helse.flex.varsler

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.helse.flex.client.altinn.AltinnVarsel
import no.nav.helse.flex.client.altinn.AltinnVarselClient
import no.nav.helse.flex.client.pdl.PdlClient
import no.nav.helse.flex.logger
import no.nav.helse.flex.narmesteleder.NarmesteLederRepository
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus.*
import no.nav.helse.flex.varsler.domain.PlanlagtVarselType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime

@Component
class VarselUtsendelse(
    private val planlagtVarselRepository: PlanlagtVarselRepository,
    private val narmesteLederRepository: NarmesteLederRepository,
    private val pdlClient: PdlClient,
    private val altinnClient: AltinnVarselClient,
    private val registry: MeterRegistry
) {

    val log = logger()

    fun sendVarsler(now: OffsetDateTime = OffsetDateTime.now()): Int {
        val planlagteVarsler =
            planlagtVarselRepository.findFirst300ByStatusAndSendesIsBefore(PLANLAGT, now)
        var varslerSendt = 0

        log.info("Fant ${planlagteVarsler.size} planlagte varsler som skal sendes før $now")

        planlagteVarsler.forEach { pv ->
            val planlagtVarsel = planlagtVarselRepository.findByIdOrNull(pv.id!!)!!
            if (planlagtVarsel.status != PLANLAGT) {
                log.warn("Planlagt varsel ${planlagtVarsel.id} er ikke lengre planlagt")
                return@forEach
            }

            val forskuttering = narmesteLederRepository.finnForskuttering(orgnummer = planlagtVarsel.orgnummer, brukerFnr = planlagtVarsel.brukerFnr)?.arbeidsgiverForskutterer
            if (forskuttering != true) {
                log.info("Sender ikke planlagt varsel ${planlagtVarsel.id} om manglende innsending av sykepengesøknad ${planlagtVarsel.sykepengesoknadId} da arbeidsgiver ikke forskutterer lønn")
                planlagtVarselRepository.save(planlagtVarsel.copy(oppdatert = Instant.now(), status = INGEN_FORSKUTTERING))
                lagreMetrikk(INGEN_FORSKUTTERING, planlagtVarsel.varselType)
                return@forEach
            }
            val navn = pdlClient.hentFormattertNavn(planlagtVarsel.brukerFnr)
            if (navn.isEmpty() || navn.isBlank()) {
                throw RuntimeException("Mangler navn for planlagt varsel ${planlagtVarsel.id}")
            }

            altinnClient.sendManglendeInnsendingAvSoknadMeldingTilArbeidsgiver(
                AltinnVarsel(
                    planlagtVarsel = planlagtVarsel,
                    navnSykmeldt = navn
                )
            )
            log.info("Sendt planlagt varsel ${planlagtVarsel.id} for søknad ${planlagtVarsel.sykepengesoknadId} med type ${planlagtVarsel.varselType} til ${planlagtVarsel.orgnummer}")
            planlagtVarselRepository.save(
                planlagtVarsel.copy(
                    oppdatert = Instant.now(),
                    status = SENDT,
                )
            )
            lagreMetrikk(SENDT, planlagtVarsel.varselType)
            varslerSendt++
        }
        return varslerSendt
    }

    private fun lagreMetrikk(status: PlanlagtVarselStatus, type: PlanlagtVarselType) {
        registry.counter(
            "planlagt_varsel_behandlet",
            Tags.of(
                "status", status.name,
                "type", type.name,
            )
        ).increment()
    }
}
