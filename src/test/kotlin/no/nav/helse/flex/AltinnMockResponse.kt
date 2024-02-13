package no.nav.helse.flex

import okhttp3.mockwebserver.MockResponse

fun mockAltinnResponse() {
    val response = """
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
            <InsertCorrespondenceBasicV2Response xmlns="http://www.altinn.no/services/ServiceEngine/Correspondence/2009/10" xmlns:res="http://schemas.altinn.no/services/Intermediary/Receipt/2009/10">
                <InsertCorrespondenceBasicV2Result>
                    <res:ReceiptStatusCode>OK</res:ReceiptStatusCode>
                </InsertCorrespondenceBasicV2Result>
            </InsertCorrespondenceBasicV2Response>
        </soap:Body>
    </soap:Envelope>"""

    FellesTestOppsett.altinnMockWebserver.enqueue(MockResponse().setBody(response))
}
