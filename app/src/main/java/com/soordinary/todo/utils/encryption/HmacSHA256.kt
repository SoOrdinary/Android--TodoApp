package com.soordinary.todo.utils.encryption

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * SHA256单向加密工具类，安全性高于MD5
 */
object HmacSHA256 {
    // 输入字符串，输出加密字符
    fun encryptBySHA256(secretKey: String, message: String): ByteArray {
        // 指定使用的 HMAC 算法，这里使用 HMAC-SHA256
        val algorithm = "HmacSHA256"
        // 创建 Mac 实例
        val mac = Mac.getInstance(algorithm)
        // 将密钥转换为字节数组
        val keyBytes = secretKey.toByteArray(StandardCharsets.UTF_8)
        // 创建 SecretKeySpec 对象
        val secretKeySpec = SecretKeySpec(keyBytes, algorithm)
        // 初始化 Mac 实例
        mac.init(secretKeySpec)
        // 将消息转换为字节数组
        val messageBytes = message.toByteArray(StandardCharsets.UTF_8)
        // 计算 HMAC 值
        val hmacBytes = mac.doFinal(messageBytes)
        return hmacBytes
    }
}