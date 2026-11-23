package com.example.fakenewsdetector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CheckEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkDao(): CheckDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        /** Keeps existing history when the result model gained structured fields. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE checks ADD COLUMN summary TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE checks ADD COLUMN reasons TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE checks ADD COLUMN sources TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE checks ADD COLUMN sourceApp TEXT")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fakenews.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
