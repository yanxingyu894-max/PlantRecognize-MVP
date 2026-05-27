package com.example.afinal.util

import java.security.MessageDigest

/**
 * 哈希工具类：提供SHA-256加密功能
 * 作用：将字符串转换为不可逆的哈希值，常用于密码加密、数据校验等场景
 * 例如：用户注册/登录时，密码不会明文存储，而是存储加密后的哈希值
 */
object HashUtils {
    /**
     * SHA-256加密方法
     * @param input 要加密的原始字符串（比如用户密码）
     * @return 加密后的十六进制字符串（长度64位）
     * 原理：
     * 1. 获取SHA-256加密算法实例
     * 2. 将字符串转为UTF-8编码的字节数组
     * 3. 计算字节数组的哈希摘要
     * 4. 将摘要字节转为十六进制字符串（每个字节转2位十六进制）
     */
    fun sha256(input: String): String {
        // 获取SHA-256加密算法的实例
        val md = MessageDigest.getInstance("SHA-256")
        // 将输入字符串转为UTF-8字节数组，并计算哈希摘要
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        // 遍历摘要字节，转为十六进制字符串（%02x表示补零到2位），并拼接成最终字符串
        return digest.joinToString("") { "%02x".format(it) }
    }
}