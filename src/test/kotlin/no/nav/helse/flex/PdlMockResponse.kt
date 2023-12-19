package no.nav.helse.flex

import no.nav.helse.flex.Testoppsett.Companion.pdlMockWebserver
import no.nav.helse.flex.client.pdl.HentNavn
import no.nav.helse.flex.client.pdl.HentNavnResponse
import no.nav.helse.flex.client.pdl.HentNavnResponseData
import no.nav.helse.flex.client.pdl.Navn
import okhttp3.mockwebserver.MockResponse
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType

fun mockPdlResponse(
    hentNavnResponse: HentNavnResponse =
        HentNavnResponse(
            errors = emptyList(),
            data =
                HentNavnResponseData(
                    hentPerson =
                        HentNavn(
                            navn =
                                listOf(
                                    Navn(fornavn = "OLE", mellomnavn = null, etternavn = "GUNNAR"),
                                ),
                        ),
                ),
        ),
) {
    val response =
        MockResponse()
            .setBody(hentNavnResponse.serialisertTilString())
            .setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

    pdlMockWebserver.enqueue(response)
}
