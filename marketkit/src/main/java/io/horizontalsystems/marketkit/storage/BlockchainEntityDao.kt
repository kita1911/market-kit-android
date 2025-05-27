package io.censystems.marketkit.storage

import androidx.room.*
import io.censystems.marketkit.models.*

@Dao
interface BlockchainEntityDao {

    @Query("SELECT * FROM BlockchainEntity")
    fun getAll(): List<BlockchainEntity>

}
