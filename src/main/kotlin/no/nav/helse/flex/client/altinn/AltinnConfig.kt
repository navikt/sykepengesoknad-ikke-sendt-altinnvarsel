package no.nav.helse.flex.client.altinn

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class AltinnConfig {
    @Bean
    @Primary
    fun iCorrespondenceAgencyExternalBasic(
        @Value("\${altinn.url}") altinnUrl: String,
    ): ICorrespondenceAgencyExternalBasic =
        WsClient<ICorrespondenceAgencyExternalBasic>()
            .createPort(
                serviceUrl = "$altinnUrl/ServiceEngineExternal/CorrespondenceAgencyExternalBasic.svc",
                portType = ICorrespondenceAgencyExternalBasic::class.java,
                handlers = listOf(LogErrorHandler()),
            )
}
