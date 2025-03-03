package com.soordinary.todo.utils.encryption

import java.security.SecureRandom

/**
 * 安全生成指定大小的随机数
 */
object RandomUtil {
    // 初始化 SecureRandom 实例
    private val SECURE_RANDOM = SecureRandom()

    /**
     * 生成指定长度的安全随机字节数组
     * @param size 所需随机字节数组的长度
     * @return 包含安全随机数的字节数组
     */
    fun generateRandomBytes(size: Int): ByteArray {
        require(size > 0) { "Size must be a positive integer." }
        val randomBytes = ByteArray(size)
        SECURE_RANDOM.nextBytes(randomBytes)
        return randomBytes
    }
}
