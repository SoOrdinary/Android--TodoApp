package com.soordinary.todo.data.network.file

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * 实现下载文件的类，可自定义在下载过程的一些回调函数
 */
class DownloadTask(private val listener: DownloadListener) {

    companion object {
        const val TYPE_SUCCESS = 0
        const val TYPE_FAILED = 1
    }

    private var lastProgress = 0

    suspend fun download(downloadUrl: String, savePath: String, fileName: String): Int = withContext(Dispatchers.IO) {
        Log.d("liuyan","88")
        var inputStream: InputStream? = null
        var savedFile: RandomAccessFile? = null
        var file: File? = null
        try {
            var downloadedLength: Long = 0 // 记录已下载的文件长度
            file = File(savePath + fileName)
            if (file.exists()) {
                downloadedLength = file.length()
            }
            val contentLength = getContentLength(downloadUrl)
            if (contentLength == 0L) {
                return@withContext TYPE_FAILED
            } else if (contentLength == downloadedLength) {
                // 已下载字节和文件总字节相等，说明已经下载完成了
                return@withContext TYPE_SUCCESS
            }
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("RANGE", "bytes=$downloadedLength-")
                .url(downloadUrl)
                .build()
            val response: Response = client.newCall(request).execute()
            inputStream = response.body?.byteStream()
            savedFile = RandomAccessFile(file, "rw")
            savedFile.seek(downloadedLength) // 跳过已下载的字节
            val b = ByteArray(1024)
            var total = 0
            var len: Int
            while (inputStream?.read(b).also { len = it!! } != -1) {
                total += len
                savedFile.write(b, 0, len)
                // 计算已下载的百分比
                val progress = ((total + downloadedLength) * 100 / contentLength).toInt()
                if (progress > lastProgress) {
                    withContext(Dispatchers.Main) {
                        listener.onProgress(progress)
                    }
                    lastProgress = progress
                }
            }
            response.body?.close()
            return@withContext TYPE_SUCCESS
        } catch (e: Exception) {
            Log.d("liuyan",e.toString())
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                savedFile?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return@withContext TYPE_FAILED
    }

    @Throws(java.io.IOException::class)
    private suspend fun getContentLength(downloadUrl: String): Long = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(downloadUrl)
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val contentLength = response.body?.contentLength() ?: 0L
            response.close()
            return@withContext contentLength
        }
        return@withContext 0
    }
}