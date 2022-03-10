# sykepengesoknad-ikke-sendt-altinnvarsel
Ansvarlig for å sende varsel om ikke sendt sykepengesøknad til arbeidsgiver i altinn.

## Utsendelse av varsler til altinn
Varsel om manglende sykepengesøknad planlegges sendt to uker at søknaden har blitt tilgjengelig for innsendelse.

## Data
Applikasjonen har en database i GCP.

Tabellen `planlagt varsel` holder oversikt over alle planlagte, avbrutte og sendte varsler.
Tabellen inkluderer fødselsnummer, orgnummer og sykpengesøknad_id og er derfor personidentifiserbar. Det slettes ikke data fra tabellen.

Tabellen `narmeste leder` holder oversikt over alle nærmesteleder relasjoner og forskutteringsstatus fr anærmeste leder skjemaet.
Dataene er personidentifiserbare.
Det slettes ikke fra tabellen.


# Komme i gang

Bygges med gradle. Standard spring boot oppsett.

---

# Henvendelser


Spørsmål knyttet til koden eller prosjektet kan stilles til flex@nav.no

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #flex.
