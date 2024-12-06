package com.example.todo.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.todo.R;
import com.example.todo.data.room.entity.Todo;
import com.example.todo.ui.listener.ClickTaskListener;
import com.example.todo.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.BaseViewHolder> {

    private List<Todo> todoList;
    private boolean isLinearLayout; // true: 线性布局, false: 瀑布流布局
    private static ClickTaskListener clickTaskListener;  // 任务的点击事件回调接口

    public TodoAdapter(List<Todo> todoList, boolean isLinearLayout) {
        this.todoList = (todoList != null) ? todoList : new ArrayList<>(); // 避免空指针
        this.isLinearLayout = isLinearLayout;
    }

    public void setClickTaskListener(ClickTaskListener clickTaskListener) {
        this.clickTaskListener = clickTaskListener;
    }

    public void setTodoList(List<Todo> todoList) {
        this.todoList = (todoList != null) ? todoList : new ArrayList<>(); // 避免空指针
        notifyDataSetChanged();  // 通知 RecyclerView 更新数据
    }

    @Override
    public int getItemViewType(int position) {
        return isLinearLayout ? 1 : 2; // 返回布局类型
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) { // 线性布局
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_todo_linear, parent, false);
            return new LinearViewHolder(view);
        } else { // 瀑布流布局
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_todo_grid, parent, false);
            return new GridViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        Todo todo = todoList.get(position);

        holder.bind(todo);
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    // 基类
    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bind(Todo todo);
    }

    // Linear Layout
    static class LinearViewHolder extends BaseViewHolder {
        LinearLayout linearTask;
        TextView linearTitleTextView, linearSubtitleTextView, linearDueDateTextView;
        CheckBox linearStatusCheckBox;

        public LinearViewHolder(@NonNull View itemView) {
            super(itemView);
            linearTask = itemView.findViewById(R.id.task_linear);
            linearTitleTextView = itemView.findViewById(R.id.task_linear_title);
            linearSubtitleTextView = itemView.findViewById(R.id.task_linear_subtitle);
            linearDueDateTextView = itemView.findViewById(R.id.task_linear_dueDate);
            linearStatusCheckBox = itemView.findViewById(R.id.task_linear_status);
        }

        @Override
        public void bind(Todo todo) {
            // 解除
            linearTask.setOnLongClickListener(null);
            linearTask.setOnClickListener(null);
            linearStatusCheckBox.setOnCheckedChangeListener(null);
            // 同步
            linearTitleTextView.setText(todo.getTitle());
            linearSubtitleTextView.setText(todo.getSubtitle());
            linearDueDateTextView.setText(DateTimeUtils.timestampToString(todo.getDueDate()));
            linearStatusCheckBox.setChecked(todo.isFinish());
            if (todo.isFinish() == true) {
                linearTask.setAlpha(0.3f);
            } else {
                linearTask.setAlpha(1.0f);
            }
            if ((!todo.isFinish()) && (todo.getDueDate() < System.currentTimeMillis())) {
                linearDueDateTextView.setTextColor(Color.RED);
            } else {
                linearDueDateTextView.setTextColor(Color.parseColor("#018786"));
            }
            // 绑定
            linearTask.setOnLongClickListener(view -> {
                clickTaskListener.onLongClickTask(view, todo);
                return true;
            });
            linearTask.setOnClickListener(view -> clickTaskListener.onClickTask(view, todo));
            linearStatusCheckBox.setOnCheckedChangeListener((view, isChecked) -> clickTaskListener.onClickTask(view, todo));
        }
    }

    // Grid Layout
    static class GridViewHolder extends BaseViewHolder {
        LinearLayout gridTask;
        ImageView gridCoverImageView;
        TextView gridTitleTextView, gridSubtitleTextView, gridDueDateTextView;
        CheckBox gridStatusCheckBox;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            gridTask = itemView.findViewById(R.id.task_grid);
            gridCoverImageView = itemView.findViewById(R.id.task_grid_cover_image);
            gridTitleTextView = itemView.findViewById(R.id.task_grid_title);
            gridSubtitleTextView = itemView.findViewById(R.id.task_grid_subtitle);
            gridDueDateTextView = itemView.findViewById(R.id.task_grid_dueDate);
            gridStatusCheckBox = itemView.findViewById(R.id.task_grid_status);

        }

        @Override
        public void bind(Todo todo) {
            // 解除
            gridTask.setOnClickListener(null);
            gridTask.setOnLongClickListener(null);
            gridStatusCheckBox.setOnCheckedChangeListener(null);
            // 同步
            gridTitleTextView.setText(todo.getTitle());
            if (!todo.getSubtitle().equals("")) {
                gridSubtitleTextView.setVisibility(View.VISIBLE);
                gridSubtitleTextView.setText(todo.getSubtitle());
            } else {
                gridSubtitleTextView.setVisibility(View.GONE);
            }
            gridDueDateTextView.setText(DateTimeUtils.timestampToString(todo.getDueDate()));
            gridStatusCheckBox.setChecked(todo.isFinish());
            if (todo.getCoverImage() == null) {
                gridCoverImageView.setVisibility(View.GONE);
            } else {
                // 使用 Glide 加载图片
                // 获取图片的宽高比例
                Glide.with(gridCoverImageView.getContext())
                        .asBitmap()  // 加载为 Bitmap
                        .load(todo.getCoverImage())  // 加载图片的 URL
                        .placeholder(R.drawable.app_icon)  // 设置占位图
                        .into(new CustomTarget<Bitmap>() {  // 使用 CustomTarget 来处理加载后的图片
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // 使用 post() 方法来确保 LayoutParams 更新时视图已布局
                                gridCoverImageView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 获取 ImageView 的当前宽度
                                        int width = gridCoverImageView.getWidth();
                                        // 根据图片的原始比例计算高度
                                        int height = (int) (width * 1.0f * resource.getHeight() / resource.getWidth());

                                        // 设置 ImageView 的 LayoutParams 来调整高度
                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) gridCoverImageView.getLayoutParams();
                                        params.width = width;
                                        params.height = height;  // 动态设置图片的高度
                                        gridCoverImageView.setLayoutParams(params);

                                        // 加载图片到 ImageView
                                        gridCoverImageView.setImageBitmap(resource);
                                    }
                                });
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // 当图片加载失败或者被清除时，做一些清理操作（如显示占位图）
                                gridCoverImageView.setImageDrawable(placeholder);
                            }
                        });
            }
            if (todo.isFinish() == true) {
                gridTask.setAlpha(0.3f);
            } else {
                gridTask.setAlpha(1.0f);
            }
            if ((!todo.isFinish()) && (todo.getDueDate() < System.currentTimeMillis())) {
                gridDueDateTextView.setTextColor(Color.RED);
            } else {
                gridDueDateTextView.setTextColor(Color.parseColor("#018786"));
            }
            // 单选框的点击事件
            gridTask.setOnLongClickListener(view -> {
                clickTaskListener.onLongClickTask(view, todo);
                return true;
            });
            gridTask.setOnClickListener(view -> clickTaskListener.onClickTask(view, todo));
            gridStatusCheckBox.setOnCheckedChangeListener((view, isChecked) -> clickTaskListener.onClickTask(view, todo));
        }
    }
}
