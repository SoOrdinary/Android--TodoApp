package com.soordinary.todo.utils.encryption

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * MD5单向加密工具类
 */
object MD5Util {
    // 输入字符串，输出加密字符(转换字符时4位作为一个字符，所以128位一共32字符)
    fun encryptByMD5(input: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())
            val hexString = StringBuilder()
            for (b in messageDigest) {
                val hex = Integer.toHexString(0xFF and b.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}