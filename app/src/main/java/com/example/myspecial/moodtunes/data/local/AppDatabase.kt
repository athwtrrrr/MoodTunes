package com.example.myspecial.moodtunes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myspecial.moodtunes.data.model.MoodLog

@Database(
    entities = [MoodLog::class],
    version = 3,  // Increment to 3 to be safe
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodLogDao(): MoodLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 2 to 3 (in case you need it)
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we're using exportSchema = false and in development,
                // we can let Room handle simple schema changes
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moodtunes_database"
                )
                    .fallbackToDestructiveMigration() // This will destroy and recreate on any schema conflict
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}