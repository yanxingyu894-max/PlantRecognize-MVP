package com.example.afinal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体类（Entity）
 * 作用：映射数据库中的 "users" 表，存储用户的登录凭据和基础信息
 * 设计原则：仅存储必要的本地数据，敏感信息（密码）仅存哈希值，保障用户隐私
 */
@Entity(tableName = "users") // Room注解，标记为数据库实体，指定对应表名：users
data class UserEntity(
    // 主键：用户名（唯一标识每个用户，确保无重复账号）
    // 特性：val类型不可修改，保证用户名一旦创建就无法变更
    @PrimaryKey val username: String,

    // 密码哈希值（核心安全字段）
    // 说明：不存储明文密码，仅存储SHA-256/MD5等算法加密后的字符串
    // 安全建议：需配合盐值（salt）使用，防止彩虹表攻击（一种破解哈希密码的手段）
    val passwordHash: String,

    // 用户是否同意服务条款（注册必备条件）
    // 默认值：false，需用户手动勾选同意后设为true，否则无法完成注册
    val agreedToTerms: Boolean = false,

    // 账号创建时间戳（毫秒）
    // 默认值：当前系统时间，用于统计用户注册时间、排序用户列表等场景
    val createdAt: Long = System.currentTimeMillis()
)