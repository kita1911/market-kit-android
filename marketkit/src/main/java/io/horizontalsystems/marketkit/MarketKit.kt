package io.censystems.marketkit

import android.content.Context
import android.os.storage.StorageManager
import io.censystems.marketkit.chart.HsChartRequestHelper
import io.censystems.marketkit.managers.CoinHistoricalPriceManager
import io.censystems.marketkit.managers.CoinManager
import io.censystems.marketkit.managers.CoinPriceManager
import io.censystems.marketkit.managers.CoinPriceSyncManager
import io.censystems.marketkit.managers.DumpManager
import io.censystems.marketkit.managers.GlobalMarketInfoManager
import io.censystems.marketkit.managers.MarketOverviewManager
import io.censystems.marketkit.managers.NftManager
import io.censystems.marketkit.managers.PostManager
import io.censystems.marketkit.models.Analytics
import io.censystems.marketkit.models.AnalyticsPreview
import io.censystems.marketkit.models.Blockchain
import io.censystems.marketkit.models.BlockchainType
import io.censystems.marketkit.models.Category
import io.censystems.marketkit.models.ChartPoint
import io.censystems.marketkit.models.Coin
import io.censystems.marketkit.models.CoinCategory
import io.censystems.marketkit.models.CoinInvestment
import io.censystems.marketkit.models.CoinPrice
import io.censystems.marketkit.models.CoinReport
import io.censystems.marketkit.models.CoinTreasury
import io.censystems.marketkit.models.DefiMarketInfo
import io.censystems.marketkit.models.Etf
import io.censystems.marketkit.models.EtfPoint
import io.censystems.marketkit.models.EtfPointResponse
import io.censystems.marketkit.models.EtfResponse
import io.censystems.marketkit.models.FullCoin
import io.censystems.marketkit.models.GlobalMarketPoint
import io.censystems.marketkit.models.HsPeriodType
import io.censystems.marketkit.models.HsPointTimePeriod
import io.censystems.marketkit.models.HsTimePeriod
import io.censystems.marketkit.models.MarketGlobal
import io.censystems.marketkit.models.MarketInfo
import io.censystems.marketkit.models.MarketInfoOverview
import io.censystems.marketkit.models.MarketOverview
import io.censystems.marketkit.models.MarketTicker
import io.censystems.marketkit.models.NftTopCollection
import io.censystems.marketkit.models.Post
import io.censystems.marketkit.models.RankMultiValue
import io.censystems.marketkit.models.RankValue
import io.censystems.marketkit.models.SubscriptionResponse
import io.censystems.marketkit.models.Token
import io.censystems.marketkit.models.TokenHolders
import io.censystems.marketkit.models.TokenQuery
import io.censystems.marketkit.models.TopMovers
import io.censystems.marketkit.models.TopPair
import io.censystems.marketkit.models.TopPlatform
import io.censystems.marketkit.models.TopPlatformMarketCapPoint
import io.censystems.marketkit.providers.CoinPriceSchedulerFactory
import io.censystems.marketkit.providers.CryptoCompareProvider
import io.censystems.marketkit.providers.HsNftProvider
import io.censystems.marketkit.providers.HsProvider
import io.censystems.marketkit.storage.CoinHistoricalPriceStorage
import io.censystems.marketkit.storage.CoinPriceStorage
import io.censystems.marketkit.storage.CoinStorage
import io.censystems.marketkit.storage.GlobalMarketInfoStorage
import io.censystems.marketkit.storage.MarketDatabase
import io.censystems.marketkit.syncers.CoinSyncer
import io.censystems.marketkit.syncers.HsDataSyncer
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Response
import java.math.BigDecimal
import java.util.Date

