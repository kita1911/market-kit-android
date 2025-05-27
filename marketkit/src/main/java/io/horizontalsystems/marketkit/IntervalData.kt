package io.censystems.marketkit

import io.censystems.marketkit.models.HsPointTimePeriod

data class IntervalData(
    val interval: HsPointTimePeriod,
    val fromTimestamp: Long?,
    val visibleTimestamp: Long,
)
