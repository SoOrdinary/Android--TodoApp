package com.soordinary.todo.view.foreground.summarize

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.soordinary.foreground.IForeground
import com.soordinary.todo.TodoApplication

object ForegroundServiceTool {

    const val TAG = "ForegroundServiceTool"

    lateinit var mService: IForeground

    private val serviceConnection = ForegroundServiceConnection()

    class ForegroundServiceConnection() : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            mService = IForeground.Stub.asInterface(service)
            mService.setPasswordFinish(true)
            // 重新启动一下
            startForeground()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
        }
    }

    // 密码输入成功后再调用
    fun bindForegroundAfterPassword() {
        with(TodoApplication.context){
            // 启动服务并获取aidl
            val bindIntent = Intent(this, ForegroundService::class.java).apply {
                setPackage(packageName+":foreground_service")
            }
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindForeground(){
        with(TodoApplication.context){
            unbindService(serviceConnection)
        }
    }

    fun startForeground() {
        with(TodoApplication.context){
            // 启动服务
            val serviceIntent = Intent(this, ForegroundService::class.java).apply {
                setPackage(packageName+":foreground_service")
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    fun stopForeground(){
        with(TodoApplication.context){
            stopService(Intent(this, ForegroundService::class.java))
        }
    }

}