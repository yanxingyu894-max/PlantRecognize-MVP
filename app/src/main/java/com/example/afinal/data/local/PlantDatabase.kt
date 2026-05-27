package com.example.afinal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 核心数据库类
 * 版本 2 修改点：PlantEntity 引入联合主键 (slug, ownerId) 以支持多用户数据隔离
 */
@Database(
    entities = [PlantEntity::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PlantDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: PlantDatabase? = null

        fun getDatabase(context: Context): PlantDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
            }
        }

        private fun buildDatabase(context: Context): PlantDatabase {
            //val dbName = "plant_database"
            //context.deleteDatabase(dbName)

            return Room.databaseBuilder(
                context.applicationContext,
                PlantDatabase::class.java,
                "plant_database"
            )
                .fallbackToDestructiveMigration() // 主键变更需清理旧数据并重建
                .build()
        }
    }
}
