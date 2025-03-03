package com.soordinary.todo.utils.encryption

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import android.util.Base64
import javax.crypto.Cipher

/**
 * RSA非对称加密
 *
 * 生成公私钥，加解密，签名
 */
object RSAUtil {
    private const val ALGORITHM = "RSA"
    private const val KEY_SIZE = 2048

    /**
     * 生成 RSA 密钥对
     * @return 密钥对对象
     * @throws NoSuchAlgorithmException 如果不支持 RSA 算法
     */
    @Throws(NoSuchAlgorithmException::class)
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
        keyPairGenerator.initialize(KEY_SIZE)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * 将公钥转换为 Base64 编码的字符串
     * @param publicKey 公钥对象
     * @return Base64 编码的公钥字符串
     */
    fun publicKeyToBase64(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
    }

    /**
     * 将私钥转换为 Base64 编码的字符串
     * @param privateKey 私钥对象
     * @return Base64 编码的私钥字符串
     */
    fun privateKeyToBase64(privateKey: PrivateKey): String {
        return Base64.encodeToString(privateKey.encoded, Base64.DEFAULT)
    }

    /**
     * 从 Base64 编码的字符串中恢复公钥
     * @param base64PublicKey Base64 编码的公钥字符串
     * @return 公钥对象
     * @throws Exception 如果恢复公钥时出现异常
     */
    @Throws(Exception::class)
    fun publicKeyFromBase64(base64PublicKey: String?): PublicKey {
        val keyBytes = Base64.decode(base64PublicKey, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(ALGORITHM)
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * 从 Base64 编码的字符串中恢复私钥
     * @param base64PrivateKey Base64 编码的私钥字符串
     * @return 私钥对象
     * @throws Exception 如果恢复私钥时出现异常
     */
    @Throws(Exception::class)
    fun privateKeyFromBase64(base64PrivateKey: String?): PrivateKey {
        val keyBytes = Base64.decode(base64PrivateKey, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(ALGORITHM)
        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * 使用公钥加密数据
     * @param plainBytes 明文数据的字节数组
     * @param publicKey 公钥对象
     * @return 加密后的字节数组
     * @throws Exception 如果加密过程中出现异常
     */
    @Throws(Exception::class)
    fun encrypt(plainBytes: ByteArray, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(plainBytes)
    }

    /**
     * 使用私钥解密数据
     * @param encryptedBytes 加密后的字节数组
     * @param privateKey 私钥对象
     * @return 解密后的字节数组
     * @throws Exception 如果解密过程中出现异常
     */
    @Throws(Exception::class)
    fun decrypt(encryptedBytes: ByteArray, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(encryptedBytes)
    }

    /**
     * 使用私钥进行签名
     * @param dataBytes 待签名的数据的字节数组
     * @param privateKey 私钥对象
     * @return 签名后的字节数组
     * @throws Exception 如果签名过程中出现异常
     */
    @Throws(Exception::class)
    fun sign(dataBytes: ByteArray, privateKey: PrivateKey): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(dataBytes)
        return signature.sign()
    }

    /**
     * 使用公钥验证签名
     * @param dataBytes 原始数据的字节数组
     * @param signBytes 签名后的字节数组
     * @param publicKey 公钥对象
     * @return 签名是否有效
     * @throws Exception 如果验证签名过程中出现异常
     */
    @Throws(Exception::class)
    fun verify(dataBytes: ByteArray, signBytes: ByteArray, publicKey: PublicKey): Boolean {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(dataBytes)
        return signature.verify(signBytes)
    }
}