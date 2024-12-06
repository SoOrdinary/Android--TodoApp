package com.example.todo.Internet;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.todo.data.model.ChatViewModel;
import com.example.todo.data.room.entity.Chat;
import com.example.todo.data.shared.PersonalShared;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 作为服务器，仅仅接收消息并解析
public class UDPServerTask implements Runnable {

    private static final int BROADCAST_PORT = 12345;  // 广播端口
    private ChatViewModel chatViewModel;
    public final ExecutorService executor;
    private volatile boolean running = true;
    private Context context;
    private final Handler mainHandler;

    public UDPServerTask(Context context, ChatViewModel chatViewModel) {
        this.context = context;
        this.chatViewModel = chatViewModel;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(BROADCAST_PORT)) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (running) {
                // 接收广播消息
                socket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // 更新 UI
                updateUI("Received: " + receivedMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(final String message) {
        mainHandler.post(() -> {
            Chat chat = Chat.fromString(message);
            // 保存接收的别人消息
            if (chat.getSenderId() != PersonalShared.getInstance(context).getUserId()) {
                chatViewModel.insert(chat);
            }
        });
    }

    public void startServer() {
        executor.execute(this);
    }

    public void stopServer() {
        running = false;
        executor.shutdownNow();
    }
}
