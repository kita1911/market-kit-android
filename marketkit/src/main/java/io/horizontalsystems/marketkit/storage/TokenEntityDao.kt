package io.censystems.marketkit.storage

import androidx.room.*
import io.censystems.marketkit.models.*

@Dao
interface TokenEntityDao {

    @Query("SELECT * FROM TokenEntity")
    fun getAll(): List<TokenEntity>

}
