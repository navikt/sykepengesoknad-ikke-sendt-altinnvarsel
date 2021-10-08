package no.nav.helse.flex.client.altinn

import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ExternalContentV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.altinn.schemas.services.serviceengine.notification._2009._10.Notification
import no.altinn.schemas.services.serviceengine.notification._2009._10.NotificationBEList
import no.nav.helse.flex.logger
import no.nav.helse.flex.varsler.domain.PlanlagtVarselType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

@Component
class AltinnVarselMapper(
    @Value("\${overstyr.orgnr:false}") private val overstyrOrgnr: Boolean
) {
    val log = logger()

    fun mapAltinnVarselTilInsertCorrespondence(altinnVarsel: AltinnVarsel): InsertCorrespondenceV2 {
        val namespace = "http://schemas.altinn.no/services/ServiceEngine/Correspondence/2010/10"
        val varsel = when (altinnVarsel.type) {
            PlanlagtVarselType.IKKE_SENDT_SYKEPENGESOKNAD -> arbeidstakerSykepengesoknadAltinnVarsel(altinnVarsel)
            PlanlagtVarselType.IKKE_SENDT_SYKEPENGESOKNAD_MED_REISETILSKUDD -> arbeidstakerGradertReisetilskuddAltinnVarsel(altinnVarsel)
        }

        return InsertCorrespondenceV2()
            .withAllowForwarding(JAXBElement(QName(namespace, "AllowForwarding"), Boolean::class.java, false))
            .withReportee(
                JAXBElement(
                    QName(namespace, "Reportee"), String::class.java,
                    getOrgnummerForSendingTilAltinn(altinnVarsel.orgnummer)
                )
            )
            .withMessageSender(
                JAXBElement(
                    QName(namespace, "MessageSender"), String::class.java,
                    "NAV (Arbeids- og velferdsetaten)"
                )
            )
            .withServiceCode(
                JAXBElement(
                    QName(namespace, "ServiceCode"),
                    String::class.java,
                    SYKEPENGESOEKNAD_TJENESTEKODE
                )
            )
            .withServiceEdition(
                JAXBElement(
                    QName(namespace, "ServiceEdition"),
                    String::class.java,
                    SYKEPENGESOEKNAD_TJENESTEVERSJON
                )
            )
            .withNotifications(
                JAXBElement(
                    QName(namespace, "Notifications"),
                    NotificationBEList::class.java,
                    NotificationBEList()
                        .withNotification(
                            varsel.epost,
                            varsel.sms
                        )
                )
            )
            .withContent(
                JAXBElement(
                    QName(namespace, "Content"),
                    ExternalContentV2::class.java,
                    ExternalContentV2()
                        .withLanguageCode(JAXBElement(QName(namespace, "LanguageCode"), String::class.java, "1044"))
                        .withMessageTitle(JAXBElement(QName(namespace, "MessageTitle"), String::class.java, varsel.tittel))
                        .withMessageBody(JAXBElement(QName(namespace, "MessageBody"), String::class.java, varsel.innhold))
                )
            )
    }

    private fun getOrgnummerForSendingTilAltinn(orgnummer: String): String {
        return if (overstyrOrgnr) {
            log.warn("Overstyrer orgnummer i altinninnsendelse")
            // dette er default orgnummer i test: 'GODVIK OG FLATÅSEN'
            "910067494"
        } else
            orgnummer
    }

    companion object {
        private const val SYKEPENGESOEKNAD_TJENESTEKODE =
            "4751" // OBS! VIKTIG! Denne må ikke endres, da kan feil personer få tilgang til sykepengesøknaden i Altinn!
        private const val SYKEPENGESOEKNAD_TJENESTEVERSJON = "1"
        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    private data class VarselInnhold(
        val tittel: String,
        val innhold: String,
        val epost: Notification,
        val sms: Notification
    )

    private fun arbeidstakerSykepengesoknadAltinnVarsel(
        altinnVarsel: AltinnVarsel
    ) = VarselInnhold(
        tittel = "Manglende søknad om sykepenger - " + altinnVarsel.navnSykmeldt + " (" + altinnVarsel.fnrSykmeldt + ")",
        innhold = """<html>
   <head>
       <meta charset="UTF-8">
   </head>
   <body>
       <div class="melding">
           <h2>Søknad om sykepenger</h2>
           <p>${altinnVarsel.navnSykmeldt} (${altinnVarsel.fnrSykmeldt}) har fått en søknad om sykepenger til utfylling, men har foreløpig ikke sendt den inn.</p>
           <p>Perioden søknaden gjelder for er ${formatter.format(altinnVarsel.soknadFom)}-${
        formatter.format(
            altinnVarsel.soknadTom
        )
        }</p>
           <h4>Om denne meldingen:</h4>
           <p>Denne meldingen er automatisk generert og skal hjelpe arbeidsgivere med å få oversikt over sykepengesøknader som mangler. NAV påtar seg ikke ansvar for eventuell manglende påminnelse. Vi garanterer heller ikke for at foreldelsesfristen ikke er passert, eller om det er andre grunner til at retten til sykepenger ikke er oppfylt. Dersom arbeidstakeren har åpnet en søknad og avbrutt den, vil det ikke bli sendt melding til dere.</p>
       </div>
   </body>
</html>""",
        epost = NotificationAltinnGenerator.opprettEpostNotification(
            "Sykepengesøknad som ikke er sendt inn",
            "<p>En ansatt i \$reporteeName$ (\$reporteeNumber$) har fått en søknad om sykepenger til utfylling, men har foreløpig ikke sendt den inn.</p>" +
                "<p>Logg inn på <a href=\"" + NotificationAltinnGenerator.lenkeAltinnPortal() + "\">Altinn</a> for å se hvem det gjelder og hvilken periode søknaden gjelder for.</p>" +
                "<p>Mer informasjon om digital sykmelding og sykepengesøknad finner du på www.nav.no/digitalsykmelding.</p>" +
                "<p>Vennlig hilsen NAV</p>"
        ),
        sms = NotificationAltinnGenerator.opprettSMSNotification(
            "En ansatt i \$reporteeName$ (\$reporteeNumber$) har fått en søknad om sykepenger til utfylling, men har foreløpig ikke sendt den inn.",
            """Gå til meldingsboksen i ${NotificationAltinnGenerator.smsLenkeAltinnPortal()} for å se hvem det gjelder og hvilken periode søknaden gjelder for. 

Vennlig hilsen NAV"""
        )
    )

    private fun arbeidstakerGradertReisetilskuddAltinnVarsel(
        altinnVarsel: AltinnVarsel
    ) = VarselInnhold(
        tittel = "Manglende søknad om sykepenger med reisetilskudd - " + altinnVarsel.navnSykmeldt + " (" + altinnVarsel.fnrSykmeldt + ")",
        innhold = """<html>
   <head>
       <meta charset="UTF-8">
   </head>
   <body>
       <div class="melding">
           <h2>Søknad om sykepenger med reisetilskudd</h2>
           <p>${altinnVarsel.navnSykmeldt} (${altinnVarsel.fnrSykmeldt}) har fått en søknad om sykepenger med reisetilskudd til utfylling, men har foreløpig ikke sendt den inn.</p>
           <p>Perioden søknaden gjelder for er ${formatter.format(altinnVarsel.soknadFom)}-${
        formatter.format(
            altinnVarsel.soknadTom
        )
        }</p>
           <h4>Om denne meldingen:</h4>
           <p>Denne meldingen er automatisk generert og skal hjelpe arbeidsgivere med å få oversikt over sykepengesøknader som mangler. NAV påtar seg ikke ansvar for eventuell manglende påminnelse. Vi garanterer heller ikke for at foreldelsesfristen ikke er passert, eller om det er andre grunner til at retten til sykepenger ikke er oppfylt. Dersom arbeidstakeren har åpnet en søknad og avbrutt den, vil det ikke bli sendt melding til dere.</p>
       </div>
   </body>
</html>""",
        epost = NotificationAltinnGenerator.opprettEpostNotification(
            "Søknad om sykepenger med reisetilskudd som ikke er sendt inn",
            "<p>En ansatt i \$reporteeName$ (\$reporteeNumber$) har fått en søknad om sykepenger med reisetilskudd til utfylling, men har foreløpig ikke sendt den inn.</p>" +
                "<p>Logg inn på <a href=\"" + NotificationAltinnGenerator.lenkeAltinnPortal() + "\">Altinn</a> for å se hvem det gjelder og hvilken periode søknaden gjelder for.</p>" +
                "<p>Mer informasjon om digital sykmelding og sykepengesøknad finner du på www.nav.no/digitalsykmelding.</p>" +
                "<p>Vennlig hilsen NAV</p>"
        ),
        sms = NotificationAltinnGenerator.opprettSMSNotification(
            "En ansatt i \$reporteeName$ (\$reporteeNumber$) har fått en søknad om sykepenger med reisetilskudd til utfylling, men har foreløpig ikke sendt den inn.",
            """Gå til meldingsboksen i ${NotificationAltinnGenerator.smsLenkeAltinnPortal()} for å se hvem det gjelder og hvilken periode søknaden gjelder for. 

Vennlig hilsen NAV"""
        )
    )
}
