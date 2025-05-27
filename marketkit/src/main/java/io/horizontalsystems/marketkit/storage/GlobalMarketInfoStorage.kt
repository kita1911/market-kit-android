package io.censystems.marketkit.storage

import io.censystems.marketkit.models.GlobalMarketInfo
import io.censystems.marketkit.models.HsTimePeriod

class GlobalMarketInfoStorage(marketDatabase: MarketDatabase) {
    private val dao = marketDatabase.globalMarketInfoDao()

    fun globalMarketInfo(currencyCode: String, timePeriod: HsTimePeriod): GlobalMarketInfo? {
        return dao.getGlobalMarketInfo(currencyCode, timePeriod)
    }

    fun save(globalMarketInfo: GlobalMarketInfo) {
        dao.insert(globalMarketInfo)
    }
}
