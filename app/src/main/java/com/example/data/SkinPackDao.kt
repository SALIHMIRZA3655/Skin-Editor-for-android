package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinPackDao {
    @Query("SELECT * FROM skin_packs ORDER BY createdAt DESC")
    fun getAllPacks(): Flow<List<SkinPackEntity>>

    @Transaction
    @Query("SELECT * FROM skin_packs WHERE id = :packId")
    fun getPackWithSkins(packId: Long): Flow<SkinPackWithSkins?>

    @Transaction
    @Query("SELECT * FROM skin_packs ORDER BY createdAt DESC")
    fun getAllPacksWithSkins(): Flow<List<SkinPackWithSkins>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: SkinPackEntity): Long

    @Update
    suspend fun updatePack(pack: SkinPackEntity)

    @Delete
    suspend fun deletePack(pack: SkinPackEntity)

    @Query("SELECT * FROM skins WHERE packId = :packId ORDER BY createdAt ASC")
    fun getSkinsForPack(packId: Long): Flow<List<SkinEntity>>

    @Query("SELECT * FROM skins WHERE id = :skinId")
    suspend fun getSkinById(skinId: Long): SkinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkin(skin: SkinEntity): Long

    @Update
    suspend fun updateSkin(skin: SkinEntity)

    @Delete
    suspend fun deleteSkin(skin: SkinEntity)

    @Query("DELETE FROM skins WHERE id = :skinId")
    suspend fun deleteSkinById(skinId: Long)
}
