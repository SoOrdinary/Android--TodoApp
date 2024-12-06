package com.example.todo.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.data.room.entity.Chat;
import com.example.todo.data.shared.PersonalShared;
import com.example.todo.ui.listener.ClickChatListener;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.BaseViewHolder> {

    private List<Chat> chatList;
    private final Long currentUserId;
    private static ClickChatListener clickChatListener;

    public ChatAdapter(Context context, List<Chat> chatList) {
        // 获取当前用户的ID
        this.currentUserId = PersonalShared.getInstance(context).getUserId();
        this.chatList = chatList;
    }

    public void setClickChatListener(ClickChatListener clickChatListener) {
        this.clickChatListener = clickChatListener;
    }

    public void setChatList(List<Chat> chatList) {
        this.chatList = (chatList != null) ? chatList : new ArrayList<>(); // 避免空指针
        notifyDataSetChanged();  // 通知 RecyclerView 更新数据
    }

    @Override
    public int getItemViewType(int position) {
        // 相等返回正数交流码代码代表我是发送者，否则返回负数交流码代码代表我是接收者
        if (currentUserId.equals(chatList.get(position).getSenderId())) {
            return Chat.TYPE_SEND * chatList.get(position).getChatType();
        } else {
            return Chat.TYPE_RECEIVE * chatList.get(position).getChatType();
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = null;
        switch (viewType) {
            case Chat.TYPE_SEND * Chat.TYPE_TEXT:
                view = inflater.inflate(R.layout.fragment_chat_sent, parent, false);
                return new SentTextViewHolder(view);
            case Chat.TYPE_RECEIVE * Chat.TYPE_TEXT:
                view = inflater.inflate(R.layout.fragment_chat_received, parent, false);
                return new ReceivedTextViewHolder(view);
            default:
                view = inflater.inflate(R.layout.fragment_chat_received, parent, false);
                return new UnknownViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    // 基类 ViewHolder，方便对所有消息进行基本的同步修改，用bind来进行个性化修改，优化了onBindViewHolder
    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bind(Chat chat);
    }

    // 发送消息的 ViewHolder
    static class SentTextViewHolder extends BaseViewHolder {
        AppCompatImageView sentPicture;
        TextView sentName;
        LinearLayout sentChat;
        TextView sentText;

        public SentTextViewHolder(@NonNull View itemView) {
            super(itemView);
            sentPicture = itemView.findViewById(R.id.sent_picture);
            sentName = itemView.findViewById(R.id.sent_name);
            sentChat = itemView.findViewById(R.id.sent_chat);
            sentText = itemView.findViewById(R.id.sent_text);

        }

        @Override
        public void bind(Chat chat) {
            sentPicture.setColorFilter(Color.parseColor(chat.getSenderPic()), PorterDuff.Mode.SRC_IN);
            sentName.setText(chat.getSenderName());
            sentText.setText(chat.getChatText());

            sentPicture.setOnClickListener(v -> {

            });
            // 长按启动服务器
            sentPicture.setOnLongClickListener(v -> {
                clickChatListener.onLongClickChatOwnPicture(v);
                return true;
            });
            sentChat.setOnLongClickListener(v -> {
                clickChatListener.onLongClickChat(v, chat);
                return true;
            });
        }
    }

    // 接收消息的 ViewHolder
    static class ReceivedTextViewHolder extends BaseViewHolder {
        AppCompatImageView receivedPicture;
        TextView receivedName;
        LinearLayout receiveChat;
        TextView receivedText;

        public ReceivedTextViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedPicture = itemView.findViewById(R.id.received_picture);
            receivedName = itemView.findViewById(R.id.received_name);
            receiveChat = itemView.findViewById(R.id.received_chat);
            receivedText = itemView.findViewById(R.id.received_text);
        }

        @Override
        public void bind(Chat chat) {
            receivedPicture.setColorFilter(Color.parseColor(chat.getSenderPic()), PorterDuff.Mode.SRC_IN);
            receivedName.setText(chat.getSenderName());
            receivedText.setText(chat.getChatText());

            receiveChat.setOnLongClickListener(v -> {
                clickChatListener.onLongClickChat(v, chat);
                return true;
            });
        }
    }


    // 未知类型消息
    static class UnknownViewHolder extends BaseViewHolder {

        public UnknownViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Chat chat) {

        }
    }
}
