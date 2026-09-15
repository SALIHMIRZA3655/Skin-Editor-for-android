package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "skin_packs")
data class SkinPackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val version: String = "1.0.0",
    val manifestUuid: String = UUID.randomUUID().toString(),
    val moduleUuid: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
)
