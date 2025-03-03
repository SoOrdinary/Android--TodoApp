package com.soordinary.todo.data.network.file

interface DownloadListener {

    fun onProgress(progress: Int)

    fun onSuccess()

    fun onFailed()

}