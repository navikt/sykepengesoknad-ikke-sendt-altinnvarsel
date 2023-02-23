package no.nav.helse.flex.client.altinn

import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType
import no.altinn.schemas.services.serviceengine.notification._2009._10.Notification
import no.altinn.schemas.services.serviceengine.notification._2009._10.ReceiverEndPoint
import no.altinn.schemas.services.serviceengine.notification._2009._10.ReceiverEndPointBEList
import no.altinn.schemas.services.serviceengine.notification._2009._10.TextToken
import no.altinn.schemas.services.serviceengine.notification._2009._10.TextTokenSubstitutionBEList
import java.lang.System
import java.util.*
import java.util.function.Function
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

internal object NotificationAltinnGenerator {
    private const val NORSK_BOKMAL = "1044"
    private const val FRA_EPOST_ALTINN = "noreply@altinn.no"
    private const val NOTIFICATION_NAMESPACE = "http://schemas.altinn.no/services/ServiceEngine/Notification/2009/10"
    private fun urlEncode(lenke: String): String {
        return lenke.replace("=".toRegex(), "%3D")
    }

    fun smsLenkeAltinnPortal(): String {
        return urlEncode(lenkeAltinnPortal())
    }

    fun lenkeAltinnPortal(): String {
        return System.getProperty("altinn.portal.baseurl", "https://www.altinn.no") + "/ui/MessageBox?O=\$reporteeNumber$"
    }

    fun opprettEpostNotification(vararg text: String): Notification {
        return opprettNotification(FRA_EPOST_ALTINN, TransportType.EMAIL, *text)
    }

    fun opprettSMSNotification(vararg text: String): Notification {
        return opprettNotification(null, TransportType.SMS, *text)
    }

    private fun opprettNotification(fraEpost: String?, type: TransportType, vararg text: String): Notification {
        return opprettNotification(fraEpost, type, konverterTilTextTokens(*text))
    }

    private fun opprettNotification(fraEpost: String?, type: TransportType, textTokens: Array<TextToken?>): Notification {
        require(textTokens.size == 2) { "Antall textTokens må være 2. Var " + textTokens.size }
        return Notification()
            .withLanguageCode(JAXBElement(QName(NOTIFICATION_NAMESPACE, "LanguageCode"), String::class.java, NORSK_BOKMAL))
            .withNotificationType(JAXBElement(QName(NOTIFICATION_NAMESPACE, "NotificationType"), String::class.java, "TokenTextOnly"))
            .withFromAddress(mapNullable(fraEpost) { epost: String? -> JAXBElement(QName(NOTIFICATION_NAMESPACE, "FromAddress"), String::class.java, epost) })
            .withReceiverEndPoints(
                JAXBElement(
                    QName(NOTIFICATION_NAMESPACE, "ReceiverEndPoints"),
                    ReceiverEndPointBEList::class.java,
                    ReceiverEndPointBEList()
                        .withReceiverEndPoint(
                            ReceiverEndPoint()
                                .withTransportType(JAXBElement(QName(NOTIFICATION_NAMESPACE, "TransportType"), TransportType::class.java, type))
                        )
                )
            )
            .withTextTokens(
                JAXBElement(
                    QName(NOTIFICATION_NAMESPACE, "TextTokens"),
                    TextTokenSubstitutionBEList::class.java,
                    TextTokenSubstitutionBEList()
                        .withTextToken(*textTokens)
                )
            )
    }

    private fun konverterTilTextTokens(vararg text: String): Array<TextToken?> {
        val textTokens = arrayOfNulls<TextToken>(text.size)
        for (i in 0 until text.size) {
            textTokens[i] = TextToken().withTokenNum(i).withTokenValue(
                JAXBElement(QName(NOTIFICATION_NAMESPACE, "TokenValue"), String::class.java, text[i])
            )
        }
        return textTokens
    }

    private fun <T, R> mapNullable(fra: T, exp: Function<T, R>): R {
        return Optional.ofNullable(fra).map(exp).orElse(null)
    }
}
