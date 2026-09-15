package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "skins",
    foreignKeys = [
        ForeignKey(
            entity = SkinPackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packId")]
)
data class SkinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packId: Long,
    val name: String,
    val isSlim: Boolean = false, // false = Classic (4px), true = Slim (3px)
    val textureBase64: String, // 64x64 PNG encoded as Base64
    val createdAt: Long = System.currentTimeMillis()
)
