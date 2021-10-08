package no.nav.helse.flex.client.altinn

import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptExternal
import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage
import no.nav.helse.flex.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AltinnVarselClient(
    private val altinnVarselMapper: AltinnVarselMapper,
    private val iCorrespondenceAgencyExternalBasic: ICorrespondenceAgencyExternalBasic,
    @param:Value("\${altinn.username}") private val username: String,
    @param:Value("\${altinn.password}") private val password: String
) {
    val log = logger()

    fun sendManglendeInnsendingAvSoknadMeldingTilArbeidsgiver(altinnVarsel: AltinnVarsel): ReceiptExternal {
        try {
            val receiptExternal = iCorrespondenceAgencyExternalBasic.insertCorrespondenceBasicV2(
                username, password,
                SYSTEM_USER_CODE,
                altinnVarsel.planlagtVarsel.sykepengesoknadId,
                altinnVarselMapper.mapAltinnVarselTilInsertCorrespondence(altinnVarsel)
            )
            if (receiptExternal.receiptStatusCode != ReceiptStatusEnum.OK) {
                log.error("Fikk uventet statuskode fra Altinn {}", receiptExternal.receiptStatusCode)
                throw RuntimeException("Feil ved sending varsel om manglende innsending av sykepengesøknad til Altinn")
            }
            return receiptExternal
        } catch (e: ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage) {
            log.error("Feil ved sending varsel om manglende innsending av sykepengesøknad til Altinn", e)
            throw RuntimeException("Feil ved sending varsel om manglende innsending av sykepengesøknad til Altinn", e)
        } catch (e: Exception) {
            log.error("Feil ved sending varsel om manglende innsending av sykepengesøknad til Altinn", e)
            throw RuntimeException("Feil ved sending varsel om manglende innsending av sykepengesøknad til Altinn", e)
        }
    }

    companion object {
        const val SYSTEM_USER_CODE = "NAV_DIGISYFO"
    }
}
