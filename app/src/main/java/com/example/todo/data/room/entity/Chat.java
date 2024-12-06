package com.example.todo.data.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "chats")
public class Chat {

    // 交流类型码
    public static final int TYPE_NULL = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_PIC = 2;
    public static final int TYPE_VOICE = 3;
    // 接收发送者识别码
    public static final int TYPE_RECEIVE = -1;
    public static final int TYPE_SEND = 1;

    @PrimaryKey(autoGenerate = true)
    private int id;                     // 自增ID
    @ColumnInfo(name = "sender_id")
    private Long senderId;              // 发送者ID
    @ColumnInfo(name = "sender_pic")
    private String senderPic;           // 发送者头像颜色[十六进制]
    @ColumnInfo(name = "sender_name")
    private String senderName;          // 发送者昵称
    @ColumnInfo(name = "send_time")
    private Long sendTime;            // 发送时间
    @ColumnInfo(name = "chat_type")
    private int chatType;               // 交流类型码（文本、图片、语音）
    @ColumnInfo(name = "chat_text")
    private String chatText;            // 文本消息
    @ColumnInfo(name = "chat_pic")
    private String chatPic;             // 图片消息
    @ColumnInfo(name = "chat_voice")
    private String chatVoice;           // 语音消息

    public Chat() {
    }

    @Ignore
    public Chat(Long senderId, String senderPic, String senderName, Long sendTime, int chatType, String chatText, String chatPic, String chatVoice) {
        this.senderId = senderId;
        this.senderPic = senderPic;
        this.senderName = senderName;
        this.sendTime = sendTime;
        this.chatType = chatType;
        this.chatText = chatText;
        this.chatPic = chatPic;
        this.chatVoice = chatVoice;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id + ", " +
                "senderId=" + senderId + ", " +
                "senderPic='" + senderPic + "', " +
                "senderName='" + senderName + "', " +
                "sendTime=" + sendTime + ", " +
                "chatType=" + chatType + ", " +
                "chatText='" + chatText + "', " +
                "chatPic='" + chatPic + "', " +
                "chatVoice='" + chatVoice + "'" +
                "}";
    }

    // 分解字符串并返回一个 Chat 对象
    public static Chat fromString(String chatString) {
        Chat chat = new Chat();

        // 提取字段内容
        try {
            // 从字符串中提取 id, senderId, senderPic 等字段的值
            String[] parts = chatString.replace("Chat{", "").replace("}", "").split(", ");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // 根据 key 分配值
                switch (key) {
                    case "id":
                        chat.setId(Integer.parseInt(value));
                        break;
                    case "senderId":
                        chat.setSenderId(Long.parseLong(value));
                        break;
                    case "senderPic":
                        chat.setSenderPic(value.replace("'", ""));
                        break;
                    case "senderName":
                        chat.setSenderName(value.replace("'", ""));
                        break;
                    case "timestamp":
                        chat.setSendTime(Long.parseLong(value));
                        break;
                    case "chatType":
                        chat.setChatType(Integer.parseInt(value));
                        break;
                    case "chatText":
                        chat.setChatText(value.replace("'", ""));
                        break;
                    case "chatPic":
                        chat.setChatPic(value.replace("'", ""));
                        break;
                    case "chatVoice":
                        chat.setChatVoice(value.replace("'", ""));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return id == chat.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderPic() {
        return senderPic;
    }

    public void setSenderPic(String senderPic) {
        this.senderPic = senderPic;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public String getChatPic() {
        return chatPic;
    }

    public void setChatPic(String chatPic) {
        this.chatPic = chatPic;
    }

    public String getChatVoice() {
        return chatVoice;
    }

    public void setChatVoice(String chatVoice) {
        this.chatVoice = chatVoice;
    }

}
