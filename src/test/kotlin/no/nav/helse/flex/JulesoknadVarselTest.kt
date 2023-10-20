package no.nav.helse.flex

import no.nav.helse.flex.sykepengesoknad.kafka.*
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus.*
import no.nav.helse.flex.varsler.domain.PlanlagtVarselType.IKKE_SENDT_SYKEPENGESOKNAD
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class JulesoknadVarselTest : Testoppsett() {

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

    @Test
    @Order(1)
    fun `Vi mottar en søknad med status NY og tom fremtiden fordi det er en julesøknad og planlegger et manglende søknad varsel med riktig sendestidspunkt`() {
        planlagteVarslerSomSendesFør(dager = 999999).size `should be equal to` 0

        sendSykepengesoknad(soknad.copy(tom = LocalDate.of(2043, 12, 24))) // Denne testen knekker hvis koden fortsatt lever om 20 år.

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
        planlagtVarsel.soknadTom `should be equal to` LocalDate.of(2043, 12, 24)
        planlagtVarsel.sendes.shouldBeBefore(LocalDate.of(2044, 1, 16).atStartOfDay().toInstant(ZoneOffset.UTC))
        planlagtVarsel.sendes.shouldBeAfter(LocalDate.of(2044, 1, 15).atStartOfDay().toInstant(ZoneOffset.UTC))

        planlagteVarslerSomSendesFør(dager = 999999).size `should be equal to` 1
    }
}
