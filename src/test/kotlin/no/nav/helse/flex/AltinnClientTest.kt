package no.nav.helse.flex

import no.altinn.services.serviceengine.correspondence._2009._10.InsertCorrespondenceBasicV2
import no.nav.helse.flex.client.altinn.AltinnVarsel
import no.nav.helse.flex.client.altinn.AltinnVarselClient
import no.nav.helse.flex.varsler.domain.PlanlagtVarsel
import no.nav.helse.flex.varsler.domain.PlanlagtVarselStatus
import no.nav.helse.flex.varsler.domain.PlanlagtVarselType
import okhttp3.mockwebserver.RecordedRequest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.io.StringReader
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader

class AltinnClientTest : FellesTestOppsett() {
    @Autowired
    lateinit var altinnVarselClient: AltinnVarselClient

    @Test
    fun `test webservice kall til altinn`() {
        mockAltinnResponse()

        val altinnVarsel =
            AltinnVarsel(
                navnSykmeldt = "Max Mekker",
                planlagtVarsel =
                    PlanlagtVarsel(
                        brukerFnr = "123123",
                        orgnummer = "234234",
                        sykepengesoknadId = UUID.randomUUID().toString(),
                        soknadFom = LocalDate.now(),
                        soknadTom = LocalDate.now(),
                        varselType = PlanlagtVarselType.IKKE_SENDT_SYKEPENGESOKNAD,
                        id = null,
                        oppdatert = Instant.now(),
                        sendes = Instant.now(),
                        status = PlanlagtVarselStatus.PLANLAGT,
                    ),
            )

        altinnVarselClient.sendManglendeInnsendingAvSoknadMeldingTilArbeidsgiver(altinnVarsel)

        val soapRequest = altinnMockWebserver.takeRequest()

        val insertCorrespondenceBasicV2 = soapRequest.parseCorrespondence()
        insertCorrespondenceBasicV2.systemUserCode `should be equal to` "NAV_DIGISYFO"
        insertCorrespondenceBasicV2.correspondence.content.value.messageTitle.value `should be equal to`
            "Manglende søknad om sykepenger - Max Mekker (123123)"
        insertCorrespondenceBasicV2.correspondence.reportee.value `should be equal to` "234234"
        soapRequest.path `should be equal to` "/ServiceEngineExternal/CorrespondenceAgencyExternalBasic.svc"
        soapRequest.method `should be equal to` "POST"
    }

    fun RecordedRequest.parseCorrespondence(): InsertCorrespondenceBasicV2 {
        val requestBody = this.body.readUtf8()
        val sr = XMLInputFactory.newFactory().createXMLStreamReader(StringReader(requestBody))
        while (sr.hasNext()) {
            val type = sr.next()
            if (type == XMLStreamReader.START_ELEMENT && "InsertCorrespondenceBasicV2" == sr.localName) {
                val jc: JAXBContext = JAXBContext.newInstance(InsertCorrespondenceBasicV2::class.java)
                val unmarshaller = jc.createUnmarshaller()
                val je: JAXBElement<InsertCorrespondenceBasicV2> = unmarshaller.unmarshal(sr, InsertCorrespondenceBasicV2::class.java)
                return je.value
            }
        }
        throw RuntimeException("Fant ikke forventa element")
    }
}
