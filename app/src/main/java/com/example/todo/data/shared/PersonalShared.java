package com.example.todo.data.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.todo.R;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

// 个人信息存储的管理类
public class PersonalShared {
    public static final String SPDB_NAME = "user_info";                // sharedPreferences文件名称
    public static final String USER_ID = "user_id";                    // 个人ID，全局唯一，自动生成且不可更改
    public static final String USER_OWN_PIC = "user_own_picture";      // 个人头像的base64
    public static final String USER_CHAT_PIC = "user_chat_picture";    // 聊天头像url
    public static final String USER_NAME = "user_name";                // 个人昵称
    public static final String USER_SIGNATURE = "user_signature";      // 个人签名
    // 单例
    private static PersonalShared instance;
    private final SharedPreferences SharedPreferencesDataBase;
    // 缓存
    private Long   cachedUserId;
    private String cachedUserOwnPic;
    private String cachedUserChatPic;
    private String cachedUserName;
    private String cachedUserSignature;

    private PersonalShared(Context context) {
        // 获取SPDB
        this.SharedPreferencesDataBase = context.getSharedPreferences(SPDB_NAME, Context.MODE_PRIVATE);
        // 缓存
        loadCatch(context);
    }

    public static synchronized PersonalShared getInstance(Context context) {
        if (instance == null) {
            instance = new PersonalShared(context.getApplicationContext());
        }
        return instance;
    }

    private void loadCatch(Context context){
        cachedUserId = SharedPreferencesDataBase.getLong(USER_ID, 0);
        if (cachedUserId == 0) {
            cachedUserId = System.currentTimeMillis(); // 根据此刻时间生成唯一
            SharedPreferences.Editor editor = SharedPreferencesDataBase.edit();
            editor.putLong(USER_ID, cachedUserId);
            editor.apply();
        }
        cachedUserOwnPic = SharedPreferencesDataBase.getString(USER_OWN_PIC, getDefaultUserPicBase64(context));
        cachedUserChatPic = SharedPreferencesDataBase.getString(USER_CHAT_PIC, "#000000");
        cachedUserName = SharedPreferencesDataBase.getString(USER_NAME, "Todo");
        cachedUserSignature = SharedPreferencesDataBase.getString(USER_SIGNATURE, "Tencent School Enterprise Joint Project");
    }
    // 获取资源图片的Base64编码
    private String getDefaultUserPicBase64(Context context) {
        try {
            // 获取资源图片的Bitmap对象
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_picture);

            // 将图片转换为字节数组
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // 将字节数组转换为Base64字符串
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // 如果出错，返回null
        }
    }
    public SharedPreferences getSharedPreferencesDataBase(){
        return SharedPreferencesDataBase;
    }

    public long getUserId() {
        return cachedUserId;
    }

    public String getUserOwnPic() {
        return cachedUserOwnPic;
    }

    public String getUserChatPic() {
        return cachedUserChatPic;
    }

    public String getUserName() {
        return cachedUserName;
    }

    public String getUserSignature() {
        return cachedUserSignature;
    }

    // 更新个人头像
    public void updateUserOwnPic(String newUserOwnPic) {
        if (!Objects.equals(newUserOwnPic, cachedUserOwnPic)) {
            cachedUserOwnPic = newUserOwnPic;
            SharedPreferences.Editor editor = SharedPreferencesDataBase.edit();
            editor.putString(USER_OWN_PIC, cachedUserOwnPic);
            editor.apply();
        }
    }

    // 更新聊天头像
    public void updateUserChatPic(String newUserChatPic) {
        if (!Objects.equals(newUserChatPic, cachedUserChatPic)) {
            cachedUserChatPic = newUserChatPic;
            SharedPreferences.Editor editor = SharedPreferencesDataBase.edit();
            editor.putString(USER_CHAT_PIC, cachedUserChatPic);
            editor.apply();
        }
    }

    // 更新个人昵称
    public void updateUserName(String newUserName) {
        if (!Objects.equals(newUserName, cachedUserName)) {
            cachedUserName = newUserName;
            SharedPreferences.Editor editor = SharedPreferencesDataBase.edit();
            editor.putString(USER_NAME, cachedUserName);
            editor.apply();
        }
    }

    // 更新个人签名
    public void updateUserSignature(String newUserSignature) {
        if (!Objects.equals(newUserSignature, cachedUserSignature)) {
            cachedUserSignature = newUserSignature;
            SharedPreferences.Editor editor = SharedPreferencesDataBase.edit();
            editor.putString(USER_SIGNATURE, cachedUserSignature);
            editor.apply();
        }
    }
}
