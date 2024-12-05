package com.example.todo.data.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.todo.utils.DateTimeUtils;

import java.util.Objects;

@Entity(tableName = "todos")
public class Todo {

    @PrimaryKey(autoGenerate = true)
    private int id;                  // 自增ID
    private String title;            // 标题
    private String subtitle;         // 副标题
    private String details;          // 具体内容
    @ColumnInfo(name = "due_date")
    private Long dueDate;            // 截止日期（时间戳，毫秒值）
    @ColumnInfo(name = "is_finish")
    private Boolean isFinish;        // 任务是否完成 (true: 已完成, false: 未完成)
    private String coverImage;       // 封面图片URI
    private String tag;              // 任务标签

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", details='" + details + '\'' +
                ", dueDate='" + DateTimeUtils.timestampToString(dueDate) + '\'' +
                ", isFinish=" + isFinish +
                ", coverImage='" + coverImage + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return id == todo.id;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Todo() {}

    @Ignore
    public Todo(String title, String subtitle, String details, Long dueDate, Boolean isFinish, String coverImage, String tag) {
        this.title = title;
        this.subtitle = subtitle;
        this.details = details;
        this.dueDate = dueDate;
        this.isFinish = isFinish;
        this.coverImage = coverImage;
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean isFinish() {
        return isFinish;
    }

    public void setFinish(Boolean isFinish) {
        this.isFinish = isFinish;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}

