package com.soordinary.todo.data.network.socket

import android.app.Activity
import android.widget.TextView
import com.fasterxml.jackson.databind.ObjectMapper
import com.soordinary.todo.TodoApplication
import com.soordinary.todo.data.shared.UserMMKV
import com.soordinary.todo.utils.encryption.AESUtil
import com.soordinary.todo.utils.encryption.HmacSHA256
import com.soordinary.todo.utils.encryption.RSAUtil
import com.soordinary.todo.utils.encryption.RandomUtil
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.PublicKey

/**
 * 基于局域网的客户端，数据传输的定制流程
 */
class DataTransferNew(private val activity: Activity, private val oldIP: String, private val oldPort: Int, private val oldPasswordMAD5: String, private val logView: TextView, private val end: () -> Unit) {

    private lateinit var newSocket: Socket

    // 收发状态量
    private var isSend = true
    private var isReceive = true
    private var expectMessageId = 0

    @Volatile
    private var step = 0

    // 打印加密过程log
    private val process = false

    // 中间态的变量
    private lateinit var newPrivateKey: PrivateKey

    private lateinit var newPublicKey: PublicKey
    private lateinit var romNumber1: ByteArray
    private lateinit var oldPublicKey: PublicKey
    private lateinit var romNumber2: ByteArray
    private lateinit var preMasterSecret: ByteArray
    private lateinit var masterSecret: ByteArray

    // 加密传输的全局量(加密传输标志、最大重传次数、已接收文件量)
    private var isTLSFlag = false
    private var retransmissionMax = 3

    @Volatile
    private var fileCount = 0

    // 心跳检测标志
    private var isDancing:Boolean? = false

    init {
        // 生成自己的公私钥
        val pair = RSAUtil.generateKeyPair()
        newPrivateKey = pair.private
        newPublicKey = pair.public
        // 根据流程，本机要发送romNumber1和preMasterSecret
        romNumber1 = RandomUtil.generateRandomBytes(32)
        preMasterSecret = RandomUtil.generateRandomBytes(48)
    }

