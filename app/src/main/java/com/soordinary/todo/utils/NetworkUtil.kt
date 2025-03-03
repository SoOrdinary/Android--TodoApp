package com.soordinary.todo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.text.TextUtils
import android.widget.Toast


/**
 * 网络通信工具类
 */
object NetworkUtil {
    const val PORT: Int = 8888

    fun isValid(context: Context): Boolean {
        return isWifiConnected(context) && !TextUtils.isEmpty(getLocalIpAddress(context))
    }

    // 检查是否连接到WiFi
    private fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        val isWifiConnected = netInfo != null && netInfo.isConnected && netInfo.type == ConnectivityManager.TYPE_WIFI
        if (!isWifiConnected) {
            Toast.makeText(context, "WIFI 未连接", Toast.LENGTH_SHORT)
        }
        return isWifiConnected
    }

    // 获取当前WiFi的IP地址
    fun getLocalIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val str = String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )
        if (TextUtils.isEmpty(str)) {
            Toast.makeText(context, "WIFI IP 地址获取失败", Toast.LENGTH_SHORT)
        }
        return str
    }
}