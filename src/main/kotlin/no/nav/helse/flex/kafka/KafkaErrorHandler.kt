package no.nav.helse.flex.kafka

import no.nav.helse.flex.logger
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.*
import org.springframework.stereotype.Component
import org.springframework.util.backoff.ExponentialBackOff

@Component
class KafkaErrorHandler : DefaultErrorHandler(
    null,
    ExponentialBackOff(1000L, 1.5).also {
        // 8 minutter, som er mindre enn max.poll.interval.ms på 10 minutter.
        it.maxInterval = 60_000L * 8
    }
) {
    val log = logger()

    override fun handleRemaining(
        thrownException: java.lang.Exception,
        records: MutableList<ConsumerRecord<*, *>>,
        consumer: Consumer<*, *>,
        container: MessageListenerContainer
    ) {
        log.error("Feil i listener:", thrownException)

        records.forEach { record ->
            log.error(
                "Feil i prossesseringen av record med offset: ${record.offset()}, key: ${record.key()} på topic ${record.topic()}"
            )
        }

        super.handleRemaining(thrownException, records, consumer, container)
    }
}
