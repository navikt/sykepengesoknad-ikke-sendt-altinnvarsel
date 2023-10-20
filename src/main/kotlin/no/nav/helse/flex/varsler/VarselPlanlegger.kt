package no.nav.helse.flex.varsler

import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.ArbeidssituasjonDTO.ARBEIDSTAKER
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO.*
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO.REISETILSKUDD
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.flex.varsler.domain.PlanlagtVarsel
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus.PLANLAGT
import no.nav.helse.flex.varsler.domain.PlanlagtVarselType
import org.springframework.stereotype.Component
import java.time.*

@Component
class VarselPlanlegger(
    private val planlagtVarselRepository: PlanlagtVarselRepository
) {

    val log = logger()

    fun planleggVarsler(soknad: SykepengesoknadDTO) {
        if (!soknad.skalSendeVarselTilArbeidsgiver()) {
            return
        }

        if (soknad.status == NY) {
            soknad.planleggVarselForStatusNy()
        }
        if (listOf(SENDT, AVBRUTT, SLETTET, KORRIGERT, UTGAATT).contains(soknad.status)) {
            soknad.avbrytManglendeSoknadVarsler()
        }
    }

    private fun SykepengesoknadDTO.avbrytManglendeSoknadVarsler() {
        planlagtVarselRepository.findBySykepengesoknadId(id)
            .forEach {
                if (it.status == PLANLAGT) {
                    log.info("Avbryter planlagt varsler med type $type for id $id")
                    planlagtVarselRepository.save(
                        it.copy(
                            status = PlanlagtVarselStatus.AVBRUTT,
                            oppdatert = Instant.now()
                        )
                    )
                }
            }
    }

    private fun SykepengesoknadDTO.planleggVarselForStatusNy() {
        val harAlleredePlanlagt = planlagtVarselRepository.findBySykepengesoknadId(id).isNotEmpty()

        if (harAlleredePlanlagt) {
            // Dette skjer ved gjenåpning av avbrutt søknad
            log.info("Har allerede planlagt varsel for status NY for soknad $id")
            return
        }
        val nå = ZonedDateTime.now(osloZone)
        val sendes = omTreUkerFornuftigDagtid(maxOf(nå, this.tom?.plusDays(1)?.atStartOfDay(osloZone) ?: nå))

        val planlagtVarsel = PlanlagtVarsel(
            id = null,
            sykepengesoknadId = id,
            brukerFnr = fnr,
            oppdatert = Instant.now(),
            orgnummer = arbeidsgiver!!.orgnummer!!,
            sendes = sendes.toInstant(),
            status = PLANLAGT,
            varselType = when (this.type) {
                SoknadstypeDTO.ARBEIDSTAKERE,
                SoknadstypeDTO.BEHANDLINGSDAGER -> PlanlagtVarselType.IKKE_SENDT_SYKEPENGESOKNAD
                SoknadstypeDTO.GRADERT_REISETILSKUDD -> PlanlagtVarselType.IKKE_SENDT_SYKEPENGESOKNAD_MED_REISETILSKUDD
                else -> throw RuntimeException("Har ikke satt opp altinnvarsel for søknadtype ${this.type}")
            },
            soknadTom = tom!!,
            soknadFom = fom!!
        )
        planlagtVarselRepository.save(planlagtVarsel)
        log.info("Planlegger varsel ${planlagtVarsel.varselType} for soknad $id som sendes ${planlagtVarsel.sendes}")
    }
}

fun nærmesteFornuftigDagtid(now: ZonedDateTime = ZonedDateTime.now(osloZone)): ZonedDateTime {
    val dagtid = if (now.hour < 15) {
        now.withHour(now.hour.coerceAtLeast(9))
    } else {
        now.plusDays(1).withHour(9)
    }
    if (dagtid.dayOfWeek == DayOfWeek.SATURDAY) {
        return dagtid.withHour(9).plusDays(2)
    }
    if (dagtid.dayOfWeek == DayOfWeek.SUNDAY) {
        return dagtid.withHour(9).plusDays(1)
    }
    return dagtid
}

fun omTreUkerFornuftigDagtid(now: ZonedDateTime = ZonedDateTime.now(osloZone)): ZonedDateTime {
    return nærmesteFornuftigDagtid(now.plusWeeks(3))
}

fun SykepengesoknadDTO.skalSendeVarselTilArbeidsgiver() =
    ARBEIDSTAKER == arbeidssituasjon && type != REISETILSKUDD

val osloZone = ZoneId.of("Europe/Oslo")
