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
    version = 4,  // Increment to 4 for the new favorite field
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodLogDao(): MoodLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 3 to 4 to add is_favorite column
        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE mood_logs ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moodtunes_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}