class MarketKit(
    private val nftManager: NftManager,
    private val marketOverviewManager: MarketOverviewManager,
    private val coinManager: CoinManager,
    private val coinSyncer: CoinSyncer,
    private val coinPriceManager: CoinPriceManager,
    private val coinHistoricalPriceManager: CoinHistoricalPriceManager,
    private val coinPriceSyncManager: CoinPriceSyncManager,
    private val postManager: PostManager,
    private val globalMarketInfoManager: GlobalMarketInfoManager,
    private val hsProvider: HsProvider,
    private val hsDataSyncer: HsDataSyncer,
    private val dumpManager: DumpManager,
) {
    private val coinsMap by lazy { coinManager.allCoins().associateBy { it.uid } }

    // Coins

    val fullCoinsUpdatedObservable: Observable<Unit>
        get() = coinSyncer.fullCoinsUpdatedObservable

    fun fullCoins(filter: String, limit: Int = 20): List<FullCoin> {
        return coinManager.fullCoins(filter, limit)
    }

    fun fullCoins(coinUids: List<String>): List<FullCoin> {
        return coinManager.fullCoins(coinUids)
    }

    fun allCoins(): List<Coin> = coinManager.allCoins()

    fun token(query: TokenQuery): Token? =
        coinManager.token(query)

    fun tokens(queries: List<TokenQuery>): List<Token> =
        coinManager.tokens(queries)

    fun tokens(reference: String): List<Token> =
        coinManager.tokens(reference)

    fun tokens(blockchainType: BlockchainType, filter: String, limit: Int = 20): List<Token> =
        coinManager.tokens(blockchainType, filter, limit)

    fun blockchains(uids: List<String>): List<Blockchain> =
        coinManager.blockchains(uids)

    fun allBlockchains(): List<Blockchain> =
        coinManager.allBlockchains()

    fun blockchain(uid: String): Blockchain? =
        coinManager.blockchain(uid)

    fun marketInfosSingle(
        top: Int,
        currencyCode: String,
        defi: Boolean,
    ): Single<List<MarketInfo>> {
        return hsProvider.marketInfosSingle(top, currencyCode, defi).map {
            coinManager.getMarketInfos(it)
        }
    }

    fun topCoinsMarketInfosSingle(top: Int, currencyCode: String): Single<List<MarketInfo>> {
        return hsProvider.topCoinsMarketInfosSingle(top, currencyCode).map {
            coinManager.getMarketInfos(it)
        }
    }

    fun advancedMarketInfosSingle(
        top: Int = 250,
        currencyCode: String,
    ): Single<List<MarketInfo>> {
        return hsProvider.advancedMarketInfosSingle(top, currencyCode).map {
            coinManager.getMarketInfos(it)
        }
    }

    fun marketInfosSingle(
        coinUids: List<String>,
        currencyCode: String,
    ): Single<List<MarketInfo>> {
        return hsProvider.marketInfosSingle(coinUids, currencyCode).map {
            coinManager.getMarketInfos(it)
        }
    }

    fun categoriesSingle(): Single<List<Category>> {
        return hsProvider.categoriesSingle()
    }

    fun marketInfosSingle(
        categoryUid: String,
        currencyCode: String,
    ): Single<List<MarketInfo>> {
        return hsProvider.marketInfosSingle(categoryUid, currencyCode).map {
            coinManager.getMarketInfos(it)
        }
    }

    fun marketInfoOverviewSingle(
        coinUid: String,
        currencyCode: String,
        language: String,
    ): Single<MarketInfoOverview> {
        return hsProvider.getMarketInfoOverview(
            coinUid = coinUid,
            currencyCode = currencyCode,
            language = language,
        ).map { rawOverview ->
            val fullCoin = coinManager.fullCoin(coinUid) ?: throw Exception("No Full Coin")

            rawOverview.marketInfoOverview(fullCoin)
        }
    }

    fun marketInfoTvlSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<ChartPoint>> {
        return hsProvider.marketInfoTvlSingle(coinUid, currencyCode, timePeriod)
    }

    fun marketInfoGlobalTvlSingle(
        chain: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<ChartPoint>> {
        return hsProvider.marketInfoGlobalTvlSingle(chain, currencyCode, timePeriod)
    }

    fun defiMarketInfosSingle(currencyCode: String): Single<List<DefiMarketInfo>> {
        return hsProvider.defiMarketInfosSingle(currencyCode).map {
            coinManager.getDefiMarketInfos(it)
        }
    }

    //Signals

    fun coinsSignalsSingle(coinsUids: List<String>): Single<Map<String, Analytics.TechnicalAdvice.Advice>> {
        return hsProvider.coinsSignalsSingle(coinsUids).map { list ->
            list.mapNotNull { coinSignal ->
                if (coinSignal.signal == null) null
                else coinSignal.uid to coinSignal.signal
            }.toMap()
        }
    }


    // Categories

    fun coinCategoriesSingle(currencyCode: String): Single<List<CoinCategory>> =
        hsProvider.getCoinCategories(currencyCode)

    fun coinCategoryMarketPointsSingle(
        categoryUid: String,
        interval: HsTimePeriod,
        currencyCode: String
    ) =
        hsProvider.coinCategoryMarketPointsSingle(categoryUid, interval, currencyCode)

    fun sync() {
        hsDataSyncer.sync()
    }

    // Coin Prices

    fun refreshCoinPrices(currencyCode: String) {
        coinPriceSyncManager.refresh(currencyCode)
    }

    fun coinPrice(coinUid: String, currencyCode: String): CoinPrice? {
        return coinPriceManager.coinPrice(coinUid, currencyCode)
    }

    fun coinPriceMap(coinUids: List<String>, currencyCode: String): Map<String, CoinPrice> {
        return coinPriceManager.coinPriceMap(coinUids, currencyCode)
    }

    fun coinPriceObservable(
        tag: String,
        coinUid: String,
        currencyCode: String
    ): Observable<CoinPrice> {
        return coinPriceSyncManager.coinPriceObservable(tag, coinUid, currencyCode)
    }

    fun coinPriceMapObservable(
        tag: String,
        coinUids: List<String>,
        currencyCode: String
    ): Observable<Map<String, CoinPrice>> {
        return coinPriceSyncManager.coinPriceMapObservable(tag, coinUids, currencyCode)
    }

    // Coin Historical Price

    fun coinHistoricalPriceSingle(
        coinUid: String,
        currencyCode: String,
        timestamp: Long
    ): Single<BigDecimal> {
        return coinHistoricalPriceManager.coinHistoricalPriceSingle(
            coinUid,
            currencyCode,
            timestamp
        )
    }

    fun coinHistoricalPrice(coinUid: String, currencyCode: String, timestamp: Long): BigDecimal? {
        return coinHistoricalPriceManager.coinHistoricalPrice(coinUid, currencyCode, timestamp)
    }

    // Posts

    fun postsSingle(): Single<List<Post>> {
        return postManager.postsSingle()
    }

    // Market Tickers

    fun marketTickersSingle(coinUid: String, currencyCode: String): Single<List<MarketTicker>> {
        return hsProvider.marketTickers(coinUid, currencyCode)
    }

    // Details

    fun tokenHoldersSingle(
        authToken: String,
        coinUid: String,
        blockchainUid: String
    ): Single<TokenHolders> {
        return hsProvider.tokenHoldersSingle(authToken, coinUid, blockchainUid)
    }

    fun treasuriesSingle(coinUid: String, currencyCode: String): Single<List<CoinTreasury>> {
        return hsProvider.coinTreasuriesSingle(coinUid, currencyCode)
    }

    fun investmentsSingle(coinUid: String): Single<List<CoinInvestment>> {
        return hsProvider.investmentsSingle(coinUid)
    }

    fun coinReportsSingle(coinUid: String): Single<List<CoinReport>> {
        return hsProvider.coinReportsSingle(coinUid)
    }

    // Pro Data

    fun cexVolumesSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<ChartPoint>> {
        val periodType = HsPeriodType.ByPeriod(timePeriod)
        val currentTime = Date().time / 1000
        val fromTimestamp = HsChartRequestHelper.fromTimestamp(currentTime, periodType)
        val interval = HsPointTimePeriod.Day1
        return hsProvider.coinPriceChartSingle(coinUid, currencyCode, interval, fromTimestamp)
            .map { response ->
                response.mapNotNull { chartCoinPrice ->
                    chartCoinPrice.totalVolume?.let { volume ->
                        ChartPoint(volume, chartCoinPrice.timestamp, null)
                    }
                }
            }
    }

    fun dexLiquiditySingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<Analytics.VolumePoint>> {
        return hsProvider.dexLiquiditySingle(authToken, coinUid, currencyCode, timePeriod)
    }

    fun dexVolumesSingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<Analytics.VolumePoint>> {
        return hsProvider.dexVolumesSingle(authToken, coinUid, currencyCode, timePeriod)
    }

    fun transactionDataSingle(
        authToken: String,
        coinUid: String,
        timePeriod: HsTimePeriod,
        platform: String?
    ): Single<List<Analytics.CountVolumePoint>> {
        return hsProvider.transactionDataSingle(authToken, coinUid, timePeriod, platform)
    }

    fun activeAddressesSingle(
        authToken: String,
        coinUid: String,
        timePeriod: HsTimePeriod
    ): Single<List<Analytics.CountPoint>> {
        return hsProvider.activeAddressesSingle(authToken, coinUid, timePeriod)
    }

    fun analyticsPreviewSingle(
        coinUid: String,
        addresses: List<String>,
    ): Single<AnalyticsPreview> {
        return hsProvider.analyticsPreviewSingle(coinUid, addresses)
    }

    fun analyticsSingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
    ): Single<Analytics> {
        return hsProvider.analyticsSingle(authToken, coinUid, currencyCode)
    }

    fun cexVolumeRanksSingle(
        authToken: String,
        currencyCode: String
    ): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "cex_volume", currencyCode)
    }

    fun dexVolumeRanksSingle(
        authToken: String,
        currencyCode: String
    ): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "dex_volume", currencyCode)
    }

    fun dexLiquidityRanksSingle(authToken: String, currencyCode: String): Single<List<RankValue>> {
        return hsProvider.rankValueSingle(authToken, "dex_liquidity", currencyCode)
    }

    fun activeAddressRanksSingle(
        authToken: String,
        currencyCode: String
    ): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "address", currencyCode)
    }

    fun transactionCountsRanksSingle(
        authToken: String,
        currencyCode: String
    ): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "tx_count", currencyCode)
    }

    fun holderRanksSingle(authToken: String, currencyCode: String): Single<List<RankValue>> {
        return hsProvider.rankValueSingle(authToken, "holders", currencyCode)
    }

    fun revenueRanksSingle(authToken: String, currencyCode: String): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "revenue", currencyCode)
    }

    fun feeRanksSingle(authToken: String, currencyCode: String): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "fee", currencyCode)
    }

    fun subscriptionsSingle(addresses: List<String>): Single<List<SubscriptionResponse>> {
        return hsProvider.subscriptionsSingle(addresses)
    }

    // Overview
    fun marketOverviewSingle(currencyCode: String): Single<MarketOverview> =
        marketOverviewManager.marketOverviewSingle(currencyCode).map { marketOverview ->
            marketOverview.copy(
                topPairs = marketOverview.topPairs.map { topPairWithCoin(it) }
            )
        }

    private fun topPairWithCoin(topPair: TopPair) =
        topPair.copy(
            baseCoin = coinsMap[topPair.baseCoinUid],
            targetCoin = coinsMap[topPair.targetCoinUid]
        )

    fun marketGlobalSingle(currencyCode: String): Single<MarketGlobal> =
        hsProvider.marketGlobalSingle(currencyCode)

    fun topPairsSingle(currencyCode: String, page: Int, limit: Int): Single<List<TopPair>> =
        hsProvider.topPairsSingle(currencyCode, page, limit).map { topPairs ->
            topPairs.map { topPairWithCoin(it) }
        }


    fun topMoversSingle(currencyCode: String): Single<TopMovers> =
        hsProvider.topMoversRawSingle(currencyCode)
            .map { raw ->
                TopMovers(
                    gainers100 = coinManager.getMarketInfos(raw.gainers100),
                    gainers200 = coinManager.getMarketInfos(raw.gainers200),
                    gainers300 = coinManager.getMarketInfos(raw.gainers300),
                    losers100 = coinManager.getMarketInfos(raw.losers100),
                    losers200 = coinManager.getMarketInfos(raw.losers200),
                    losers300 = coinManager.getMarketInfos(raw.losers300)
                )
            }

    // Chart Info

    fun chartPointsSingle(
        coinUid: String,
        currencyCode: String,
        interval: HsPointTimePeriod,
        pointCount: Int
    ): Single<List<ChartPoint>> {
        val fromTimestamp = Date().time / 1000 - interval.interval * pointCount

        return hsProvider.coinPriceChartSingle(coinUid, currencyCode, interval, fromTimestamp)
            .map { response ->
                response.map { chartCoinPrice ->
                    chartCoinPrice.chartPoint
                }
            }
    }

    fun chartPointsSingle(
        coinUid: String,
        currencyCode: String,
        periodType: HsPeriodType
    ): Single<Pair<Long, List<ChartPoint>>> {
        val data = intervalData(periodType)
        return hsProvider.coinPriceChartSingle(
            coinUid,
            currencyCode,
            data.interval,
            data.fromTimestamp
        )
            .map {
                Pair(data.visibleTimestamp, it.map { it.chartPoint })
            }
    }

    private fun intervalData(periodType: HsPeriodType): IntervalData {
        val interval = HsChartRequestHelper.pointInterval(periodType)
        val visibleTimestamp: Long
        val fromTimestamp: Long?
        when (periodType) {
            is HsPeriodType.ByPeriod -> {
                val currentTime = Date().time / 1000
                visibleTimestamp = HsChartRequestHelper.fromTimestamp(currentTime, periodType)
                fromTimestamp = visibleTimestamp
            }

            is HsPeriodType.ByCustomPoints -> {
                val currentTime = Date().time / 1000
                visibleTimestamp = HsChartRequestHelper.fromTimestamp(currentTime, periodType)
                val customPointsInterval = interval.interval * periodType.pointsCount
                fromTimestamp = visibleTimestamp - customPointsInterval
            }

            is HsPeriodType.ByStartTime -> {
                visibleTimestamp = periodType.startTime
                fromTimestamp = null
            }
        }

        return IntervalData(interval, fromTimestamp, visibleTimestamp)
    }

    fun chartStartTimeSingle(coinUid: String): Single<Long> {
        return hsProvider.coinPriceChartStartTime(coinUid)
    }

    fun topPlatformMarketCapStartTimeSingle(platform: String): Single<Long> {
        return hsProvider.topPlatformMarketCapStartTime(platform)
    }

    // Global Market Info

    fun globalMarketPointsSingle(
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<GlobalMarketPoint>> {
        return globalMarketInfoManager.globalMarketInfoSingle(currencyCode, timePeriod)
    }

    fun topPlatformsSingle(currencyCode: String): Single<List<TopPlatform>> {
        return hsProvider.topPlatformsSingle(currencyCode)
            .map { responseList -> responseList.map { it.topPlatform } }
    }

    fun topPlatformMarketCapPointsSingle(
        chain: String,
        currencyCode: String,
        periodType: HsPeriodType
    ): Single<List<TopPlatformMarketCapPoint>> {
        val data = intervalData(periodType)
        return hsProvider.topPlatformMarketCapPointsSingle(
            chain,
            currencyCode,
            data.interval,
            data.fromTimestamp
        )
    }

    fun topPlatformMarketInfosSingle(
        chain: String,
        currencyCode: String,
    ): Single<List<MarketInfo>> {
        return hsProvider.topPlatformCoinListSingle(chain, currencyCode)
            .map { coinManager.getMarketInfos(it) }
    }

    // NFT

    suspend fun nftTopCollections(): List<NftTopCollection> = nftManager.topCollections()

    // Auth

    fun authGetSignMessage(address: String): Single<String> {
        return hsProvider.authGetSignMessage(address)
    }

    fun authenticate(signature: String, address: String): Single<String> {
        return hsProvider.authenticate(signature, address)
    }

    fun requestPersonalSupport(authToken: String, username: String): Single<Response<Void>> {
        return hsProvider.requestPersonalSupport(authToken, username)
    }

    fun requestVipSupport(authToken: String, subscriptionId: String): Single<Map<String, String>> {
        return hsProvider.requestVipSupport(authToken, subscriptionId)
    }

    //Misc

    fun syncInfo(): SyncInfo {
        return coinSyncer.syncInfo()
    }

    fun getInitialDump(): String {
        return dumpManager.getInitialDump()
    }

    //ETF

    fun etfSingle(currencyCode: String): Single<List<Etf>> {
        return hsProvider.etfsSingle(currencyCode)
            .map { items ->
                items.map { EtfResponse.toEtf(it) }
            }
    }

    fun etfPointSingle(currencyCode: String): Single<List<EtfPoint>> {
        return hsProvider.etfPointsSingle(currencyCode)
            .map { points ->
                points.mapNotNull { EtfPointResponse.toEtfPoint(it) }
            }
    }

    //Stats

    fun sendStats(statsJson: String, appVersion: String, appId: String?): Single<Unit> {
        return hsProvider.sendStats(statsJson, appVersion, appId)
    }

    companion object {
        fun getInstance(
            context: Context,
            hsApiBaseUrl: String,
            hsApiKey: String,
        ): MarketKit {
            // init cache
            (context.getSystemService(Context.STORAGE_SERVICE) as StorageManager?)?.let { storageManager ->
                val cacheDir = context.cacheDir
                val cacheQuotaBytes =
                    storageManager.getCacheQuotaBytes(storageManager.getUuidForPath(cacheDir))

                HSCache.cacheDir = cacheDir
                HSCache.cacheQuotaBytes = cacheQuotaBytes
            }

            val marketDatabase = MarketDatabase.getInstance(context)
            val dumpManager = DumpManager(marketDatabase)
            val hsProvider = HsProvider(hsApiBaseUrl, hsApiKey)
            val hsNftProvider = HsNftProvider(hsApiBaseUrl, hsApiKey)
            val coinStorage = CoinStorage(marketDatabase)
            val coinManager = CoinManager(coinStorage)
            val nftManager = NftManager(coinManager, hsNftProvider)
            val marketOverviewManager = MarketOverviewManager(nftManager, hsProvider)
            val coinSyncer = CoinSyncer(hsProvider, coinStorage, marketDatabase.syncerStateDao())
            val coinPriceManager = CoinPriceManager(CoinPriceStorage(marketDatabase))
            val coinHistoricalPriceManager = CoinHistoricalPriceManager(
                CoinHistoricalPriceStorage(marketDatabase),
                hsProvider,
            )
            val coinPriceSchedulerFactory = CoinPriceSchedulerFactory(coinPriceManager, hsProvider)
            val coinPriceSyncManager = CoinPriceSyncManager(coinPriceSchedulerFactory)
            coinPriceManager.listener = coinPriceSyncManager
            val cryptoCompareProvider = CryptoCompareProvider()
            val postManager = PostManager(cryptoCompareProvider)
            val globalMarketInfoStorage = GlobalMarketInfoStorage(marketDatabase)
            val globalMarketInfoManager =
                GlobalMarketInfoManager(hsProvider, globalMarketInfoStorage)
            val hsDataSyncer = HsDataSyncer(coinSyncer, hsProvider)

            return MarketKit(
                nftManager,
                marketOverviewManager,
                coinManager,
                coinSyncer,
                coinPriceManager,
                coinHistoricalPriceManager,
                coinPriceSyncManager,
                postManager,
                globalMarketInfoManager,
                hsProvider,
                hsDataSyncer,
                dumpManager,
            )
        }
    }

}

//Errors

sealed class ProviderError : Exception() {
    class ApiRequestLimitExceeded : ProviderError()
    class NoDataForCoin : ProviderError()
    class ReturnedTimestampIsVeryInaccurate : ProviderError()
}

class SyncInfo(
    val coinsTimestamp: String?,
    val blockchainsTimestamp: String?,
    val tokensTimestamp: String?
)
