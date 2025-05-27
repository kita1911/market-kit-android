package io.censystems.marketkit.managers

import io.censystems.marketkit.models.GlobalMarketInfo
import io.censystems.marketkit.models.GlobalMarketPoint
import io.censystems.marketkit.models.HsTimePeriod
import io.censystems.marketkit.providers.HsProvider
import io.censystems.marketkit.storage.GlobalMarketInfoStorage
import io.reactivex.Single

class GlobalMarketInfoManager(
    private val provider: HsProvider,
    private val storage: GlobalMarketInfoStorage
) {
    private val expirationInterval = 600 // 10 minutes

    fun globalMarketInfoSingle(currencyCode: String, timePeriod: HsTimePeriod): Single<List<GlobalMarketPoint>> {
        val currentTimestamp = System.currentTimeMillis() / 1000

        storage.globalMarketInfo(currencyCode, timePeriod)?.let { data ->
            if (currentTimestamp - data.timestamp <= expirationInterval)
                return Single.just(data.points)
        }

        return provider.getGlobalMarketPointsSingle(currencyCode, timePeriod)
            .map { globalMarketPoints ->
                storage.save(GlobalMarketInfo(currencyCode, timePeriod, globalMarketPoints, currentTimestamp))

                globalMarketPoints
            }
    }
}
