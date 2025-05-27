package io.censystems.marketkit.managers

import android.util.Log
import io.censystems.marketkit.models.Blockchain
import io.censystems.marketkit.models.BlockchainType
import io.censystems.marketkit.models.Coin
import io.censystems.marketkit.models.DefiMarketInfo
import io.censystems.marketkit.models.DefiMarketInfoResponse
import io.censystems.marketkit.models.FullCoin
import io.censystems.marketkit.models.MarketInfo
import io.censystems.marketkit.models.MarketInfoRaw
import io.censystems.marketkit.models.Token
import io.censystems.marketkit.models.TokenQuery
import io.censystems.marketkit.storage.CoinStorage

class CoinManager(
    private val storage: CoinStorage,
) {

    fun coin(coinUid: String) = storage.coin(coinUid)
    fun coins(coinUids: List<String>) = storage.coins(coinUids)

    fun fullCoin(uid: String): FullCoin? =
        storage.fullCoin(uid)

    fun fullCoins(filter: String, limit: Int): List<FullCoin> =
        storage.fullCoins(filter, limit)

    fun fullCoins(coinUids: List<String>): List<FullCoin> =
        storage.fullCoins(coinUids)

    fun allCoins(): List<Coin> = storage.allCoins()

    fun token(query: TokenQuery): Token? =
        storage.getToken(query)

    fun tokens(queries: List<TokenQuery>): List<Token> =
        storage.getTokens(queries)

    fun tokens(reference: String): List<Token> =
        storage.getTokens(reference)

    fun tokens(blockchainType: BlockchainType, filter: String, limit: Int): List<Token> =
        storage.getTokens(blockchainType, filter, limit)

    fun blockchain(uid: String): Blockchain? =
        storage.getBlockchain(uid)

    fun blockchains(uids: List<String>): List<Blockchain> =
        storage.getBlockchains(uids)

    fun allBlockchains(): List<Blockchain> =
        storage.getAllBlockchains()

    fun getMarketInfos(rawMarketInfos: List<MarketInfoRaw>): List<MarketInfo> {
        return buildList {
            rawMarketInfos.chunked(700).forEach { chunkedRawMarketInfos ->
                try {
                    val fullCoins = storage.fullCoins(chunkedRawMarketInfos.map { it.uid })
                    val hashMap = fullCoins.associateBy { it.coin.uid }

                    addAll(
                        chunkedRawMarketInfos.mapNotNull { rawMarketInfo ->
                            val fullCoin = hashMap[rawMarketInfo.uid] ?: return@mapNotNull null
                            MarketInfo(rawMarketInfo, fullCoin)
                        }
                    )
                } catch (e: Exception) {
                    Log.e("CoinManager", "getMarketInfos: ", e)
                }
            }
        }
    }

    fun getDefiMarketInfos(rawDefiMarketInfos: List<DefiMarketInfoResponse>): List<DefiMarketInfo> {
        val fullCoins = storage.fullCoins(rawDefiMarketInfos.mapNotNull { it.uid })
        val hashMap = fullCoins.map { it.coin.uid to it }.toMap()

        return rawDefiMarketInfos.map { rawDefiMarketInfo ->
            val fullCoin = hashMap[rawDefiMarketInfo.uid]
            DefiMarketInfo(rawDefiMarketInfo, fullCoin)
        }
    }

}
