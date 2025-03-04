package co.nilin.opex.matching.gateway.ports.postgres.util

import co.nilin.opex.matching.gateway.ports.postgres.dto.PairSetting
import co.nilin.opex.matching.gateway.ports.postgres.model.PairSettingModel


fun PairSettingModel.toPairSetting(): PairSetting {
    return PairSetting(
        pair,
        isAvailable,
        updateDate,
    )
}

