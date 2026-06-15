package no.nav.helse.flex.cronjob

import no.nav.helse.flex.logger
import no.nav.helse.flex.tilOsloLocalDateTime
import no.nav.helse.flex.varsler.VarselUtsendelse
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime

@Component
class CronJob(
    val leaderElection: LeaderElection,
    val varselUtsendelse: VarselUtsendelse,
) {
    val log = logger()

    @Scheduled(cron = "0 0/2 * * * ?")
    fun run() {
        if (leaderElection.isLeader()) {
            val norskTidspunkt = Instant.now().tilOsloLocalDateTime()
            if (norskTidspunkt.isAfter(LocalDateTime.of(2026, 6, 15, 12, 0))) {
                log.info("Kjører ikke varsel-cronjobb siiden klokken er: $norskTidspunkt")
                return
            }

            log.info("Kjører varsel utsendelse job")
            val antall = varselUtsendelse.sendVarsler()
            log.info("Ferdig med varsel utsendelse job. $antall varsler sendt")
        } else {
            log.info("Kjører ikke varsel utsendelse job siden denne podden ikke er leader")
        }
    }
}
