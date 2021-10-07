package no.nav.helse.flex.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.varsler.VarselPlanlegger
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SykepengesoknadListener(
    val varselPlanlegger: VarselPlanlegger
) {

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory",
        groupId = "sykepengesoknad-ikke-sendt-altinnvarsel-2"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val soknad = cr.value().tilSykepengesoknadDTO()
        varselPlanlegger.planleggVarsler(soknad, Instant.ofEpochMilli(cr.timestamp()))
        acknowledgment.acknowledge()
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}

const val FLEX_SYKEPENGESOKNAD_TOPIC = "flex.sykepengesoknad"
