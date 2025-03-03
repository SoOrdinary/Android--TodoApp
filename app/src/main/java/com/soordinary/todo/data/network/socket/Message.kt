package com.soordinary.todo.data.network.socket

import android.util.Base64
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.soordinary.todo.TodoApplication
import com.soordinary.todo.utils.encryption.HmacSHA256

/**
 * 认证协议的握手交互消息格式，校验码仅校验payload
 */
data class Message(
    @JsonProperty("version") val version: String = TodoApplication.context.packageManager.getPackageInfo(TodoApplication.context.packageName, 0).versionName,
    @JsonProperty("id") val id: String,
    @JsonProperty("payload") val payload: Payload,
    @JsonProperty("checksum") var checksum: ByteArray = HmacSHA256.encryptBySHA256("SoOrdinary", payload.toChecksumString())
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (version != other.version) return false
        if (id != other.id) return false
        if (payload != other.payload) return false
        if (!checksum.contentEquals(other.checksum)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + payload.hashCode()
        result = 31 * result + checksum.contentHashCode()
        return result
    }
}

data class Payload(
    val newPublicKeyToString: String?=null,
    val romNumber1:ByteArray?=null,
    val oldPublicKeyToString: String?=null,
    val romNumber2Encrypted:ByteArray?=null,
    val preMasterSecretEncrypted:ByteArray?=null,
    val anotherInfo: ByteArray? = null
) {



    fun toChecksumString(): String {
        val mapper = ObjectMapper()
        val map = mutableMapOf<String, Any?>()
        map["newPublicKey"] = newPublicKeyToString
        map["romNumber1"] = romNumber1?.let { Base64.encodeToString(it, Base64.DEFAULT) }
        map["oldPublicKey"] = oldPublicKeyToString
        map["romNumber2"] = romNumber2Encrypted?.let { Base64.encodeToString(it, Base64.DEFAULT) }
        map["preMasterSecret"] = preMasterSecretEncrypted?.let { Base64.encodeToString(it, Base64.DEFAULT) }
        map["anotherInfo"] = anotherInfo?.let { Base64.encodeToString(it, Base64.DEFAULT) }
        return mapper.writeValueAsString(map)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Payload

        if (newPublicKeyToString != other.newPublicKeyToString) return false
        if (romNumber1 != null) {
            if (other.romNumber1 == null) return false
            if (!romNumber1.contentEquals(other.romNumber1)) return false
        } else if (other.romNumber1 != null) return false
        if (oldPublicKeyToString != other.oldPublicKeyToString) return false
        if (romNumber2Encrypted != null) {
            if (other.romNumber2Encrypted == null) return false
            if (!romNumber2Encrypted.contentEquals(other.romNumber2Encrypted)) return false
        } else if (other.romNumber2Encrypted != null) return false
        if (preMasterSecretEncrypted != null) {
            if (other.preMasterSecretEncrypted == null) return false
            if (!preMasterSecretEncrypted.contentEquals(other.preMasterSecretEncrypted)) return false
        } else if (other.preMasterSecretEncrypted != null) return false
        if (anotherInfo != null) {
            if (other.anotherInfo == null) return false
            if (!anotherInfo.contentEquals(other.anotherInfo)) return false
        } else if (other.anotherInfo != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = newPublicKeyToString?.hashCode() ?: 0
        result = 31 * result + (romNumber1?.contentHashCode() ?: 0)
        result = 31 * result + (oldPublicKeyToString?.hashCode() ?: 0)
        result = 31 * result + (romNumber2Encrypted?.contentHashCode() ?: 0)
        result = 31 * result + (preMasterSecretEncrypted?.contentHashCode() ?: 0)
        result = 31 * result + (anotherInfo?.contentHashCode() ?: 0)
        return result
    }

}
