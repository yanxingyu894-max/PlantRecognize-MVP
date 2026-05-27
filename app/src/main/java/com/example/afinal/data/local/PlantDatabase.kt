package com.example.afinal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 应用核心数据库类（RoomDatabase子类）
 * 作用：管理数据库的创建、版本升级、DAO接口获取等核心逻辑
 * 版本说明：
 * - 版本1：初始版本，仅包含基础植物和用户表
 * - 版本2：PlantEntity新增联合主键（slug, ownerId），支持多用户数据隔离
 */
@Database(
    entities = [PlantEntity::class, UserEntity::class], // 声明数据库包含的所有实体类
    version = 2, // 数据库版本号（版本变更需处理迁移）
    exportSchema = false // 关闭架构导出（生产环境建议关闭，避免泄露表结构）
)
abstract class PlantDatabase : RoomDatabase() {

    // 提供植物DAO接口的实例（Room自动实现）
    abstract fun plantDao(): PlantDao

    // 提供用户DAO接口的实例（Room自动实现）
    abstract fun userDao(): UserDao

    // 伴生对象：实现数据库单例模式，避免重复创建数据库连接
    companion object {
        // @Volatile：保证INSTANCE变量的可见性，多线程下不会读取到过期值
        @Volatile
        private var INSTANCE: PlantDatabase? = null

        /**
         * 获取数据库单例实例
         * @param context 上下文（建议用Application Context，避免内存泄漏）
         * @return 唯一的PlantDatabase实例
         */
        fun getDatabase(context: Context): PlantDatabase {
            // 双重校验锁：先判断INSTANCE是否为空，再加锁创建，保证线程安全且性能高
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it // 创建后赋值给INSTANCE，后续直接复用
                }
            }
        }

        /**
         * 构建数据库实例
         * @param context 上下文
         * @return 配置完成的PlantDatabase实例
         */
        private fun buildDatabase(context: Context): PlantDatabase {
            // 调试用代码（注释状态）：删除旧数据库，每次启动重建（开发阶段可用，生产需删除）
            //val dbName = "plant_database"
            //context.deleteDatabase(dbName)

            return Room.databaseBuilder(
                context.applicationContext, // 应用全局上下文，避免Activity销毁导致的内存泄漏
                PlantDatabase::class.java, // 数据库类的Class对象
                "plant_database" // 数据库文件名（存储在设备本地的文件名）
            )
                // 迁移策略：破坏性迁移（版本变更时删除旧数据库，重建新数据库）
                // 适用场景：开发阶段、数据无备份必要的场景；生产环境建议用addMigrations实现数据迁移
                .fallbackToDestructiveMigration()
                .build() // 构建并返回数据库实例
        }
    }
}