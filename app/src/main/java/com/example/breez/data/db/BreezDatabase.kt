package com.example.breez.data.db
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.breez.data.db.dao.FavoriteDao
import com.example.breez.data.db.dao.AlertDao
import com.example.breez.data.db.dao.WeatherCacheDao
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.WeatherCacheEntity
import com.example.breez.data.db.converter.DateConverter

@Database(
    entities = [FavoriteEntity::class, AlertEntity::class, WeatherCacheEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class BreezDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun alertDao(): AlertDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        const val DATABASE_NAME = "breez_database"

        fun create(context: Context): BreezDatabase {
            return Room.databaseBuilder(
                context,
                BreezDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}