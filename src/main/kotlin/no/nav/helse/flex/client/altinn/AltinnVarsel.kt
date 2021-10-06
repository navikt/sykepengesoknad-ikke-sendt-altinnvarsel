package no.nav.helse.flex.client.altinn

import no.nav.helse.flex.varsler.domain.PlanlagtVarselType
import java.time.LocalDate

data class AltinnVarsel(
    val id: Long,
    val ressursId: String,
    val type: PlanlagtVarselType,
    val orgnummer: String,
    val aktorId: String,
    val fnrSykmeldt: String,
    val navnSykmeldt: String,
    val soknadFom: LocalDate,
    val soknadTom: LocalDate
)
