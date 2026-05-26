package com.example.afinal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体类，与数据库中的「users」表一一映射
 * 存储用户登录凭据和基础信息，所有字段对应表的列
 * 设计原则：仅存储必要的本地数据，敏感信息（密码）存储哈希值而非明文
 */
@Entity(tableName = "users") // 标记为Room实体，指定对应的数据表名：users
data class UserEntity(
    // 主键：用户名作为唯一标识，保证每个用户的唯一性
    // 类型：String，非空，不可修改（data class的val特性）
    @PrimaryKey val username: String,

    // 密码哈希值：存储SHA-256/MD5等哈希后的字符串，严禁存储明文密码
    // 安全注意：需配合盐值（salt）使用，防止彩虹表攻击
    val passwordHash: String,

    // 用户是否同意服务条款：默认false，注册时需用户勾选后设为true
    val agreedToTerms: Boolean = false,

    // 账号创建时间：默认当前系统时间戳（毫秒），用于统计/排序
    val createdAt: Long = System.currentTimeMillis()
)