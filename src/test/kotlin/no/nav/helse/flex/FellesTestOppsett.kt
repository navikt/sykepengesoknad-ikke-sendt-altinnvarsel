package no.nav.helse.flex

import no.nav.helse.flex.kafka.FLEX_SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.kafka.NARMESTELEDER_LEESAH_TOPIC
import no.nav.helse.flex.narmesteleder.NarmesteLederRepository
import no.nav.helse.flex.narmesteleder.domain.NarmesteLederLeesah
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.flex.varsler.PlanlagtVarselRepository
import no.nav.helse.flex.varsler.domain.PlanlagtVarsel
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import okhttp3.mockwebserver.MockWebServer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.OffsetDateTime

private class PostgreSQLContainer14 : PostgreSQLContainer<PostgreSQLContainer14>("postgres:14-alpine")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@EnableMockOAuth2Server
abstract class FellesTestOppsett {
    @Autowired
    lateinit var kafkaProducer: Producer<String, String>

    @Autowired
    lateinit var narmesteLederRepository: NarmesteLederRepository

    @Autowired
    lateinit var planlagtVarselRepository: PlanlagtVarselRepository

    final val fnr = "12345678901"

    companion object {
        var altinnMockWebserver: MockWebServer
        var pdlMockWebserver: MockWebServer

        init {
            PostgreSQLContainer14().also {
                it.start()
                System.setProperty("spring.datasource.url", it.jdbcUrl)
                System.setProperty("spring.datasource.username", it.username)
                System.setProperty("spring.datasource.password", it.password)
            }

            KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.1")).also {
                it.start()
                System.setProperty("KAFKA_BROKERS", it.bootstrapServers)
            }
            altinnMockWebserver =
                MockWebServer()
                    .also { it.start() }
                    .also {
                        System.setProperty("altinn.url", "http://localhost:${it.port}")
                    }

            pdlMockWebserver =
                MockWebServer()
                    .also {
                        System.setProperty("PDL_BASE_URL", "http://localhost:${it.port}")
                    }
        }
    }

    @AfterAll
    fun `Vi tømmer databasen`() {
        narmesteLederRepository.deleteAll()
        planlagtVarselRepository.deleteAll()
    }

    fun sendSykepengesoknad(soknad: SykepengesoknadDTO) {
        kafkaProducer
            .send(
                ProducerRecord(
                    FLEX_SYKEPENGESOKNAD_TOPIC,
                    null,
                    soknad.id,
                    soknad.serialisertTilString(),
                ),
            ).get()
    }

    fun sendNarmesteLederLeesah(nl: NarmesteLederLeesah) {
        kafkaProducer
            .send(
                ProducerRecord(
                    NARMESTELEDER_LEESAH_TOPIC,
                    null,
                    nl.narmesteLederId.toString(),
                    nl.serialisertTilString(),
                ),
            ).get()
    }

    fun planlagteVarslerSomSendesFør(dager: Int): List<PlanlagtVarsel> =
        planlagtVarselRepository.findFirst300ByStatusAndSendesIsBefore(
            PlanlagtVarselStatus.PLANLAGT,
            OffsetDateTime.now().plusDays(dager.toLong()).toInstant(),
        )
}
