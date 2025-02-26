package com.soordinary.todo.utils.encryption

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 对称加密AES-128静态工具类
 *
 * @role：生成密匙，用对应密匙加解密
 */
object AESUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val BLOCK_SIZE = 16

    /**
     * 生成符合要求的 AES 密钥字节数组
     * @param key 原始密钥字符串
     * @return 符合 AES 要求的密钥字节数组
     */
    private fun generateKeyBytes(key: String): ByteArray {
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
        val finalKeyBytes = ByteArray(BLOCK_SIZE)
        // 若 keyBytes 长度小于 BLOCK_SIZE，将其复制到 finalKeyBytes 并补 0
        if (keyBytes.size < BLOCK_SIZE) {
            System.arraycopy(keyBytes, 0, finalKeyBytes, 0, keyBytes.size)
        } else {
            System.arraycopy(keyBytes, 0, finalKeyBytes, 0, BLOCK_SIZE)
        }
        return finalKeyBytes
    }

    /**
     * 加密方法，传入要加密的数据和密钥，返回带 IV 的密文
     * @param plaintext 要加密的明文数据
     * @param key 加密使用的密钥
     * @return 带 IV 的密文
     */
    fun encrypt(plaintext: ByteArray?, key: String): ByteArray {
        val keyBytes = generateKeyBytes(key)
        val secretKeySpec = SecretKeySpec(keyBytes, ALGORITHM)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secureRandom = SecureRandom()
        val iv = ByteArray(BLOCK_SIZE)
        secureRandom.nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val ciphertext = cipher.doFinal(plaintext)
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)
        return combined
    }

    /**
     * 解密方法，传入带 IV 的密文和密钥，返回明文
     * @param ciphertext 带 IV 的密文
     * @param key 解密使用的密钥
     * @return 解密后的明文
     */
    fun decrypt(ciphertext: ByteArray, key: String): ByteArray {
        val keyBytes = generateKeyBytes(key)
        val secretKeySpec = SecretKeySpec(keyBytes, ALGORITHM)
        val iv = ByteArray(BLOCK_SIZE)
        val encryptedBytes = ByteArray(ciphertext.size - BLOCK_SIZE)
        System.arraycopy(ciphertext, 0, iv, 0, BLOCK_SIZE)
        System.arraycopy(ciphertext, BLOCK_SIZE, encryptedBytes, 0, encryptedBytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        return cipher.doFinal(encryptedBytes)
    }
}