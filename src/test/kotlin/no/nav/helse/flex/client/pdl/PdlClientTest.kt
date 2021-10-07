package no.nav.helse.flex.client.pdl

import no.nav.helse.flex.Testoppsett
import no.nav.helse.flex.mockPdlResponse
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldStartWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PdlClientTest : Testoppsett() {

    @Autowired
    private lateinit var pdlClient: PdlClient

    @Test
    fun `Vi tester happycase`() {
        mockPdlResponse()

        val responseData = pdlClient.hentFormattertNavn(fnr)

        responseData `should be equal to` "Ole Gunnar"
        val request = pdlMockWebserver.takeRequest()
        request.body.readUtf8() `should be equal to` "{\"query\":\"\\nquery(\$ident: ID!){\\n  hentPerson(ident: \$ident) {\\n  \\tnavn(historikk: false) {\\n  \\t  fornavn\\n  \\t  mellomnavn\\n  \\t  etternavn\\n    }\\n  }\\n}\\n\",\"variables\":{\"ident\":\"12345678901\"}}"
        request.headers["Authorization"]!!.shouldStartWith("Bearer ey")
    }
}

