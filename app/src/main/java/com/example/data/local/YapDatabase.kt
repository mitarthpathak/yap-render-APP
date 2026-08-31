package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [YapProjectEntity::class],
    version = 1,
    exportSchema = false
)
abstract class YapDatabase : RoomDatabase() {
    abstract fun yapProjectDao(): YapProjectDao

    companion object {
        @Volatile
        private var INSTANCE: YapDatabase? = null

        fun getDatabase(context: Context): YapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YapDatabase::class.java,
                    "yap_render_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
