package com.soordinary.todo.data.network.socket

import android.app.Activity
import android.widget.TextView
import com.fasterxml.jackson.databind.ObjectMapper
import com.soordinary.todo.TodoApplication
import com.soordinary.todo.data.shared.UserInfoSharedPreference
import com.soordinary.todo.utils.encryption.AESUtil
import com.soordinary.todo.utils.encryption.HmacSHA256
import com.soordinary.todo.utils.encryption.RSAUtil
import com.soordinary.todo.utils.encryption.RandomUtil
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.PublicKey
import kotlin.jvm.Throws

/**
 * 基于局域网的服务器，数据传输的定制流程
 */
class DataTransferOld(private val activity: Activity, private val oldPort: Int, private val newPasswordMAD5: String, private val logView: TextView, private val end: () -> Unit) {

    private lateinit var serverSocket: ServerSocket
    private lateinit var oldSocket: Socket

    // 收发状态量
    private var isSend = true
    private var isReceive = true
    private var expectMessageId = 0
    @Volatile
    private var step = 0

    // 打印加密过程log
    private val process = false

    // 中间态的变量
    private lateinit var oldPrivateKey: PrivateKey

    private lateinit var newPublicKey: PublicKey
    private lateinit var romNumber1: ByteArray
    private lateinit var oldPublicKey: PublicKey
    private lateinit var romNumber2: ByteArray
    private lateinit var preMasterSecret: ByteArray
    private lateinit var masterSecret: ByteArray

    // 加密传输的全局量(完成加密协商、最大重传次数、发送后已被接收文件量、临时同步量)
    private var isTLSFlag = false
    private var retransmissionMax = 3
    @Volatile
    private var receivedFileCount = 0
    private var tempCount = 0

    init {
        // 生成自己的公私钥
        val pair = RSAUtil.generateKeyPair()
        oldPrivateKey = pair.private
        oldPublicKey = pair.public
        // 根据流程，本机要发送romNumber2
        romNumber2 = RandomUtil.generateRandomBytes(32)
    }

