package com.example.todo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.todo.data.model.PersonalSharedViewModel;
import com.example.todo.data.model.TodoTagSharedViewModel;
import com.example.todo.ui.fragments.ChatFragment;
import com.example.todo.ui.fragments.PersonalFragment;
import com.example.todo.ui.fragments.TodoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private TodoTagSharedViewModel todoTagSharedViewModel;
    private PersonalSharedViewModel personalSharedViewModel;
    private DrawerLayout mDrawerLayout;
    // 全局toolbar及其属性
    Toolbar toolbar;
    TextView toolbarText;
    ActionBar actionBar;
    NavigationView navigationView;
    Menu menu;
    // 缓存 Fragment
    private TodoFragment todoFragment;
    private ChatFragment chatFragment;
    private PersonalFragment personalFragment;
    // 当前显示的 Fragment
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 ViewModel
        todoTagSharedViewModel = new ViewModelProvider(this).get(TodoTagSharedViewModel.class);
        personalSharedViewModel = new ViewModelProvider(this).get(PersonalSharedViewModel.class);

        // 自定义标题栏
        createBar();
        // 底部导航渲染
        renderBottomNav();
        // 侧边栏渲染
        renderSideNav();


    }

    // 隐藏了自带标题栏，创建自定义标题栏toolbar
    public void createBar() {
        // 找到自己设计的标题栏，并显示
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarText = findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        // 隐藏默认标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // 绘制导航栏图标，点击后会发出R.id.home
        actionBar = getSupportActionBar();
        personalSharedViewModel.getUserOwnPicLiveData().observe(this, newOwnPic -> {
            if (newOwnPic != null && !newOwnPic.isEmpty() && actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);

                // 获取 30dp 对应的像素值
                int sizeInPx = (int) (30 * getResources().getDisplayMetrics().density);

                try {
                    // 解码 Base64 字符串为 Bitmap
                    byte[] decodedString = Base64.decode(newOwnPic, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    // 使用 Glide 加载 Bitmap 并设置为 ActionBar 的 HomeAsUpIndicator
                    Glide.with(this)
                            .asBitmap()  // 将图片加载为 Bitmap
                            .load(decodedBitmap)  // 加载解码后的 Bitmap
                            .override(sizeInPx, sizeInPx)  // 设置图片大小为 30dp*30dp
                            .circleCrop()  // 裁剪为圆形
                            .placeholder(R.drawable.app_icon)  // 设置占位图
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    // 将 Bitmap 转换为 Drawable 并设置为 HomeAsUp 图标
                                    Drawable drawable = new BitmapDrawable(getResources(), resource);
                                    actionBar.setHomeAsUpIndicator(drawable);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    // 当图片加载失败或者被清除时，做一些清理操作（如显示占位图）
                                    actionBar.setHomeAsUpIndicator(placeholder);
                                }
                            });
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    // 处理 Base64 解码错误
                }
                // 如果当前页面不是Fragment，隐藏渲染的导航图标
                if (activeFragment != todoFragment && actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            }
        });
    }

    // 底部导航渲染
    public void renderBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        // 获取fragment管理器
        FragmentManager fragmentManager = getSupportFragmentManager();
        // 初始化 Fragment
        todoFragment = new TodoFragment();
        chatFragment = new ChatFragment();
        personalFragment = new PersonalFragment();
        activeFragment = todoFragment;// 默认Todo

        // 渲染默认的 Fragment（ToDoFragment）
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, todoFragment, "TODO_FRAGMENT")
                .commit();
        // 设置导航按钮点击监听
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            // 如果第一次选中，就进行缓存，否则直接拿旧的show
            switch (item.getItemId()) {
                case R.id.nav_todo:
                    selectedFragment = todoFragment;
                    break;
                case R.id.nav_chat:
                    selectedFragment = chatFragment;
                    if (!chatFragment.isAdded()) {
                        fragmentManager.beginTransaction()
                                .add(R.id.fragment_container, chatFragment, "CHAT_FRAGMENT")
                                .hide(activeFragment)
                                .show(chatFragment)
                                .commit();
                    }
                    break;
                case R.id.nav_personal:
                    selectedFragment = personalFragment;
                    if (!personalFragment.isAdded()) {
                        fragmentManager.beginTransaction()
                                .add(R.id.fragment_container, personalFragment, "PERSONAL_FRAGMENT")
                                .hide(activeFragment)
                                .show(personalFragment)
                                .commit();
                    }
                    break;
            }
            if (selectedFragment != null && selectedFragment != activeFragment) {
                // 切换 Fragment：隐藏当前的，显示选中的
                fragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(selectedFragment)
                        .commit();
                activeFragment = selectedFragment;  // 更新当前活动 Fragment
            }
            updateUIVisibility(selectedFragment);
            return true;
        });
    }

    // 根据 Fragment 设置 Toolbar 的属性
    private void updateUIVisibility(Fragment fragment) {
        // 获取顶部栏元素
        ActionBar actionBar = getSupportActionBar();
        if (fragment instanceof TodoFragment) {
            // 主题
            toolbarText.setText("TodoTask");
            // 导航栏
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            // 菜单
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setVisible(true);
                }
            }
        } else if (fragment instanceof ChatFragment) {
            toolbarText.setText("Self-Study Room");
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setVisible(false);
                }
            }
        } else if (fragment instanceof PersonalFragment) {
            toolbarText.setText("Information and Settings");
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
            if (menu != null) {
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setVisible(false);
                }
            }
        }
    }

    // 侧边栏渲染
    public void renderSideNav() {
        // 拿到整个布局对象和侧边栏
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.side_nav);
        // 默认选中的按钮
        navigationView.setCheckedItem(R.id.today_task);
        // 获取屏幕宽度
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        // 设置侧边栏宽度为屏幕的2/3
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = (int) (screenWidth * 0.66); // 66%屏幕宽度
        navigationView.setLayoutParams(params);
        // 点击逻辑
        navigationView.setNavigationItemSelectedListener(item -> {
            todoFragment.onClickTaskTag(item);
            mDrawerLayout.close();
            return true;
        });
        int sizeInPx = (int) (30 * getResources().getDisplayMetrics().density);
        navigationView.post(() -> {
            ImageView profilePicture = navigationView.findViewById(R.id.profile_picture);
            TextView username = navigationView.findViewById(R.id.username);
            TextView userSignature = navigationView.findViewById(R.id.user_signature);
            personalSharedViewModel.getUserOwnPicLiveData().observe(this, newOwnPic -> {
                if (newOwnPic != null && !newOwnPic.isEmpty()) {
                    try {
                        // 解码Base64字符串
                        byte[] decodedString = Base64.decode(newOwnPic, Base64.DEFAULT);
                        // 将解码后的字节数组转换为Bitmap
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        // 将Bitmap显示在ImageView中
                        profilePicture.setImageBitmap(decodedBitmap);
                    } catch (IllegalArgumentException e) {
                        // 如果Base64解码失败，则加载默认头像
                        e.printStackTrace();
                        Glide.with(this)
                                .asBitmap()  // 将图片加载为 Bitmap
                                .load(R.drawable.profile_picture)  // 本地资源或网络图片 URL
                                .override(sizeInPx, sizeInPx)  // 设置图片大小为 30dp*30dp
                                .circleCrop()  // 裁剪为圆形
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        // 将 Bitmap 转换为 Drawable 并设置为 HomeAsUp 图标
                                        Drawable drawable = new BitmapDrawable(getResources(), resource);
                                        actionBar.setHomeAsUpIndicator(drawable);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        // 可选：当图片清理时，可以设置占位符
                                    }
                                });
                    }
                } else {
                    // 如果头像为空或无效，加载默认头像
                    Glide.with(this)
                            .asBitmap()  // 将图片加载为 Bitmap
                            .load(R.drawable.profile_picture)  // 本地资源或网络图片 URL
                            .override(sizeInPx, sizeInPx)  // 设置图片大小为 30dp*30dp
                            .circleCrop()  // 裁剪为圆形
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    // 将 Bitmap 转换为 Drawable 并设置为 HomeAsUp 图标
                                    Drawable drawable = new BitmapDrawable(getResources(), resource);
                                    actionBar.setHomeAsUpIndicator(drawable);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    // 可选：当图片清理时，可以设置占位符
                                }
                            });
                }
            });
            // 观察用户名的 LiveData
            personalSharedViewModel.getUserNameLiveData().observe(this, newUserName -> {
                username.setText(newUserName);
            });

            // 观察个人签名的 LiveData
            personalSharedViewModel.getUserSignatureLiveData().observe(this, newUserSignature -> {
                userSignature.setText(newUserSignature);
            });
            // 观察标签数据的变化
            todoTagSharedViewModel.getTagListLiveData().observe(this, tags -> {
                // 更新 UI，显示标签列表
                updateTagMenu(navigationView.getMenu(), tags);
            });
        });
    }

    private void updateTagMenu(Menu menu, Set<String> tags) {
        // 清除现有的菜单项
        menu.findItem(R.id.tag).getSubMenu().clear();
        // 按字母顺序排序标签
        tags.stream().sorted().forEach(tag -> {
            menu.findItem(R.id.tag).getSubMenu()
                    .add(R.id.classify_by_tags, Menu.FIRST + 1, 0, tag) // 添加标签到菜单
                    .setCheckable(true); // 设置标签为可选中
        });
    }

    // 菜单渲染
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 点击头像打开侧边栏
            case android.R.id.home:
                if (mDrawerLayout != null) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;
            // 切换为线性布局
            case R.id.linear:
                ((TodoFragment) todoFragment).toggleLayout(true);
                break;
            // 切换为瀑布流布局
            case R.id.waterfall:
                ((TodoFragment) todoFragment).toggleLayout(false);
                break;
            default:
        }
        return true;
    }

}