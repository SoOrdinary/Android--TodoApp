package com.example.todo.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.todo.BuildConfig;
import com.example.todo.R;
import com.example.todo.data.model.ChatViewModel;
import com.example.todo.data.model.PersonalSharedViewModel;
import com.example.todo.data.model.TodoTagSharedViewModel;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import io.noties.markwon.Markwon;

public class PersonalFragment extends Fragment {

    private TodoTagSharedViewModel todoTagSharedViewModel;
    private ChatViewModel chatViewModel;
    private PersonalSharedViewModel personalSharedViewModel;
    private String userId;
    private ImageView pictureImageView;
    private TextView nameTextView;
    private TextView SignatureTextView;
    private ImageView editInformationImageView;
    private LinearLayout changeInterfaceLinearLayout;
    private ImageView currentPictureImageView;
    private ImageView currentChatPictureImageView;
    private TextView currentNameTextView;
    private TextView currentSignatureTextView;

    private LinearLayout taskTag;
    private LinearLayout chatClean;
    private LinearLayout introduction;
    private LinearLayout share;
    private LinearLayout submitBug;
    private LinearLayout aboutAuthor;


    // 监听器
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 ViewModel
        todoTagSharedViewModel = new ViewModelProvider(requireActivity()).get(TodoTagSharedViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        personalSharedViewModel = new ViewModelProvider(requireActivity()).get(PersonalSharedViewModel.class);
        // 控件绑定
        editInformationImageView = view.findViewById(R.id.edit_information);
        pictureImageView = view.findViewById(R.id.picture);
        nameTextView = view.findViewById(R.id.name);
        SignatureTextView = view.findViewById(R.id.signature);
        changeInterfaceLinearLayout = view.findViewById(R.id.change_interface);
        currentPictureImageView = view.findViewById(R.id.current_picture);
        currentChatPictureImageView = view.findViewById(R.id.current_chat_picture);
        currentNameTextView = view.findViewById(R.id.current_name);
        currentSignatureTextView = view.findViewById(R.id.current_signature);

        // 按钮绑定
        editInformationImageView.setOnClickListener(v -> {
            // 如果视图是可见的
            if (changeInterfaceLinearLayout.getVisibility() == View.VISIBLE) {
                changeInterfaceLinearLayout.setVisibility(View.GONE);
            } else {
                changeInterfaceLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        // 观察用户个人头像的Url
        personalSharedViewModel.getUserOwnPicLiveData().observe(getViewLifecycleOwner(), newOwnPic -> {
            if (newOwnPic != null && !newOwnPic.isEmpty()) {
                try {
                    // 解码Base64字符串
                    byte[] decodedString = Base64.decode(newOwnPic, Base64.DEFAULT);
                    // 将解码后的字节数组转换为Bitmap
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    // 将Bitmap显示在ImageView中
                    pictureImageView.setImageBitmap(decodedBitmap);
                    currentPictureImageView.setImageBitmap(decodedBitmap);
                } catch (IllegalArgumentException e) {
                    // 如果Base64解码失败，则加载默认头像
                    e.printStackTrace();
                    pictureImageView.setImageResource(R.drawable.profile_picture);
                    currentPictureImageView.setImageResource(R.drawable.profile_picture);
                }
            } else {
                // 如果头像为空或无效，加载默认头像
                pictureImageView.setImageResource(R.drawable.profile_picture);
                currentPictureImageView.setImageResource(R.drawable.profile_picture);
            }
        });

        // 观察用户聊天头像的Url
        personalSharedViewModel.getUserChatPicLiveData().observe(getViewLifecycleOwner(), newChatPic -> {
            currentChatPictureImageView.setColorFilter(Color.parseColor(newChatPic), PorterDuff.Mode.SRC_IN);
        });

        // 观察用户名的 LiveData
        personalSharedViewModel.getUserNameLiveData().observe(getViewLifecycleOwner(), newUserName -> {
            nameTextView.setText(newUserName);
            currentNameTextView.setText(newUserName);
        });

        // 观察个人签名的 LiveData
        personalSharedViewModel.getUserSignatureLiveData().observe(getViewLifecycleOwner(), newUserSignature -> {
            SignatureTextView.setText(newUserSignature);
            currentSignatureTextView.setText(newUserSignature);
        });

        // 修改的点击事件绑定
        // 个人头像
        currentPictureImageView.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            } else {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        // 聊天头像
        currentChatPictureImageView.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.fragment_personal_change);
            EditText changeChatPicToText = dialog.findViewById(R.id.want_to_chang);
            Button confirmButton = dialog.findViewById(R.id.confirm_change);
            changeChatPicToText.setHint("Hexadecimal color code");
            confirmButton.setOnClickListener(viewConfirm -> {
                String inputColor = changeChatPicToText.getText().toString();
                // 使用正则表达式直接验证十六进制颜色代码
                if (inputColor.matches("^[0-9A-Fa-f]{6}$")) {
                    // 有效就更新
                    personalSharedViewModel.updateUserChatPic("#" + inputColor);
                    dialog.dismiss();
                } else {
                    // 无效则提示
                    Toast.makeText(requireContext(), "Invalid hexadecimal color code", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });
        // 昵称
        currentNameTextView.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.fragment_personal_change);
            EditText changeNameToText = dialog.findViewById(R.id.want_to_chang);
            Button confirmButton = dialog.findViewById(R.id.confirm_change);
            changeNameToText.setHint("Change your name");
            confirmButton.setOnClickListener(viewConfirm -> {
                personalSharedViewModel.updateUserName(changeNameToText.getText().toString());
                dialog.dismiss();
            });
            dialog.show();
        });
        // 签名
        currentSignatureTextView.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.fragment_personal_change);
            EditText changeSignatureToText = dialog.findViewById(R.id.want_to_chang);
            Button confirmButton = dialog.findViewById(R.id.confirm_change);
            changeSignatureToText.setHint("Change your signature");
            confirmButton.setOnClickListener(viewConfirm -> {
                personalSharedViewModel.updateUserSignature(changeSignatureToText.getText().toString());
                dialog.dismiss();
            });
            dialog.show();
        });
        taskTag = view.findViewById(R.id.task_tag_management);
        chatClean = view.findViewById(R.id.clean_chat_history);
        introduction = view.findViewById(R.id.function_introduction);
        share = view.findViewById(R.id.share_with_your_friend);
        submitBug = view.findViewById(R.id.submit_a_bug);
        aboutAuthor = view.findViewById(R.id.about_author);
        taskTag.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.fragment_personal_todo_tag);
            EditText changeTag = dialog.findViewById(R.id.change_tag);
            Button deleteTag = dialog.findViewById(R.id.confirm_delete);
            Button addTag = dialog.findViewById(R.id.confirm_add);
            deleteTag.setOnClickListener(viewDelete -> {
                String tag = changeTag.getText().toString();
                if (todoTagSharedViewModel.isTagExist(tag)) {
                    todoTagSharedViewModel.removeTag(tag);
                    Toast.makeText(requireContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Delete failed, the tag does not exist", Toast.LENGTH_SHORT).show();
                }
            });
            addTag.setOnClickListener(viewDelete -> {
                String tag = changeTag.getText().toString();
                if (!todoTagSharedViewModel.isTagExist(tag)) {
                    todoTagSharedViewModel.addTag(tag);
                    Toast.makeText(requireContext(), "Add successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Add failed, the tag already exists", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });
        chatClean.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.fragment_personal_chat_clean);
            Button confirmDeleteAll = dialog.findViewById(R.id.confirm_delete);
            confirmDeleteAll.setOnClickListener(viewDelete -> {
                chatViewModel.deleteAll();
                Toast.makeText(requireContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            dialog.show();
        });
        introduction.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.fragment_personal_introduction);
            // 加载 TextView
            TextView textView = dialog.findViewById(R.id.user_manual);
            // 加载 Markdown 文件内容
            String markdown = loadMarkdownFromAssets("user_manual.md");
            // 使用 Markwon 渲染 Markdown 内容
            Markwon markwon = Markwon.create(requireActivity());
            markwon.setMarkdown(textView, markdown);

            dialog.show();
        });


        share.setOnClickListener(v -> {
            try {
                // GitHub 项目链接
                String url = "百度网盘[3.Android--TodoApp]\n" +
                        "链接: https://pan.baidu.com/s/17nlVIVCPMdmzWI7P4lnCSw\n" +
                        "提取码: mw74";

                // 创建 Intent 来分享 URL
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain"); // 设定为纯文本类型

                // 添加分享内容
                shareIntent.putExtra(Intent.EXTRA_TEXT, url);

                // 启动分享界面
                startActivity(Intent.createChooser(shareIntent, "Share GitHub URL"));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error sharing URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        submitBug.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.fragment_personal_submit_bug);
            dialog.show();
        });
        aboutAuthor.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(requireContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.fragment_personal_author);
            // 加载 TextView
            TextView textView = dialog.findViewById(R.id.author);
            // 加载 Markdown 文件内容
            String markdown = loadMarkdownFromAssets("author.md");
            // 使用 Markwon 渲染 Markdown 内容
            Markwon markwon = Markwon.create(requireActivity());
            markwon.setMarkdown(textView, markdown);
            dialog.show();
        });
    }

    // 从 assets 文件夹加载 markdown 文件
    private String loadMarkdownFromAssets(String url) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = requireActivity().getAssets().open(url);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
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

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);  // 使用JPEG格式压缩
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);  // 将字节数组转换为Base64字符串
    }

    private void saveImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            String base64Image = bitmapToBase64(bitmap);
            // 存储到 SharedPreferences 中
            personalSharedViewModel.updateUserOwnPic(base64Image);
            Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

}
