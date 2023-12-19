package no.nav.helse.flex

import no.nav.helse.flex.narmesteleder.OppdateringAvNarmesteLeder
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ForskutteringTest : Testoppsett() {
    final val record1 =
        getNarmesteLederLeesah(
            aktivTom = null,
            arbeidsgiverForskutterer = null,
            narmesteLederId = UUID.randomUUID(),
        )
    final val record2 = record1.copy(arbeidsgiverForskutterer = true)
    final val record3 = record2.copy(aktivTom = LocalDate.now().minusDays(5))
    final val record4 =
        getNarmesteLederLeesah(
            aktivTom = null,
            arbeidsgiverForskutterer = false,
            narmesteLederId = UUID.randomUUID(),
        )
    final val record5 = record4.copy(aktivTom = LocalDate.now().minusDays(4))
    final val record6 = record5.copy(aktivTom = LocalDate.now().minusDays(6))

    @Autowired
    lateinit var oppdateringAvNarmesteLeder: OppdateringAvNarmesteLeder

    @Test
    @Order(0)
    fun `først er forskuttering ukjent`() {
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer).shouldBeNull()
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer)?.arbeidsgiverForskutterer.shouldBeNull()
    }

    @Test
    @Order(1)
    fun `så er forskuttering fortsatt ukjent, men vi får en record`() {
        oppdateringAvNarmesteLeder.behandleMeldingFraKafka(record1.serialisertTilString())
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer).shouldNotBeNull()
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer)?.arbeidsgiverForskutterer.shouldBeNull()
    }

    @Test
    @Order(2)
    fun `Vi oppdaterer til forskutterer true`() {
        oppdateringAvNarmesteLeder.behandleMeldingFraKafka(record2.serialisertTilString())
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer)?.arbeidsgiverForskutterer!!.shouldBeTrue()
    }

    @Test
    @Order(3)
    fun `Vi bryter nl kobling, men forskutterer er fortsatt true`() {
        oppdateringAvNarmesteLeder.behandleMeldingFraKafka(record3.serialisertTilString())
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer)?.arbeidsgiverForskutterer!!.shouldBeTrue()
    }

    @Test
    @Order(4)
    fun `Vi sender nytt skjema med forskuttering false`() {
        oppdateringAvNarmesteLeder.behandleMeldingFraKafka(record4.serialisertTilString())
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer)?.arbeidsgiverForskutterer!!.shouldBeFalse()
    }

    @Test
    @Order(5)
    fun `Vi bryter nl kobling, men forskutterer er fortsatt false`() {
        oppdateringAvNarmesteLeder.behandleMeldingFraKafka(record5.serialisertTilString())
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer)?.arbeidsgiverForskutterer!!.shouldBeFalse()
    }

    @Test
    @Order(6)
    fun `Vi endrer siste skjema til å ha gammel aktiv tom, da blir forskuttering true`() {
        oppdateringAvNarmesteLeder.behandleMeldingFraKafka(record6.serialisertTilString())
        narmesteLederRepository.finnForskuttering(record1.fnr, record1.orgnummer)?.arbeidsgiverForskutterer!!.shouldBeTrue()
    }
}
