package no.nav.helse.flex.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.flex.tilOsloLocalDateTime
import no.nav.helse.flex.varsler.VarselPlanlegger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime

@Component
class SykepengesoknadListener(
    val varselPlanlegger: VarselPlanlegger,
) {
    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory",
    )
    fun listen(
        cr: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment,
    ) {
        val sykepengesoknadDTO = cr.value().tilSykepengesoknadDTO()

        val norskTidspunkt = Instant.now().tilOsloLocalDateTime()
        if (norskTidspunkt.isAfter(LocalDateTime.of(2026, 6, 15, 12, 0))) {
            log.info("Konsumerer ikke søknad: ${sykepengesoknadDTO.id} til Altinn siden klokken er: $norskTidspunkt")
            acknowledgment.acknowledge()
            return
        }

        varselPlanlegger.planleggVarsler(sykepengesoknadDTO)
        acknowledgment.acknowledge()
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}

const val FLEX_SYKEPENGESOKNAD_TOPIC = "flex.sykepengesoknad"
