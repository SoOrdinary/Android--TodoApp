package com.example.todo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todo.data.shared.PersonalShared;

public class PersonalSharedRepository {

    private final PersonalShared personalShared;
    private final MutableLiveData<String> userNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userSignatureLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userOwnPicLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userChatPicLiveData = new MutableLiveData<>();

    public PersonalSharedRepository(Context context) {
        // 获取 PersonalShared 实例
        personalShared = PersonalShared.getInstance(context);
        // 初始化 LiveData 值
        userNameLiveData.setValue(personalShared.getUserName());
        userSignatureLiveData.setValue(personalShared.getUserSignature());
        userOwnPicLiveData.setValue(personalShared.getUserOwnPic());
        userChatPicLiveData.setValue(personalShared.getUserChatPic());
    }

    // 获取用户名的 LiveData，用于 UI 更新
    public LiveData<String> getUserNameLiveData() {
        return userNameLiveData;
    }

    // 获取个人签名的 LiveData，用于 UI 更新
    public LiveData<String> getUserSignatureLiveData() {
        return userSignatureLiveData;
    }

    // 获取个人头像的 LiveData，用于 UI 更新
    public LiveData<String> getUserOwnPicLiveData() {
        return userOwnPicLiveData;
    }

    // 获取聊天头像的 LiveData，用于 UI 更新
    public LiveData<String> getUserChatPicLiveData() {
        return userChatPicLiveData;
    }

    // 更新用户名
    public void updateUserName(String newUserName) {
        personalShared.updateUserName(newUserName);
        userNameLiveData.setValue(newUserName); // 更新 LiveData，通知UI更新
    }

    // 更新个人签名
    public void updateUserSignature(String newUserSignature) {
        personalShared.updateUserSignature(newUserSignature);
        userSignatureLiveData.setValue(newUserSignature); // 更新 LiveData
    }

    // 更新个人头像
    public void updateUserOwnPic(String newUserOwnPic) {
        personalShared.updateUserOwnPic(newUserOwnPic);
        userOwnPicLiveData.setValue(newUserOwnPic); // 更新 LiveData
    }

    // 更新聊天头像
    public void updateUserChatPic(String newUserChatPic) {
        personalShared.updateUserChatPic(newUserChatPic);
        userChatPicLiveData.setValue(newUserChatPic); // 更新 LiveData
    }

}
