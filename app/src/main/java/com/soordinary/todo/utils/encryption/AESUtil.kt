package com.soordinary.todo.utils.encryption

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
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
    private const val IV_SIZE = BLOCK_SIZE

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
     * 在TLS加密中使用，根据三个数来生成
     */
    fun generateKeyBytes(preMasterSecret:ByteArray,romNumber1:ByteArray,romNumber2:ByteArray): ByteArray{
        val combinedData = preMasterSecret + romNumber1 + romNumber2
        try {
            return MessageDigest.getInstance("SHA-256").digest(combinedData).copyOf(16)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 加密方法，传入要加密的数据和密钥，返回带 IV 的密文
     * @param plaintext 要加密的明文数据
     * @param key 加密使用的密钥
     * @return 带 IV 的密文
     */
    fun encrypt(plaintext: ByteArray?, key: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(key, ALGORITHM)
        val cipher = Cipher.getInstance(TRANSFORMATION)// ignore_security_alert [ByDesign7.3]WeakEncryptionModes
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

    // 计算加密后的大小
    fun calculateEncryptedSize(plaintextSize: Int): Int {
        // 计算填充后的明文大小
        val paddedSize = if (plaintextSize % BLOCK_SIZE == 0) {
            // 如果明文长度是块大小的整数倍，额外填充一个块
            plaintextSize + BLOCK_SIZE
        } else {
            // 否则，填充到下一个块大小的整数倍
            ((plaintextSize / BLOCK_SIZE) + 1) * BLOCK_SIZE
        }
        // 最终加密大小为 IV 大小加上填充后的明文大小
        return IV_SIZE + paddedSize
    }

    /**
     * 解密方法，传入带 IV 的密文和密钥，返回明文
     * @param ciphertext 带 IV 的密文
     * @param key 解密使用的密钥
     * @return 解密后的明文
     */
    fun decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(key, ALGORITHM)
        val iv = ByteArray(BLOCK_SIZE)
        val encryptedBytes = ByteArray(ciphertext.size - BLOCK_SIZE)
        System.arraycopy(ciphertext, 0, iv, 0, BLOCK_SIZE)
        System.arraycopy(ciphertext, BLOCK_SIZE, encryptedBytes, 0, encryptedBytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)// ignore_security_alert [ByDesign7.3]WeakEncryptionModes
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        return cipher.doFinal(encryptedBytes)
    }
}