    // 开启加密协商与加密传输，加密协商已经完成则传入true
    fun start(isTLSFinish: Boolean) {
        newSocket = Socket()
        // 准备输入输出流
        var output: OutputStream? = null
        var input: InputStream? = null
        var sendRequest: OutputStreamWriter? = null
        var readResponse: BufferedReader? = null

        try {
            addLog("尝试发起连接，倒计时10s...")
            val address = InetSocketAddress(oldIP, oldPort)
            newSocket.connect(address, 10000)
            addLog("已连接到旧设备")
            addLog("******")
            output = newSocket.getOutputStream()
            input = newSocket.getInputStream()

            // 向旧设备发送请求的线程
            val outputThread = Thread {
                sendRequest = OutputStreamWriter(output, Charset.forName("UTF-8"))
                try {
                    while (isSend) {
                        // 读取线程根据应答修改step，如果step小于0，则说明交互结束或中途出错，关闭通道
                        if (step < 0) {
                            addLog("!!加密过程出现错误")
                            return@Thread
                        }
                        if (step > 0) {
                            val message = sendRequest(step)
                            sendRequest!!.write("$message\n")
                            sendRequest!!.flush()
                            if (process) addLog("(本机)新设备请求:$message")
                            step = 0
                        }
                        Thread.sleep(10)
                    }
                } catch (e: Exception) {
                    addLog("!!异常出错:${e.message}")
                    e.printStackTrace()
                    isReceive = false
                    isSend = false
                    step = -1
                }
            }
            // 接收旧设备应答的线程
            val inputThread = Thread {
                readResponse = BufferedReader(InputStreamReader(input, Charset.forName("UTF-8")))
                var response: String?
                try {
                    while (isReceive) {
                        response = readResponse!!.readLine()
                        if (process) addLog("旧设备应答: $response")
                        if (!defResponse(expectMessageId, response)) {
                            step = -1
                            return@Thread
                        }
                        Thread.sleep(10)
                    }
                } catch (e: Exception) {
                    addLog("!!异常出错:${e.message}")
                    e.printStackTrace()
                    isReceive = false
                    isSend = false
                    step = -1
                }
            }
            // 接收文件的线程
            val inputFileThread = Thread {
                try {
                    receiveFileContents(input, output)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // 心跳线程初始化
            val heartThread = Thread {
                while (true){
                    isDancing=false
                    Thread.sleep(10000)
                    // 10秒内心跳还是false就强制关闭
                    if(isDancing!=true){
                        // 没有心跳说明已经结束啦，跳出
                        if(isDancing==null) break
                        // 尝试提醒一下服务器
                        output.write((-999).toFixedLengthString(8).toByteArray(StandardCharsets.UTF_8))
                        // 强制关闭
                        sendRequest?.close()
                        readResponse?.close()
                        output?.close()
                        input?.close()
                        newSocket.close()
                        inputFileThread.interrupt()
                        break
                    }
                }
            }

            // 根据传入参数决定是否执行加密协商
            if(!isTLSFinish){
                inputThread.start()
                outputThread.start()
                // 开始请求
                step = 1
                // 等待两个线程结束
                inputThread.join()
                outputThread.join()

                // step小于0说明出现问题了，直接结束
                if (step < 0) {
                    return
                }
            }

            addLog("******")
            // 接收并解析文件
            addLog("开始接收加密数据")

            inputFileThread.start()
            heartThread.start()
            inputFileThread.join()
        } catch (e: IOException) {
            e.printStackTrace()
            addLog("!!异常出错:${e.message}")
        } finally {
            // 已经结尾则置空
            isDancing = null
            sendRequest?.close()
            readResponse?.close()
            output?.close()
            input?.close()
            newSocket.close()
            // 检查加密传输过程是否出现问题,进行断点重传
            if (isTLSFlag && retransmissionMax > 0) {
                retransmissionMax--
                addLog("------")
                addLog("已同步${fileCount}份文件，中途异常，尝试第${3 - retransmissionMax}次重新同步...")
                start(isTLSFlag)
            }
            // 执行传递进来的首尾工作
            end()
        }
    }

    // 更新日志
    private fun addLog(log: String) {
        activity.runOnUiThread { logView.append("  $log\n") }
    }

    /**
     * 加密流程调用
     */

    // 发送请求
    private fun sendRequest(step: Int): String {
        var message: Message? = null
        when (step) {
            // 第一次握手，新设备需要发送请求，其中包括 [自己的公钥 、 第一个随机数 、SHA256加密的自己的公钥（自己的密码的MD5值作密匙）]
            1 -> {
                val newPublicKeyToString = RSAUtil.publicKeyToBase64(newPublicKey)
                val romNumber1 = romNumber1
                val anotherInfo = HmacSHA256.encryptBySHA256(UserMMKV.userPassword!!, newPublicKeyToString)

                val payload = Payload(newPublicKeyToString = newPublicKeyToString, romNumber1 = romNumber1, anotherInfo = anotherInfo)
                message = Message(id = "1", payload = payload)
                // 等待第二次握手的消息
                expectMessageId = 2
                addLog("发送第一次握手信息")
            }
            // 第三次握手，请求包括 [随机密匙(用旧设备的公钥加密) 、 加密通知“I trust your data”(可算出公共密匙，用其加密)]  本次需对摘要签名
            3 -> {
                val preMasterSecretEncrypted = RSAUtil.encrypt(preMasterSecret, oldPublicKey)
                masterSecret = AESUtil.generateKeyBytes(preMasterSecret, romNumber1, romNumber2)
                val anotherInfo = AESUtil.encrypt("I trust your data".toByteArray(StandardCharsets.UTF_8), masterSecret)

                val payload = Payload(preMasterSecretEncrypted = preMasterSecretEncrypted, anotherInfo = anotherInfo)
                message = Message(id = "3", payload = payload)
                message.checksum = RSAUtil.sign(message.checksum, newPrivateKey)
                // 等待第四次握手的消息
                expectMessageId = 4
                isSend = false
                addLog("发送第三次握手信息")
            }
        }
        return ObjectMapper().writeValueAsString(message)
    }

    // 辨别应答
    private fun defResponse(expectMessageId: Int, response: String): Boolean {
        val message = ObjectMapper().readValue(response, Message::class.java)
        // 如果版本号或期望消息号不同，判断出现异常，中断
        if (message.version != TodoApplication.context.packageManager.getPackageInfo(TodoApplication.context.packageName, 0).versionName) {
            addLog("!!两设备软件版本不一致")
            return false
        }
        if (expectMessageId != message.id.toInt()) {
            addLog("!!握手流程出错，可能出现恶意攻击")
            return false
        }
        // 开始验证正文
        val payload = message.payload
        when (expectMessageId) {
            // 收到第二次握手传来的消息,验证对方的公钥是否无误
            2 -> {
                addLog("  收到第二次握手信息")
                // 首先验证消息完成性与公钥是否是对方的
                val checksum = HmacSHA256.encryptBySHA256("SoOrdinary", payload.toChecksumString())
                if (!message.checksum.contentEquals(checksum)) {
                    addLog("!!信息已被篡改，可能遭到入侵")
                    return false
                }
                val anotherInfo = HmacSHA256.encryptBySHA256(oldPasswordMAD5, payload.oldPublicKeyToString ?: "")
                if (!payload.anotherInfo.contentEquals(anotherInfo)) {
                    addLog("!!公钥不可信任，连接已被中间人伪装")
                    return false
                }
                // 发来的消息可被信任(随机数2被加密)
                oldPublicKey = RSAUtil.publicKeyFromBase64(payload.oldPublicKeyToString)
                romNumber2 = RSAUtil.decrypt(payload.romNumber2Encrypted!!, newPrivateKey)
                // 第二次握手成功，开始第三次
                step = 3
                addLog("  第二次握手信息验证无误")
            }
            // 收到第四次握手消息，准备接收文件
            4 -> {
                addLog("  收到第四次握手信息")
                // 验证信息完整性（摘要已被签名）
                val checksum = HmacSHA256.encryptBySHA256("SoOrdinary", payload.toChecksumString())
                if (!RSAUtil.verify(checksum, message.checksum, oldPublicKey)) {
                    addLog("!!信息已被篡改，可能遭到入侵")
                    return false
                }
                // 信息可被信任，测试密匙是否有效
                if ("Encryption begins" != String(AESUtil.decrypt(payload.anotherInfo!!, masterSecret), StandardCharsets.UTF_8)) {
                    addLog("!!密匙解密失败，请排查握手流程")
                    return false
                }
                // 握手结束，开始接收数据
                isReceive = false
                addLog("  第四次握手信息验证无误")
                isTLSFlag = true
            }
        }
        return true
    }


    /**
     * 接收文件api
     */

    // 接收文件内容
    private fun receiveFileContents(inputStream: InputStream, outputStream: OutputStream) {
        try {
            while (true) {
                addLog("------")
                // 读取文件标志是否正确
                val fileSymbol = ByteArray(10)
                readFully(inputStream, fileSymbol)
                val fileSymbolString = String(fileSymbol, Charsets.UTF_8)
                if (fileSymbolString != "SoOrdinary") {
                    if (fileSymbolString == "completed~") {
                        addLog("数据同步完成,重启软件可完成更新")
                        UserMMKV.userPassword = ""
                        addLog("软件密码已重置")
                        outputStream.write(0.toFixedLengthString(8).toByteArray(StandardCharsets.UTF_8))
                        isTLSFlag = false
                        break
                    }
                    outputStream.write((-1).toFixedLengthString(8).toByteArray(StandardCharsets.UTF_8))
                    addLog("!!数据同步出错，网络波动")
                    break
                }
                // 读取文件相对路径长度
                val relativePathLengthBytes = ByteArray(4)
                readFully(inputStream, relativePathLengthBytes)
                val relativePathLength = byteArrayToInt(relativePathLengthBytes)

                // 读取文件相对路径
                val relativePathBytes = ByteArray(relativePathLength)
                readFully(inputStream, relativePathBytes)
                val relativePath = String(relativePathBytes, Charsets.UTF_8)

                // 读取文件长度
                val fileLengthBytes = ByteArray(8)
                readFully(inputStream, fileLengthBytes)
                val fileLength = byteArrayToLong(fileLengthBytes)
                addLog("文件大小：${fileLength}B")
                // 创建文件保存路径
                val file = File(activity.dataDir, relativePath)
                file.parentFile?.mkdirs()
                // 保存文件内容（未加密）
//                var remainToRead: Long = fileLength
//                val fileOutputStream=FileOutputStream(file)
//                val buffer = ByteArray(8192)
//                while (remainToRead > 0 ) {
//                    if(remainToRead<8192){
//                        val remain = remainToRead.toInt()
//                        inputStream.read(buffer,0,remain)
//                        fileOutputStream.write(buffer, 0, remain)
//                        break
//                    }else{
//                        inputStream.read(buffer)
//                        fileOutputStream.write(buffer)
//                        remainToRead -=8192
//                    }
//                }
                // 保存文件内容
                var remainToRead: Long = fileLength
                val fileOutputStream = FileOutputStream(file)
                val buffer = ByteArray(8224)
                while (remainToRead > 0) {
                    readFully(inputStream, buffer)
                    val partFile = AESUtil.decrypt(buffer, masterSecret)
                    if (remainToRead > 8192) {
                        fileOutputStream.write(partFile)
                    } else {
                        fileOutputStream.write(partFile, 0, remainToRead.toInt())
                    }
                    remainToRead -= 8192
                }
                fileOutputStream.flush()
                fileOutputStream.close()
                addLog("已接收：$relativePath")
                // 发送已完成文件数
                fileCount++
                outputStream.write(fileCount.toFixedLengthString(8).toByteArray(StandardCharsets.UTF_8))
            }
        } catch (e: IOException) {
            outputStream.write((-2).toFixedLengthString(8).toByteArray(StandardCharsets.UTF_8))
            addLog("!!异常出错:${e.message}")
            e.printStackTrace()
        }
    }

    // 保证接收到完整长度
    private fun readFully(inputStream: InputStream, buffer: ByteArray): Int {
        var totalBytesRead = 0

        while (totalBytesRead < buffer.size) {
            val bytesRead = inputStream.read(buffer, totalBytesRead, buffer.size - totalBytesRead)
            if (bytesRead == -1) {
                continue
            }
            totalBytesRead += bytesRead
        }
        isDancing = true
        return totalBytesRead
    }

    // 将字节数组转换为整数
    private fun byteArrayToInt(bytes: ByteArray): Int {
        return (bytes[0].toInt() and 0xFF shl 24) or
                (bytes[1].toInt() and 0xFF shl 16) or
                (bytes[2].toInt() and 0xFF shl 8) or
                (bytes[3].toInt() and 0xFF)
    }

    // 将字节数组转换为长整数
    private fun byteArrayToLong(bytes: ByteArray): Long {
        return (bytes[0].toLong() and 0xFF shl 56) or
                (bytes[1].toLong() and 0xFF shl 48) or
                (bytes[2].toLong() and 0xFF shl 40) or
                (bytes[3].toLong() and 0xFF shl 32) or
                (bytes[4].toLong() and 0xFF shl 24) or
                (bytes[5].toLong() and 0xFF shl 16) or
                (bytes[6].toLong() and 0xFF shl 8) or
                (bytes[7].toLong() and 0xFF)
    }

    // 将Int转换为固定长度字符串
    private fun Int.toFixedLengthString(length: Int): String {
        val str = this.toString()
        return if (str.length < length) {
            "0".repeat(length - str.length) + str
        } else {
            str.take(length)
        }
    }
}