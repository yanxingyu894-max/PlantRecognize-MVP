package com.example.afinal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room数据库类 —— 整个应用的数据库入口
 * @Database 标记这是一个Room数据库
 * entities = 包含的表（数组形式）
 * version = 数据库版本号，升级时需要递增
 */
@Database(
    entities = [PlantEntity::class],
    version = 1,
    exportSchema = false  // 不导出schema文件，简化项目结构
)
abstract class PlantDatabase : RoomDatabase() {

    /**
     * 获取DAO实例 —— Room会自动生成实现类
     */
    abstract fun plantDao(): PlantDao

    companion object {
        // @Volatile 确保多线程可见性，一个线程修改后其他线程立即可见
        @Volatile
        private var INSTANCE: PlantDatabase? = null

        /**
         * 单例模式 —— 整个应用只有一个数据库实例
         * synchronized(this) 防止多线程同时创建多个实例
         */
        fun getDatabase(context: Context): PlantDatabase {
            // 如果实例已存在，直接返回（快速路径，无锁）
            return INSTANCE ?: synchronized(this) {
                // 双重检查：进入同步块后再检查一次
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
            }
        }

        private fun buildDatabase(context: Context): PlantDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PlantDatabase::class.java,
                "plant_database"  // 数据库文件名
            )
                // .fallbackToDestructiveMigration() // 开发时使用：版本升级销毁重建（会丢失数据！）
                .build()
        }
    }
}