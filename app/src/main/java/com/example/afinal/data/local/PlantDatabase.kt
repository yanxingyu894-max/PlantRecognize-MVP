package com.example.afinal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room数据库核心类，是整个应用本地数据持久化的唯一入口
 * 负责管理数据库连接、版本控制、DAO实例提供，采用单例模式保证全局唯一实例
 *
 * Room是Google推荐的ORM框架，封装了SQLite，提供编译期SQL语法检查、协程支持等特性
 */
// 标记为Room数据库，核心注解说明：
// entities：指定数据库包含的所有实体类（对应数据库表），支持多表数组形式
// version：数据库版本号（整数），升级/降级时必须递增，配合Migration使用
// exportSchema：是否导出数据库架构文件（.json），开发阶段可开启，生产环境关闭简化结构
@Database(
    entities = [PlantEntity::class, UserEntity::class], // 关联植物表、用户表
    version = 2, // 版本升级说明：v1→v2 新增了用户表字段/植物表收藏状态字段
    exportSchema = false  // 关闭schema导出，避免版本管理时的额外文件维护
)
abstract class PlantDatabase : RoomDatabase() {

    /**
     * 获取植物表的DAO（数据访问对象）实例
     * Room会在编译期自动生成PlantDao的实现类（PlantDao_Impl）
     * 调用方式：PlantDatabase.getInstance(context).plantDao()
     * @return PlantDao 可执行植物表的增删改查操作
     */
    abstract fun plantDao(): PlantDao

    /**
     * 获取用户表的DAO（数据访问对象）实例
     * Room自动生成UserDao_Impl实现类
     * 调用方式：PlantDatabase.getInstance(context).userDao()
     * @return UserDao 可执行用户表的增删改查操作
     */
    abstract fun userDao(): UserDao

    /**
     * 伴生对象：提供数据库单例创建逻辑，静态作用域
     * 单例设计：避免多线程下创建多个数据库实例导致的连接冲突/数据不一致
     */
    companion object {
        // @Volatile注解：保证INSTANCE的内存可见性
        // 多线程场景下，一个线程修改INSTANCE后，其他线程能立即看到最新值，避免指令重排导致的空指针
        @Volatile
        private var INSTANCE: PlantDatabase? = null

        /**
         * 全局获取数据库单例的核心方法
         * 采用「双重检查锁（DCL）」设计，兼顾性能和线程安全
         * 快速路径（无锁）：实例已存在时直接返回，避免每次调用都加锁
         * 同步块（加锁）：实例未创建时，仅允许一个线程创建实例
         *
         * @param context 上下文（建议传入Application Context，避免Activity生命周期泄漏）
         * @return PlantDatabase 全局唯一的数据库实例
         * 调用示例：val db = PlantDatabase.getDatabase(applicationContext)
         */
        fun getDatabase(context: Context): PlantDatabase {
            // 第一次检查：无锁，提升性能
            return INSTANCE ?: synchronized(this) {
                // 第二次检查：进入同步块后再次确认，防止多线程并发创建
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it // 将创建好的实例赋值给全局变量
                }
            }
        }

        /**
         * 私有构建数据库的方法，封装Room数据库构建逻辑
         * 仅在getDatabase的同步块中调用，外部不可访问
         *
         * @param context 应用上下文（必须用applicationContext，避免内存泄漏）
         * @return PlantDatabase 构建完成的数据库实例
         */
        private fun buildDatabase(context: Context): PlantDatabase {
            return Room.databaseBuilder(
                // 上下文：使用应用全局上下文，避免Activity销毁导致的上下文失效
                context.applicationContext,
                // 数据库实现类：当前PlantDatabase的Class对象
                PlantDatabase::class.java,
                // 数据库文件名：存储在/data/data/com.example.afinal/databases/plant_database
                "plant_database"
            )
                // 迁移策略：开发阶段使用破坏性迁移（版本升级时删除旧库重建）
                // 生产环境需替换为Migration类，避免数据丢失！
                // 注意：该策略会清空所有数据，仅适用于开发/测试阶段
                .fallbackToDestructiveMigration()
                .build() // 构建数据库实例
        }
    }
}