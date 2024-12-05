package com.example.todo.data.model;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todo.data.repository.PersonalSharedRepository;

public class PersonalSharedViewModel extends AndroidViewModel {

    private final PersonalSharedRepository personalSharedRepository;

    public PersonalSharedViewModel(Application application) {
        super(application);
        personalSharedRepository = new PersonalSharedRepository(application.getApplicationContext());
    }

    // 获取个人头像的 LiveData
    public LiveData<String> getUserOwnPicLiveData() {
        return personalSharedRepository.getUserOwnPicLiveData();
    }

    // 获取聊天头像的 LiveData
    public LiveData<String> getUserChatPicLiveData() {
        return personalSharedRepository.getUserChatPicLiveData();
    }

    // 获取用户名的 LiveData
    public LiveData<String> getUserNameLiveData() {
        return personalSharedRepository.getUserNameLiveData();
    }

    // 获取个人签名的 LiveData
    public LiveData<String> getUserSignatureLiveData() {
        return personalSharedRepository.getUserSignatureLiveData();
    }

    // 更新用户名
    public void updateUserName(String newUserName) {
        personalSharedRepository.updateUserName(newUserName);
    }

    // 更新个人签名
    public void updateUserSignature(String newUserSignature) {
        personalSharedRepository.updateUserSignature(newUserSignature);
    }

    // 更新个人头像
    public void updateUserOwnPic(String newUserOwnPic) {
        personalSharedRepository.updateUserOwnPic(newUserOwnPic);
    }

    // 更新聊天头像
    public void updateUserChatPic(String newUserChatPic) {
        personalSharedRepository.updateUserChatPic(newUserChatPic);
    }
}
