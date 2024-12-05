package com.example.todo.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.Internet.UDPClientTask;
import com.example.todo.Internet.UDPServerTask;
import com.example.todo.R;
import com.example.todo.data.model.TodoViewModel;
import com.example.todo.data.room.entity.Chat;
import com.example.todo.data.room.entity.Todo;
import com.example.todo.data.shared.TodoTagShared;
import com.example.todo.ui.adapter.ChatAdapter;
import com.example.todo.data.model.ChatViewModel;
import com.example.todo.data.shared.PersonalShared;
import com.example.todo.ui.listener.ClickChatListener;
import com.example.todo.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ClickChatListener {

    private Long currentUserId;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;
    private LiveData<List<Chat>> currentLiveData;
    private PersonalShared personalShared;

    // 接收广播的服务器与发送广播客户端
    private UDPServerTask udpServerTask;
    private UDPClientTask udpClientTask;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);


        personalShared = PersonalShared.getInstance(requireContext());
        currentUserId = personalShared.getUserId();
        RecyclerView recyclerView = view.findViewById(R.id.chat_content);
        EditText inputText = view.findViewById(R.id.input_text);
        Button sendButton = view.findViewById(R.id.send_button);

        // 初始化 RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(requireContext(), new ArrayList<>());
        chatAdapter.setClickChatListener(this);
        recyclerView.setAdapter(chatAdapter);

        // 获取 ViewModel
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        currentLiveData = chatViewModel.getAllChats();

        // 观察聊天记录的变化
        currentLiveData.observe(getViewLifecycleOwner(), chats -> {
            chatAdapter.setChatList(chats);
            recyclerView.scrollToPosition(chats.size() - 1); // 滚动到底部
        });

        udpServerTask = new UDPServerTask(requireContext(),chatViewModel);
        // 软键盘弹出后定位至最后一行，bug
        inputText.setOnClickListener(v-> {
            recyclerView.scrollToPosition(chatAdapter.getItemCount()- 1);
        });
        // 发送按钮点击事件
        sendButton.setOnClickListener(v -> {
            String content = inputText.getText().toString();
            if (!content.isEmpty()) {
                // 创建新消息
                Chat chat = new Chat(currentUserId, personalShared.getUserChatPic(), personalShared.getUserName(), System.currentTimeMillis(), Chat.TYPE_TEXT, content, null, null);
                chatViewModel.insert(chat); // 使用 ViewModel 插入消息
                udpClientTask = new UDPClientTask(chat.toString());
                udpClientTask.sendMessage();
                inputText.setText(""); // 清空输入框
            }
        });
        return view;
    }

    @Override
    public void onClickChatOwnPicture(View view) {

    }

    // 长按消息事件
    @Override
    public void onLongClickChat(View view, Chat chat) {
        Toast.makeText(requireContext(), "AAA", Toast.LENGTH_SHORT).show();
        switch (chat.getChatType()) {
            case Chat.TYPE_TEXT:
            {
                final Dialog dialog = new Dialog(requireContext());
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.fragment_todo_click_edit);
                // 获取布局中的输入控件
                EditText titleInput = dialog.findViewById(R.id.task_title);
                EditText subtitleInput = dialog.findViewById(R.id.task_subtitle);
                EditText detailsInput = dialog.findViewById(R.id.task_details);
                EditText dueDateDayInput = dialog.findViewById(R.id.task_due_date_day);
                EditText dueDateHourInput = dialog.findViewById(R.id.task_due_date_hour);
                EditText dueDateMinuteInput = dialog.findViewById(R.id.task_due_date_minute);
                Spinner tagSpinner = dialog.findViewById(R.id.task_tag);
                ImageView giveCoverImage =dialog.findViewById(R.id.task_cover_image);
                TextView coverUrlInput =dialog.findViewById(R.id.cover_url);
                Button button_confirm=dialog.findViewById(R.id.button_confirm);
                titleInput.setText(chat.getChatText());
                List<String> tags = new ArrayList<>(TodoTagShared.getInstance(requireContext()).getTags());
                tags.add(0,"default");
                // 分隔时间
                String[] parts = DateTimeUtils.getSeparatedStringFromTimestamp(DateTimeUtils.timestampToString(System.currentTimeMillis()));
                String day = parts[0];
                String hour = parts[1];
                String minute = parts[2];
                // 绑定时间
                dueDateDayInput.setText(day);
                dueDateHourInput.setText(hour);
                dueDateMinuteInput.setText(minute);

                // 创建 ArrayAdapter 并绑定数据
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),                         // 上下文
                        android.R.layout.simple_spinner_item,     // 下拉框的布局
                        tags                                      // 数据源
                );
                button_confirm.setText("Add");
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(adapter);
                button_confirm.setOnClickListener( dialogAddView ->{
                    // 获取输入内容
                    String title = titleInput.getText().toString().trim();
                    String subtitle = subtitleInput.getText().toString().trim();
                    String details = detailsInput.getText().toString().trim();
                    String dueDate = dueDateDayInput.getText().toString().trim()+"  "+dueDateHourInput.getText().toString().trim()+":"+dueDateMinuteInput.getText().toString().trim();
                    String coverImage = coverUrlInput.getText().toString().trim();
                    String tag = tagSpinner.getSelectedItem().toString();
                    // 校验输入
                    if (title.isEmpty() || dueDate.isEmpty()) {
                        Toast.makeText(requireContext(), "Title and Due Date are required!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 转换日期并创建任务对象
                    long dueTimestamp = DateTimeUtils.stringToTimestamp(dueDate);
                    Todo newTask = new Todo(
                            title,
                            subtitle,
                            details,
                            dueTimestamp,
                            false, // 默认未完成
                            coverImage,
                            tag
                    );

                    // 插入到数据库
                    (new ViewModelProvider(this).get(TodoViewModel.class)).insert(newTask);
                    dialog.dismiss();
                });
                dialog.show();
            }
                break;
        }
    }

    @Override
    public void onLongClickChatOwnPicture(View view) {
        if (udpServerTask == null || !udpServerTask.executor.isShutdown()) {
            udpServerTask = new UDPServerTask(requireContext(),chatViewModel);
            udpServerTask.startServer();
            Toast.makeText(requireContext(), "服务器已启动，等待客户端广播", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "服务器已经在运行", Toast.LENGTH_SHORT).show();
        }
    }
}

