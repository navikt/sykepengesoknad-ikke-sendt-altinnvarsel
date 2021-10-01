package no.nav.helse.flex

import no.nav.helse.flex.kafka.FLEX_SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.kafka.NARMESTELEDER_LEESAH_TOPIC
import no.nav.helse.flex.narmesteleder.NarmesteLederRepository
import no.nav.helse.flex.narmesteleder.domain.NarmesteLederLeesah
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.annotation.PostConstruct

private class PostgreSQLContainer12 : PostgreSQLContainer<PostgreSQLContainer12>("postgres:12-alpine")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@EnableMockOAuth2Server
abstract class Testoppsett {

    @Autowired
    lateinit var kafkaProducer: Producer<String, String>

    var pdlMockServer: MockRestServiceServer? = null
    var syfoServiceStanglerMockServer: MockRestServiceServer? = null

    @Autowired
    lateinit var pdlRestTemplate: RestTemplate

    @Autowired
    lateinit var narmesteLederRepository: NarmesteLederRepository

    final val fnr = "12345678901"
    final val aktorId = "aktorid123"

    companion object {
        init {
            PostgreSQLContainer12().also {
                it.start()
                System.setProperty("spring.datasource.url", it.jdbcUrl)
                System.setProperty("spring.datasource.username", it.username)
                System.setProperty("spring.datasource.password", it.password)
            }

            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.1")).also {
                it.start()
                System.setProperty("KAFKA_BROKERS", it.bootstrapServers)
            }
        }
    }

    @PostConstruct
    fun setupRestServiceServers() {
        if (pdlMockServer == null) {
            pdlMockServer = MockRestServiceServer.createServer(pdlRestTemplate)
        }
    }

    @AfterAll
    fun `Vi tømmer databasen`() {
        narmesteLederRepository.deleteAll()
    }

    fun sendSykepengesoknad(soknad: SykepengesoknadDTO) {
        kafkaProducer.send(
            ProducerRecord(
                FLEX_SYKEPENGESOKNAD_TOPIC,
                null,
                soknad.id,
                soknad.serialisertTilString()
            )
        ).get()
    }

    fun sendNarmesteLederLeesah(nl: NarmesteLederLeesah) {
        kafkaProducer.send(
            ProducerRecord(
                NARMESTELEDER_LEESAH_TOPIC,
                null,
                nl.narmesteLederId.toString(),
                nl.serialisertTilString()
            )
        ).get()
    }
}
