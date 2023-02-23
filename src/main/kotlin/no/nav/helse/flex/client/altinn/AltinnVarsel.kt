package no.nav.helse.flex.client.altinn

import no.nav.helse.flex.varsler.domain.PlanlagtVarsel

data class AltinnVarsel(
    val planlagtVarsel: PlanlagtVarsel,
    val navnSykmeldt: String
)
