package no.nav.helse.flex

import no.nav.helse.flex.varsler.domain.PlanlagtVarsel
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus.*
import no.nav.helse.flex.varsler.domain.PlanlagtVarselType.IKKE_SENDT_SYKEPENGESOKNAD
import no.nav.syfo.kafka.felles.*
import no.nav.syfo.kafka.felles.SoknadsstatusDTO.SENDT
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class VarselTest : Testoppsett() {

    val orgnummer = "999111555"
    val soknad = SykepengesoknadDTO(
        fnr = fnr,
        id = UUID.randomUUID().toString(),
        type = SoknadstypeDTO.ARBEIDSTAKERE,
        status = SoknadsstatusDTO.NY,
        fom = LocalDate.now().minusDays(1),
        tom = LocalDate.now(),
        arbeidssituasjon = ArbeidssituasjonDTO.ARBEIDSTAKER,
        arbeidsgiver = ArbeidsgiverDTO(navn = "Bedriften AS", orgnummer = orgnummer)
    )

    fun planlagteVarslerSomSendesFør(dager: Int): List<PlanlagtVarsel> {
        return planlagtVarselRepository.findFirst300ByStatusAndSendesIsBefore(
            PLANLAGT,
            OffsetDateTime.now().plusDays(dager.toLong())
        )
    }

    @Test
    @Order(0)
    fun `Arbeidsledig, frilanser og sånt skaper ikke planlagt varsel`() {

        planlagtVarselRepository.findAll().iterator().asSequence().toList().isEmpty()

        val arbeidsledig = SykepengesoknadDTO(
            fnr = fnr,
            id = UUID.randomUUID().toString(),
            type = SoknadstypeDTO.ARBEIDSLEDIG,
            status = SoknadsstatusDTO.NY,
            arbeidssituasjon = ArbeidssituasjonDTO.ARBEIDSLEDIG,
        )
        val frilanser = SykepengesoknadDTO(
            fnr = fnr,
            id = UUID.randomUUID().toString(),
            type = SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE,
            status = SoknadsstatusDTO.NY,
            arbeidssituasjon = ArbeidssituasjonDTO.FRILANSER,
        )

        sendSykepengesoknad(arbeidsledig)
        sendSykepengesoknad(frilanser)

        await().during(3, SECONDS).until {
            planlagtVarselRepository.findAll().iterator().asSequence().toList().isEmpty()
        }

        planlagtVarselRepository.findAll().iterator().asSequence().toList().shouldBeEmpty()
    }

    @Test
    @Order(1)
    fun `Vi mottar en søknad med status NY og planlegger et manglende søknad varsel`() {

        planlagteVarslerSomSendesFør(dager = 20).size `should be equal to` 0

        sendSykepengesoknad(soknad)

        await().atMost(5, SECONDS).until {
            planlagtVarselRepository.findBySykepengesoknadId(soknad.id).size == 1
        }

        val planlagtVarsel = planlagtVarselRepository.findBySykepengesoknadId(soknad.id).first()
        planlagtVarsel.brukerFnr `should be equal to` soknad.fnr
        planlagtVarsel.sykepengesoknadId `should be equal to` soknad.id
        planlagtVarsel.orgnummer `should be equal to` soknad.arbeidsgiver!!.orgnummer
        planlagtVarsel.varselType `should be equal to` IKKE_SENDT_SYKEPENGESOKNAD
        planlagtVarsel.status `should be equal to` PLANLAGT
        planlagtVarsel.soknadFom `should be equal to` LocalDate.now().minusDays(1)
        planlagtVarsel.soknadTom `should be equal to` LocalDate.now()
        planlagtVarsel.sendes.shouldBeBefore(OffsetDateTime.now().plusDays(24).toInstant())
        planlagtVarsel.sendes.shouldBeAfter(OffsetDateTime.now().plusDays(20).toInstant())

        planlagteVarslerSomSendesFør(dager = 25).size `should be equal to` 1
    }

    @Test
    @Order(2)
    fun `Vi mottar en søknad med status SENDT og avbryter manglende søknad varsel `() {
        planlagteVarslerSomSendesFør(dager = 25).size `should be equal to` 1
        planlagtVarselRepository.findBySykepengesoknadId(soknad.id).size `should be equal to` 1
        planlagteVarslerSomSendesFør(dager = 30).size `should be equal to` 1
        planlagteVarslerSomSendesFør(dager = 3).size `should be equal to` 0
        val soknaden = soknad.copy(status = SENDT, sendtArbeidsgiver = LocalDateTime.now())
        sendSykepengesoknad(soknaden)

        await().atMost(5, SECONDS).until {
            planlagtVarselRepository.findBySykepengesoknadId(soknad.id).first().status == AVBRUTT
        }

        val planlagteVarsler = planlagtVarselRepository.findBySykepengesoknadId(soknad.id)
        planlagteVarsler.size `should be equal to` 1

        val avbruttVarsel = planlagteVarsler.first { it.status == AVBRUTT }
        avbruttVarsel.brukerFnr `should be equal to` soknad.fnr
        avbruttVarsel.sykepengesoknadId `should be equal to` soknad.id
        avbruttVarsel.orgnummer `should be equal to` soknad.arbeidsgiver!!.orgnummer
        avbruttVarsel.varselType `should be equal to` IKKE_SENDT_SYKEPENGESOKNAD
        avbruttVarsel.status `should be equal to` AVBRUTT
        planlagteVarslerSomSendesFør(dager = 25).size `should be equal to` 0
    }
}