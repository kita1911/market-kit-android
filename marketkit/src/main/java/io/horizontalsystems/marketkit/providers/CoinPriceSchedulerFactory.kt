package io.censystems.marketkit.providers

import io.censystems.marketkit.managers.CoinPriceManager
import io.censystems.marketkit.managers.ICoinPriceCoinUidDataSource
import io.censystems.marketkit.Scheduler

class CoinPriceSchedulerFactory(
    private val manager: CoinPriceManager,
    private val provider: HsProvider
) {
    fun scheduler(currencyCode: String, coinUidDataSource: ICoinPriceCoinUidDataSource): Scheduler {
        val schedulerProvider = CoinPriceSchedulerProvider(currencyCode, manager, provider)
        schedulerProvider.dataSource = coinUidDataSource
        return Scheduler(schedulerProvider)
    }
}
