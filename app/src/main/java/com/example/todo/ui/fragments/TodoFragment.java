package com.example.todo.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.todo.R;
import com.example.todo.data.shared.TodoTagShared;
import com.example.todo.receiver.TodoAlarmReceiver;
import com.example.todo.ui.adapter.TodoAdapter;
import com.example.todo.data.room.entity.Todo;
import com.example.todo.data.model.TodoViewModel;
import com.example.todo.ui.listener.ClickTaskListener;
import com.example.todo.utils.DateTimeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends Fragment implements ClickTaskListener {

    private RecyclerView recyclerView;
    private TodoAdapter todoAdapterLinear;
    private TodoAdapter todoAdapterGrid;
    private boolean isLinearLayout;

    // 上一个观察者
    private LiveData<List<Todo>> currentLiveData;
    private TodoViewModel todoViewModel;//数据库

    ImageView giveCoverImage;
    private String imageName;
    private String imageUrl;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 视图绑定与逻辑设置应该放在 onViewCreated 中
        recyclerView = view.findViewById(R.id.TaskList); // 绑定 RecyclerView
        todoAdapterLinear = new TodoAdapter(new ArrayList<>(), true);  // 线性适配器
        todoAdapterGrid = new TodoAdapter(new ArrayList<>(), false);  // 瀑布流适配器
        isLinearLayout = true;
        todoAdapterLinear.setClickTaskListener(this);
        todoAdapterGrid.setClickTaskListener(this);
        // 设置布局（初始布局设置）
        defaultLayout();
        // 获取 ViewModel 实例
        todoViewModel = new ViewModelProvider(this).get(TodoViewModel.class);
        // 观察当天的数据
        observeTodos(todoViewModel.getTodosByDueDateAndFinish(DateTimeUtils.getStartOfDay(0), DateTimeUtils.getEndOfDay(0), null));
        // 渲染悬浮按钮
        renderFab(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentLiveData.removeObservers(getViewLifecycleOwner());
    }

    private void observeTodos(LiveData<List<Todo>> liveData) {
        // 先移除之前的liveData
        if (currentLiveData != null) {
            currentLiveData.removeObservers(getViewLifecycleOwner());
        }
        // 绑定
        currentLiveData = liveData;
        currentLiveData.observe(getViewLifecycleOwner(), todos -> {
            // 更新适配器数据
            todoAdapterLinear.setTodoList(todos);
            todoAdapterGrid.setTodoList(todos);
        });
    }

    // 默认布局模式
    public void defaultLayout() {
        // 该函数出现在recyclerView已经设置之后
        if (isLinearLayout) {
            // 设置为线性布局[特殊瀑布流]
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(todoAdapterLinear);
        } else {
            // 设置为瀑布流布局
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(todoAdapterGrid);
        }
    }

    // 改变布局模式
    public void toggleLayout(boolean isLinearLayout) {
        // 全局变量保存当前流式信息
        this.isLinearLayout = isLinearLayout;
        if (isLinearLayout) {
            // 设置为线性布局
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(todoAdapterLinear);
        } else {
            // 设置为瀑布流布局
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(todoAdapterGrid);
        }
    }

    // 悬浮按钮的创建与渲染
    public void renderFab(View view) {
        FloatingActionButton fabAddTodo = (FloatingActionButton) view.findViewById(R.id.fab_add_todo);
        fabAddTodo.setOnClickListener(v -> {
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
            giveCoverImage = dialog.findViewById(R.id.task_cover_image);
            Button button_confirm = dialog.findViewById(R.id.button_confirm);
            List<String> tags = new ArrayList<>(TodoTagShared.getInstance(requireContext()).getTags());
            tags.add(0, "default");
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
            giveCoverImage.setOnClickListener(viewCover -> onClickPhoto(viewCover, ((Long) System.currentTimeMillis()).toString()));

            button_confirm.setOnClickListener(dialogAddView -> {
                // 获取输入内容
                String title = titleInput.getText().toString().trim();
                String subtitle = subtitleInput.getText().toString().trim();
                String details = detailsInput.getText().toString().trim();
                String dueDate = dueDateDayInput.getText().toString().trim() + "  " + dueDateHourInput.getText().toString().trim() + ":" + dueDateMinuteInput.getText().toString().trim();
                String coverImage = imageUrl;
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
                todoViewModel.insert(newTask);
                imageUrl = null;
                giveCoverImage = null;
                dialog.dismiss();
            });
            dialog.setOnCancelListener(viewCancel -> {
                if (imageUrl != null) {
                    File uncommittedCoverFile = new File(imageUrl);
                    if (uncommittedCoverFile.exists()) {
                        uncommittedCoverFile.delete();  // 删除原有封面图
                    }
                    imageUrl = null;
                    giveCoverImage = null;
                }
            });
            dialog.show();
        });
    }

    @Override
    public void onClickTask(View view, Todo todo) {
        switch (view.getId()) {
            // 点击进入详情
            case R.id.task_linear:
            case R.id.task_grid:
                final Dialog dialog = new Dialog(requireContext());
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.fragment_todo_click_view);
                TextView titleOutput = dialog.findViewById(R.id.task_title);
                TextView subtitleOutput = dialog.findViewById(R.id.task_subtitle);
                TextView detailsOutput = dialog.findViewById(R.id.task_details);
                TextView tagOutput = dialog.findViewById(R.id.task_tag);
                TextView dueDateOutput = dialog.findViewById(R.id.task_due_date);
                ImageView editImage = dialog.findViewById(R.id.todo_edit);
                ImageView alarmImage = dialog.findViewById(R.id.todo_alarm);
                titleOutput.setText(todo.getTitle());
                if (!todo.getSubtitle().equals("")) {
                    subtitleOutput.setVisibility(View.VISIBLE);
                    subtitleOutput.setText(todo.getSubtitle());
                } else {
                    subtitleOutput.setVisibility(View.GONE);
                }
                if (!todo.getDetails().equals("")) {
                    detailsOutput.setVisibility(View.VISIBLE);
                    detailsOutput.setText("  " + todo.getDetails());
                } else {
                    detailsOutput.setVisibility(View.GONE);
                }
                if (!todo.getTag().equals("")) {
                    tagOutput.setVisibility(View.VISIBLE);
                    tagOutput.setText(todo.getTag());
                } else {
                    tagOutput.setVisibility(View.GONE);
                }
                dueDateOutput.setText(DateTimeUtils.timestampToString(todo.getDueDate()));
                // 设置图片监听点击
                editImage.setOnClickListener(v -> onClickEdit(v, todo, newTodo -> {
                            // 修改后更新详情界面
                            titleOutput.setText(newTodo.getTitle());
                            if (!newTodo.getSubtitle().equals("")) {
                                subtitleOutput.setVisibility(View.VISIBLE);
                                subtitleOutput.setText(newTodo.getSubtitle());
                            } else {
                                subtitleOutput.setVisibility(View.GONE);
                            }
                            if (!newTodo.getDetails().equals("")) {
                                detailsOutput.setVisibility(View.VISIBLE);
                                detailsOutput.setText("  " + newTodo.getDetails());
                            } else {
                                detailsOutput.setVisibility(View.GONE);
                            }
                            if (!newTodo.getTag().equals("")) {
                                tagOutput.setVisibility(View.VISIBLE);
                                tagOutput.setText(newTodo.getTag());
                            } else {
                                tagOutput.setVisibility(View.GONE);
                            }
                            dueDateOutput.setText(DateTimeUtils.timestampToString(newTodo.getDueDate()));
                        })
                );
                alarmImage.setOnClickListener(v -> onClickAlarm(v, todo));
                dialog.show();
                break;
            // 更新是否完成
            case R.id.task_linear_status:
            case R.id.task_grid_status:
                boolean currentStatus = ((CheckBox) view).isChecked();
                if (todo.isFinish() != currentStatus) {
                    todo.setFinish(currentStatus);
                    todoViewModel.update(todo);
                }
                break;
            default:
        }
    }

    // 长按删除
    @Override
    public void onLongClickTask(View view, Todo todo) {
        switch (view.getId()) {
            case R.id.task_linear:
            case R.id.task_grid:
                final Dialog dialog = new Dialog(requireContext());
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.fragment_todo_click_delete);
                Button confirmDelete = dialog.findViewById(R.id.confirm_delete);
                confirmDelete.setOnClickListener(viewDelete -> {
                    todoViewModel.delete(todo);
                    dialog.dismiss();
                });
                dialog.show();
                break;
            default:
        }
    }

    // 侧边栏点击标签
    public void onClickTaskTag(MenuItem item) {
        switch (item.getGroupId()) {
            case R.id.classify_by_dates:
                switch (item.getItemId()) {
                    case R.id.today_task:
                        observeTodos(todoViewModel.getTodosByDueDateAndFinish(DateTimeUtils.getStartOfDay(0), DateTimeUtils.getEndOfDay(0), null));
                        break;
                    case R.id.list_task:
                        observeTodos(todoViewModel.getTodosByFinish(null));
                        break;
                }
                break;
            case R.id.classify_by_tags: {
                String tag = item.getTitle().toString();
                observeTodos(todoViewModel.getTodosByTagAndFinish(tag, null));
            }
        }
    }

    // 仅用于Todo详情页面的更新
    public interface TodoUpdateListener {
        void updateViewUI(Todo todo);
    }

    // 编辑按钮
    @Override
    public void onClickEdit(View view, Todo todo, TodoUpdateListener listener) {
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
        giveCoverImage = dialog.findViewById(R.id.task_cover_image);
        Button button_confirm = dialog.findViewById(R.id.button_confirm);

        // 绑定已有值
        titleInput.setText(todo.getTitle());
        subtitleInput.setText(todo.getSubtitle());
        detailsInput.setText(todo.getDetails());
        // 分隔时间
        String[] parts = DateTimeUtils.getSeparatedStringFromTimestamp(DateTimeUtils.timestampToString(todo.getDueDate()));
        String day = parts[0];
        String hour = parts[1];
        String minute = parts[2];
        // 绑定时间
        dueDateDayInput.setText(day);
        dueDateHourInput.setText(hour);
        dueDateMinuteInput.setText(minute);
        // 给tag绑定对应值
        List<String> tags = new ArrayList<>(TodoTagShared.getInstance(requireContext()).getTags());
        tags.add(0, "default");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),                         // 上下文
                android.R.layout.simple_spinner_item,     // 下拉框的布局
                tags                                      // 数据源
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(adapter);
        int position = tags.indexOf(todo.getTag());
        if (position != -1) {  // 确保tag存在
            tagSpinner.setSelection(position);  // 选择"life"所在的位置
        }
        giveCoverImage.setOnClickListener(viewCover -> onClickPhoto(viewCover, ((Long) System.currentTimeMillis()).toString()));
        button_confirm.setText("Update");

        button_confirm.setOnClickListener(dialogAddView -> {
            // 更新Todo
            todo.setTitle(titleInput.getText().toString().trim());
            todo.setSubtitle(subtitleInput.getText().toString().trim());
            todo.setDetails(detailsInput.getText().toString().trim());
            todo.setDueDate(DateTimeUtils.stringToTimestamp(dueDateDayInput.getText().toString().trim() + "  " + dueDateHourInput.getText().toString().trim() + ":" + dueDateMinuteInput.getText().toString().trim()));
            todo.setCoverImage(imageUrl);
            todo.setTag(tagSpinner.getSelectedItem().toString());
            // 校验输入
            if (todo.getTitle().isEmpty() || DateTimeUtils.timestampToString(todo.getDueDate()).isEmpty()) {
                Toast.makeText(requireContext(), "Title and Due Date are required!", Toast.LENGTH_SHORT).show();
                return;
            }
            // 数据库更新
            todoViewModel.update(todo);
            // 观察界面的UI更新
            listener.updateViewUI(todo);
            imageUrl = null;
            giveCoverImage = null;
            dialog.dismiss();
        });
        dialog.setOnCancelListener(viewCancel -> {
            if (imageUrl != null) {
                File uncommittedCoverFile = new File(imageUrl);
                if (uncommittedCoverFile.exists()) {
                    uncommittedCoverFile.delete();  // 删除原有封面图
                }
                imageUrl = null;
                giveCoverImage = null;
            }
        });
        dialog.show();
    }

    // 闹钟按钮
    @Override
    public void onClickAlarm(View view, Todo todo) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.fragment_todo_click_alarm);
        EditText earlyDays = dialog.findViewById(R.id.early_days);
        EditText earlyHours = dialog.findViewById(R.id.early_hours);
        EditText earlyMinutes = dialog.findViewById(R.id.early_minutes);
        Button confirmRemind = dialog.findViewById(R.id.confirm_remind);
        earlyDays.setText("0");
        earlyHours.setText("0");
        earlyMinutes.setText("30");
        confirmRemind.setOnClickListener(v -> {
            // 获取用户输入
            int days = Integer.parseInt(earlyDays.getText().toString());
            int hours = Integer.parseInt(earlyHours.getText().toString());
            int minutes = Integer.parseInt(earlyMinutes.getText().toString());

            // 计算提前的时间 (转换为毫秒)
            long timeInAdvance = (days * 24 * 60 * 60 * 1000)
                    + (hours * 60 * 60 * 1000)
                    + (minutes * 60 * 1000);

            // 创建一个 Intent，用来发送广播
            Intent alarmIntent = new Intent(requireContext(), TodoAlarmReceiver.class);
            alarmIntent.putExtra("title", todo.getTitle()); // 设置消息内容为任务标题
            alarmIntent.putExtra("subTitle", todo.getSubtitle());// 任务副标题

            // 创建 PendingIntent，这样广播接收器就可以收到这个 Intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), todo.getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // 获取 AlarmManager 实例
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            // 判断是否授权了闹钟精确提醒功能
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                // 请求运行时权限
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, 1);
                try {
                    // 使用 setExact 设置精确的触发时间
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, todo.getDueDate() - timeInAdvance, pendingIntent);
                    // 提示用户闹钟已设置
                    Toast.makeText(requireContext(), "Alarm set for task: " + DateTimeUtils.timestampToString(todo.getDueDate() - timeInAdvance), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Insufficient  permission", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                try {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, todo.getDueDate() - timeInAdvance, pendingIntent);
                    Toast.makeText(requireContext(), "Alarm set for task: " + DateTimeUtils.timestampToString(todo.getDueDate() - timeInAdvance), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Insufficient  permission", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            dialog.dismiss();
        });

        dialog.show();

    }


    // 仅用于编辑界面图片的更新
    public interface TodoEditUpdateListener {
        void updateEditViewUI(String coverUrl);
    }

    // 相册选择按钮点击事件
    @Override
    public void onClickPhoto(View view, String name) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            imageName = ((Long) System.currentTimeMillis()).toString();
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        } else {
            imageName = ((Long) System.currentTimeMillis()).toString();
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == requireActivity().RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上的系统
                        handleImageOnKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(requireContext(), uri)) {
            // document类型的Uri，通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // content类型，普遍处理
                imagePath = getImagePath(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                // file类型，直接获取
                imagePath = uri.getPath();
            }
            saveImage(imagePath);//根据图片路径显示图片
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        saveImage(imagePath);
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = requireActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void saveImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            // 保存到应用缓存目录中
            try {
                File file = new File(requireContext().getCacheDir(), imageName);
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                Toast.makeText(requireContext(), file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                // 重复选择的时候删除上一张
                if (imageUrl != null) {
                    File uncommittedCoverFile = new File(imageUrl);
                    if (uncommittedCoverFile.exists()) {
                        uncommittedCoverFile.delete();  // 删除原有封面图
                    }
                    imageUrl = null;
                }
                imageUrl = file.getAbsolutePath();

                Glide.with(requireView())
                        .load(new File(imageUrl))  // 加载本地图片路径
                        .into(giveCoverImage);  // 设置到 ImageView 中
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(requireContext(), "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }
}
