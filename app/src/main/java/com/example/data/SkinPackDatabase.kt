package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SkinPackEntity::class, SkinEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SkinPackDatabase : RoomDatabase() {
    abstract fun skinPackDao(): SkinPackDao

    companion object {
        @Volatile
        private var INSTANCE: SkinPackDatabase? = null

        fun getDatabase(context: Context): SkinPackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkinPackDatabase::class.java,
                    "mc_skin_pack_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
