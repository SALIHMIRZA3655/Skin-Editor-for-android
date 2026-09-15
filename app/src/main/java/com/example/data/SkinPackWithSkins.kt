package com.example.data

import androidx.room.Embedded
import androidx.room.Relation

data class SkinPackWithSkins(
    @Embedded val pack: SkinPackEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "packId"
    )
    val skins: List<SkinEntity>
)
