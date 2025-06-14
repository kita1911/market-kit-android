package io.censystems.marketkit.models

import java.math.BigDecimal

data class ChartPoint(
    val value: BigDecimal,
    val timestamp: Long,
    val volume: BigDecimal?
)
