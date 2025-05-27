package io.censystems.marketkit.managers

import io.censystems.marketkit.models.Post
import io.censystems.marketkit.providers.CryptoCompareProvider
import io.reactivex.Single

class PostManager(
    private val provider: CryptoCompareProvider
) {
    fun postsSingle(): Single<List<Post>> {
        return provider.postsSingle()
    }
}
