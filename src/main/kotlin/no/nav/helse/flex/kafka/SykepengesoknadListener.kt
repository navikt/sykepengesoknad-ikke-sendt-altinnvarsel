package no.nav.helse.flex.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.varsler.VarselPlanlegger
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class SykepengesoknadListener(
    val varselPlanlegger: VarselPlanlegger
) : ConsumerSeekAware {

    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory",
        properties = ["auto.offset.reset = latest"],
        groupId = "sykepengesoknad-ikke-sendt-altinnvarsel-2"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val soknad = cr.value().tilSykepengesoknadDTO()
        varselPlanlegger.planleggVarsler(soknad, Instant.ofEpochMilli(cr.timestamp()))
        acknowledgment.acknowledge()
    }

    override fun registerSeekCallback(callback: ConsumerSeekAware.ConsumerSeekCallback) {
        // register custom callback
        log.info("registerSeekCallback ${this.javaClass.simpleName}")
    }

    override fun onPartitionsAssigned(assignments: Map<TopicPartition, Long>, callback: ConsumerSeekAware.ConsumerSeekCallback) {
        // Seek all the assigned partition to a certain offset
        log.info("onPartitionsAssigned ${this.javaClass.simpleName}")

        val sjetterOktober = LocalDate.of(2021, 10, 6).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        callback.seekToTimestamp(assignments.keys, sjetterOktober)
        log.info("ferdig med seek seekToTimestamp ${this.javaClass.simpleName}")
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}

const val FLEX_SYKEPENGESOKNAD_TOPIC = "flex.sykepengesoknad"
