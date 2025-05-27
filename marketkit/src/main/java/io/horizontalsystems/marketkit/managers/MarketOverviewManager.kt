package io.censystems.marketkit.managers

import io.censystems.marketkit.models.HsTimePeriod
import io.censystems.marketkit.models.MarketOverview
import io.censystems.marketkit.models.MarketOverviewResponse
import io.censystems.marketkit.providers.HsProvider
import io.reactivex.Single

class MarketOverviewManager(
    private val nftManager: NftManager,
    private val hsProvider: HsProvider
) {

    private fun marketOverview(response: MarketOverviewResponse): MarketOverview =
        MarketOverview(
            globalMarketPoints = response.globalMarketPoints,
            coinCategories = response.coinCategories,
            topPlatforms = response.topPlatforms.map { it.topPlatform },
            nftCollections = mapOf(
                HsTimePeriod.Day1 to nftManager.topCollections(response.nft.one_day),
                HsTimePeriod.Week1 to nftManager.topCollections(response.nft.seven_day),
                HsTimePeriod.Month1 to nftManager.topCollections(response.nft.thirty_day)
            ),
            topPairs = response.pairs
        )

    fun marketOverviewSingle(currencyCode: String): Single<MarketOverview> =
        hsProvider.marketOverviewSingle(currencyCode).map { marketOverview(it) }

}
