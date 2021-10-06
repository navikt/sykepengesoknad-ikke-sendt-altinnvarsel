package no.nav.helse.flex.client.altinn

import org.springframework.stereotype.Component

@Component
class DevOrgnummerWhitelist {

    fun allowsOrgnummer(orgnummer: String): Boolean {
        return allowedOrgnummer.contains(orgnummer)
    }

    val allowedOrgnummer = setOf(
        "910127128",
        "910024426",
        "910105531",
        "910015907",
        "910055844",
        "910019600",
        "910014587",
        "910025996",
        "910107895",
        "910108204",
        "910106104",
        "910106554",
        "910107542",
        "910081241",
        "910022776",
        "910067494",
        "810024992",
        "910079611",
        "910026194",
        "910106503",
        "910106384",
        "910019600",
        "910128450",
        "910104799",
        "910127020",
        "124578129",
        "810514442",
        "910033255",
        "910127128",
        "910089358",
        "910521616",
        "910532235",
        "910532278",
        "910532308",
        "910532251",
        "910128574",
        "910128728",
        "910128523",
        "910108506",
        "910108549",
        "910108581",
        "910107348",
        "910107372",
        "910532162",
        "910532189",
        "910532219",
        "811297992",
        "811297712",
        "811297542",
        "811296872",
        "811297062",
        "811296562",
        "811289612",
        "811289302",
        "811289892",
        "810269812",
        "810270012",
        "810272562",
        "810272872",
        "811290742",
        "811290572",
        "811290262",
        "811295612",
        "811295892",
        "811296082",
        "811294012",
        "811293822",
        "811294292",
        "811291102",
        "811291382",
        "811291692"
    )
}
