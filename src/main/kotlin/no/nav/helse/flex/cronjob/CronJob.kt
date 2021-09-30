package no.nav.helse.flex.cronjob

import no.nav.helse.flex.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CronJob(
    val leaderElection: LeaderElection,
) {
    val log = logger()

    @Scheduled(cron = "0 0/2 * * * ?")
    fun run() {
        if (leaderElection.isLeader()) {
            log.info("Kjører varsel utsendelse job")
            val antall = 0
            log.info("Ferdig med varsel utsendelse job. $antall varsler sendt")
        } else {
            log.info("Kjører ikke varsel utsendelse job siden denne podden ikke er leader")
        }
    }
}
