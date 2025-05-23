package no.nav.helse.flex

import no.nav.helse.flex.narmesteleder.domain.NarmesteLederLeesah
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNull
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

class OppdaterNarmesteLederTest : FellesTestOppsett() {
    @Test
    fun `Oppretter ny nærmeste leder hvis den ikke finnes fra før`() {
        narmesteLederRepository.deleteAll()
        val narmesteLederId = UUID.randomUUID()
        narmesteLederRepository.findByNarmesteLederId(narmesteLederId).shouldBeNull()

        val narmesteLederLeesah = getNarmesteLederLeesah(narmesteLederId)

        narmesteLederRepository
            .finnForskuttering(
                narmesteLederLeesah.fnr,
                narmesteLederLeesah.orgnummer,
            )?.arbeidsgiverForskutterer
            .shouldBeNull()

        sendNarmesteLederLeesah(narmesteLederLeesah)

        await().atMost(10, TimeUnit.SECONDS).until {
            narmesteLederRepository.findByNarmesteLederId(narmesteLederId) != null
        }

        val narmesteLeder = narmesteLederRepository.findByNarmesteLederId(narmesteLederId)
        narmesteLeder.shouldNotBeNull()
        narmesteLeder.narmesteLederEpost `should be equal to` narmesteLederLeesah.narmesteLederEpost
        narmesteLeder.aktivTom.shouldBeNull()

        narmesteLederRepository
            .finnForskuttering(
                narmesteLederLeesah.fnr,
                narmesteLederLeesah.orgnummer,
            )?.arbeidsgiverForskutterer!!
            .shouldBeTrue()
    }

    @Test
    fun `Oppdaterer nærmeste leder hvis den finnes fra før`() {
        narmesteLederRepository.deleteAll()
        val narmesteLederId = UUID.randomUUID()
        val narmesteLederLeesah = getNarmesteLederLeesah(narmesteLederId)
        narmesteLederRepository
            .finnForskuttering(
                narmesteLederLeesah.fnr,
                narmesteLederLeesah.orgnummer,
            )?.arbeidsgiverForskutterer
            .shouldBeNull()

        sendNarmesteLederLeesah(narmesteLederLeesah)
        await().atMost(10, TimeUnit.SECONDS).until {
            narmesteLederRepository.findByNarmesteLederId(narmesteLederId) != null
        }

        val narmesteLeder = narmesteLederRepository.findByNarmesteLederId(narmesteLederId)!!
        narmesteLeder.narmesteLederTelefonnummer `should be equal to` "90909090"
        narmesteLeder.narmesteLederEpost `should be equal to` "test@nav.no"
        narmesteLederRepository
            .finnForskuttering(
                narmesteLederLeesah.fnr,
                narmesteLederLeesah.orgnummer,
            )?.arbeidsgiverForskutterer!!
            .shouldBeTrue()

        sendNarmesteLederLeesah(
            getNarmesteLederLeesah(
                narmesteLederId,
                telefonnummer = "98989898",
                epost = "mail@banken.no",
                aktivTom = LocalDate.now(),
                arbeidsgiverForskutterer = false,
            ),
        )

        await().atMost(10, TimeUnit.SECONDS).until {
            narmesteLederRepository.findByNarmesteLederId(narmesteLederId)!!.narmesteLederEpost == "mail@banken.no"
        }

        narmesteLederRepository
            .finnForskuttering(
                narmesteLederLeesah.fnr,
                narmesteLederLeesah.orgnummer,
            )?.arbeidsgiverForskutterer!!
            .shouldBeFalse()

        val oppdaterNl = narmesteLederRepository.findByNarmesteLederId(narmesteLederId)!!
        oppdaterNl.narmesteLederTelefonnummer `should be equal to` "98989898"
        oppdaterNl.narmesteLederEpost `should be equal to` "mail@banken.no"
        oppdaterNl.aktivTom `should be equal to` LocalDate.now()
    }
}

fun getNarmesteLederLeesah(
    narmesteLederId: UUID,
    telefonnummer: String = "90909090",
    epost: String = "test@nav.no",
    aktivTom: LocalDate? = null,
    fnr: String = "12345678910",
    orgnummer: String = "999999",
    arbeidsgiverForskutterer: Boolean? = true,
): NarmesteLederLeesah =
    NarmesteLederLeesah(
        narmesteLederId = narmesteLederId,
        fnr = fnr,
        orgnummer = orgnummer,
        narmesteLederFnr = "01987654321",
        narmesteLederTelefonnummer = telefonnummer,
        narmesteLederEpost = epost,
        aktivFom = LocalDate.now(),
        aktivTom = aktivTom,
        arbeidsgiverForskutterer = arbeidsgiverForskutterer,
        timestamp = OffsetDateTime.now(ZoneOffset.UTC),
    )