    // 开启加密协商与加密传输，加密协商已经完成则传入true
    fun start(isTLSFinish: Boolean) {
        serverSocket = ServerSocket(oldPort)
        serverSocket.soTimeout = 10000

        // 准备输入输出流
        var output: OutputStream? = null
        var input: InputStream? = null
        var sendRequest: OutputStreamWriter? = null
        var readResponse: BufferedReader? = null

        try {
            addLog("正在搜索，倒计时10s..." + "")
            oldSocket = serverSocket.accept()
            addLog("已找到新设备:${oldSocket.inetAddress}")
            addLog("******")
            output = oldSocket.getOutputStream()
            input = oldSocket.getInputStream()
            // 读取新设备请求的线程
            val inputThread = Thread {
                readResponse = BufferedReader(InputStreamReader(input, Charset.forName("UTF-8")))
                var request: String?
                try {
                    while (isReceive) {
                        request = readResponse!!.readLine()
                        if (process) addLog("新设备请求: $request")
                        if (!defRequest(expectMessageId, request)) {
                            addLog("!!加密过程出现错误")
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
            // 向新设备应答的线程
            val outputThread = Thread {
                sendRequest = OutputStreamWriter(output, Charset.forName("UTF-8"))
                try {
                    while (isSend) {
                        // 读取线程根据请求修改step
                        if (step < 0) {
                            return@Thread
                        }
                        if (step != 0) {
                            val message = responseByStep(step)
                            sendRequest!!.write("$message\n")
                            sendRequest!!.flush()
                            if (process) addLog("(本机)旧设备应答:$message")
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
            // 传输数据的线程
            val outputFileThread = Thread {
                try {
                    // 获取同步量
                    tempCount = receivedFileCount
                    // 填写需要传过去的文件或文件夹,基于包名的相对路径
                    val fileList = listOf("cache/task_photo_cache", "cache/user_icon_cache", "databases", "shared_prefs")
                    for (subFile in fileList) {
                        val file = File(activity.dataDir, subFile)
                        if (file.exists() && file.isDirectory) {
                            sendFolderContents(file, output)
                        } else if (file.isFile) {
                            sendSingleFile(file, output)
                        }
                    }
                    output.write("completed~".toByteArray(StandardCharsets.UTF_8))
                    addLog("------")
                    addLog("数据传输完毕")
                } catch (e: Exception) {
                    addLog("!!异常出错:${e.message}")
                    e.printStackTrace()
                }
            }
            // 监听异常的线程
            val inputErrorThread = Thread {
                try {
                    val symbolArray = ByteArray(8)
                    while (true) {
                        readFully(input, symbolArray)
                        val symbol = String(symbolArray, Charsets.UTF_8).toInt()
                        if (symbol > 0) {
                            receivedFileCount = symbol
                        } else if (symbol == 0) {
                            // 数据同步成功
                            isTLSFlag = false
                            addLog("------")
                            addLog("数据同步成功")
                            break
                        } else {
                            // 发生异常情况传递负数并打断
                            addLog("------")
                            addLog("!!数据同步出现异常")
                            break
                        }
                    }
                } catch (e: Exception) {
                    addLog("!!异常出错:${e.message}")
                    e.printStackTrace()
                }
            }

            // 根据传入参数决定是否执行加密协商
            if (!isTLSFinish) {
                inputThread.start()
                outputThread.start()
                // 开始响应
                expectMessageId = 1
                // 等待两个线程结束
                inputThread.join()
                outputThread.join()
                // step小于0，出现异常，直接结束
                if (step < 0) {
                    return
                }
                addLog("******")
            }
            // 等待一段时间，因为读取方要花费时间一定时间验证第四次握手信息后，再来创建读取流，不延时无法保证在写入输入流前读取流能够成功创建，而本次文件量也很大，容易把缓冲区循环覆盖掉，所以最好等待一下
            Thread.sleep(1000)
            // 加密并发送文件
            addLog("开始传输加密数据")
            outputFileThread.start()
            inputErrorThread.start()
            outputFileThread.join()
            inputErrorThread.join()
        } catch (e: IOException) {
            addLog("!!异常出错:${e.message}")
            e.printStackTrace()
        } finally {
            sendRequest?.close()
            readResponse?.close()
            output?.close()
            input?.close()
            // 如果超时断开的话，oldSocket还没有初始化
            if (::oldSocket.isInitialized) {
                oldSocket.close()
            }
            serverSocket.close()

            // 检查加密传输过程是否出现问题,进行断点重传
            if (isTLSFlag && retransmissionMax > 0) {
                retransmissionMax--
                addLog("------")
                addLog("已同步${receivedFileCount}份文件，中途异常，尝试第${3 - retransmissionMax}次重新同步...")
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

    // 辨别请求
    private fun defRequest(expectMessageId: Int, request: String): Boolean {
        val message = ObjectMapper().readValue(request, Message::class.java)
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
            // 收到第一次握手传来的消息,验证对方的公钥是否无误
            1 -> {
                addLog("  收到第一次握手信息")
                // 首先验证消息完成性与公钥是否是对方的
                val checksum = HmacSHA256.encryptBySHA256("SoOrdinary", payload.toChecksumString())
                if (!message.checksum.contentEquals(checksum)) {
                    addLog("!!信息已被篡改，可能遭到入侵")
                    return false
                }
                val anotherInfo = HmacSHA256.encryptBySHA256(newPasswordMAD5, payload.newPublicKeyToString ?: "")
                if (!payload.anotherInfo.contentEquals(anotherInfo)) {
                    addLog("!!公钥不可信任，连接已被中间人伪装")
                    return false
                }

                // 发来的消息可被信任
                newPublicKey = RSAUtil.publicKeyFromBase64(payload.newPublicKeyToString)
                romNumber1 = payload.romNumber1!!
                // 第一次握手成功，开始第二次
                step = 2
                addLog("  第一次握手信息验证无误")
            }
            // 收到第三次握手的消息，进行同步
            3 -> {
                addLog("  收到第三次握手信息")
                // 验证信息完整性（摘要已被签名）
                val checksum = HmacSHA256.encryptBySHA256("SoOrdinary", payload.toChecksumString())
                if (!RSAUtil.verify(checksum, message.checksum, newPublicKey)) {
                    addLog("!!信息已被篡改，可能遭到入侵")
                    return false
                }

                // 信息可被信任
                preMasterSecret = RSAUtil.decrypt(payload.preMasterSecretEncrypted!!, oldPrivateKey)
                masterSecret = AESUtil.generateKeyBytes(preMasterSecret, romNumber1, romNumber2)
                // 测试密钥是否可通过
                if ("I trust your data" != String(AESUtil.decrypt(payload.anotherInfo!!, masterSecret), StandardCharsets.UTF_8)) {
                    addLog("!!密匙解密失败，请排查握手流程")
                    return false
                }
                // 第三次握手成功，开始第四次
                step = 4
                isReceive = false
                addLog("  第四次握手信息验证无误")
            }
        }

        return true
    }

    // 响应请求
    private fun responseByStep(step: Int): String {
        var message: Message? = null
        when (step) {
            // 第二次握手，旧设备需要响应请求，其中包括 [自己的公钥 、 第二个随机数（用新设备的公钥加密） 、SHA256加密的自己的公钥（自己的密码的MD5值作密匙）]
            2 -> {
                val oldPublicKeyToString = RSAUtil.publicKeyToBase64(oldPublicKey)
                val romNumber2Encrypted = RSAUtil.encrypt(romNumber2, newPublicKey)
                val anotherInfo = HmacSHA256.encryptBySHA256(UserInfoSharedPreference.userPassword!!, oldPublicKeyToString)

                val payload = Payload(oldPublicKeyToString = oldPublicKeyToString, romNumber2Encrypted = romNumber2Encrypted, anotherInfo = anotherInfo)
                message = Message(id = "2", payload = payload)
                // 等待第三次握手的消息
                expectMessageId = 3
                addLog("发送第二次握手信息")
            }
            // 第四次握手，响应包括 [随机密匙(用旧设备的公钥加密) 、 加密通知“Encryption begins”(可算出公共密匙，用其加密)]  本次需对摘要签名
            4 -> {
                val anotherInfo = AESUtil.encrypt("Encryption begins".toByteArray(StandardCharsets.UTF_8), masterSecret)

                val payload = Payload(anotherInfo = anotherInfo)
                message = Message(id = "4", payload = payload)
                message.checksum = RSAUtil.sign(message.checksum, oldPrivateKey)
                // 握手结束，发送数据
                isSend = false
                addLog("发送第四次握手信息")
                isTLSFlag = true
            }
        }
        return ObjectMapper().writeValueAsString(message)
    }


    /**
     * 传送文件api
     */

    // 递归发送文件夹内容
    private fun sendFolderContents(folder: File, outputStream: OutputStream) {
        val files = folder.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    sendSingleFile(file, outputStream)
                } else if (file.isDirectory) {
                    sendFolderContents(file, outputStream)
                }
            }
        }
    }

    // 发送单个文件
    @Throws(java.io.IOException::class)
    private fun sendSingleFile(file: File, outputStream: OutputStream) {
        // 已经同步的文件不再发送
        if(tempCount>0){
            tempCount--
            return
        }
        addLog("------")
        // 传输每个文件的读取标志，用来检测乱序覆盖
        outputStream.write("SoOrdinary".toByteArray(StandardCharsets.UTF_8))
        // 获取文件相对于根文件夹的相对路径
        val relativePath = file.absolutePath.removePrefix(activity.dataDir.absolutePath + File.separator)
        val relativePathBytes = relativePath.toByteArray(StandardCharsets.UTF_8)
        val relativePathLength = relativePathBytes.size
        outputStream.write(intToByteArray(relativePathLength))
        outputStream.write(relativePathBytes)
        // 发送文件长度
        val fileLength = file.length()
        outputStream.write(longToByteArray(fileLength))
        addLog("文件大小：${fileLength}B")
        // 发送文件内容(未加密)
//            val fileInputStream=FileInputStream(file)
//            val buffer = ByteArray(8192)
//            var bytes = fileInputStream.read(buffer)
//            while (bytes >= 0) {
//                outputStream.write(buffer, 0, bytes)
//                bytes = fileInputStream.read(buffer)
//            }
        // 发送文件内容
        val fileInputStream = FileInputStream(file)
        val buffer = ByteArray(8192)
        while (fileInputStream.read(buffer) >= 0) {
            outputStream.write(AESUtil.encrypt(buffer, masterSecret))
        }
        outputStream.flush()
        fileInputStream.close()
        addLog("传输: $relativePath")
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
        return totalBytesRead
    }

    // 将整数转换为字节数组，用于转换文件路径大小
    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }

    // 将长整数转换为字节数组，用于转换文件大小
    private fun longToByteArray(value: Long): ByteArray {
        return byteArrayOf(
            (value shr 56).toByte(),
            (value shr 48).toByte(),
            (value shr 40).toByte(),
            (value shr 32).toByte(),
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }

}