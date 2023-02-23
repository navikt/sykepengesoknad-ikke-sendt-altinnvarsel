package no.nav.helse.flex.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.Serializable

@Configuration
class TestKafkaBeans(
    private val kafkaConfig: KafkaConfig
) {

    @Bean
    fun stringStringProducer(): KafkaProducer<String, String> =
        KafkaProducer<String, String>(kafkaConfig.producerConfig(StringSerializer::class.java))

    @Bean
    fun kafkaConsumer() = KafkaConsumer<String, String>(consumerConfig())

    private fun consumerConfig() = mapOf(
        ConsumerConfig.GROUP_ID_CONFIG to "testing-group-id",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"

    ) + kafkaConfig.commonConfig()

    private fun producerConfig(): Map<String, Serializable> {
        return mapOf(
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to "1",
            ProducerConfig.MAX_BLOCK_MS_CONFIG to "15000",
            ProducerConfig.RETRIES_CONFIG to "100000"
        )
    }

    fun testConsumerProps(groupId: String) = mapOf(
        ConsumerConfig.GROUP_ID_CONFIG to groupId,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to "1"
    ) + kafkaConfig.commonConfig()
}
