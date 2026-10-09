package com.example.nolalate

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlbumEntity::class, FiguritaEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun albumDao(): AlbumDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nolalate_database" // Este es el nombre del archivo real que se creará en el teléfono
                )
                    .fallbackToDestructiveMigration() // Si en el futuro cambias las tablas, esto evita que la app falle
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
