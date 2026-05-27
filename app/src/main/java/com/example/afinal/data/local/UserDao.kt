package com.example.afinal.data.local

import androidx.room.*

/**
 * 用户数据访问接口（DAO）
 * 作用：定义针对 "users" 表的所有数据库操作，Room框架自动生成实现类
 * 核心规则：所有方法均为挂起函数（suspend），必须在协程中调用，避免阻塞主线程（Android主线程不能做耗时操作）
 */
@Dao // Room注解，标记这是数据访问接口
interface UserDao {
    /**
     * 插入单个用户数据（注册时使用）
     * @param user 要插入的用户实体
     * 冲突策略：ABORT（如果用户名已存在，终止插入并抛出异常）
     * 设计目的：保证用户名唯一性，避免重复注册
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    /**
     * 根据用户名查询用户信息（登录时使用）
     * @param username 要查询的用户名
     * @return 匹配的用户实体（包含密码哈希、注册时间等），无匹配则返回null
     * 场景：登录验证（查询后对比密码哈希）、获取用户基础信息
     */
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): UserEntity?

    /**
     * 更新用户信息
     * @param user 要更新的用户实体（需包含用户名主键）
     * 场景：修改用户服务条款同意状态、更新密码哈希（改密码时）
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * 删除用户数据
     * @param user 要删除的用户实体（需包含用户名主键）
     * 场景：用户注销账号、删除测试账号
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)
}