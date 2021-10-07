package no.nav.helse.flex.varsler

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.helse.flex.logger
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus.*
import no.nav.helse.flex.varsler.domain.PlanlagtVarselType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class VarselUtsendelse(
    private val planlagtVarselRepository: PlanlagtVarselRepository,
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
