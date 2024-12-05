package com.example.todo.Internet;

import android.os.Handler;
import android.os.Looper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 作为客户端，仅仅广播消息
public class UDPClientTask implements Runnable {

    private static final int BROADCAST_PORT = 12345;  // 广播端口
    private static final String BROADCAST_ADDRESS = "255.255.255.255";  // 广播地址，发送到局域网所有设备
    private String message;
    public final ExecutorService executor;
    private final Handler mainHandler;

    public UDPClientTask(String message) {
        this.message = message;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(BROADCAST_ADDRESS), BROADCAST_PORT);
            socket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage() {
        executor.execute(this);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
