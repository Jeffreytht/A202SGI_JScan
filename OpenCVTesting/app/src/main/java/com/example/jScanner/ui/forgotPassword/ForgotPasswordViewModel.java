package com.example.jScanner.ui.forgotPassword;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jScanner.R;

public class ForgotPasswordViewModel extends AndroidViewModel {
    private String mEmail;

    private MutableLiveData<String> errEmail = new MutableLiveData<>();

    public ForgotPasswordViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getErrEmail() {return errEmail;}

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
        validate();
    }

    public boolean validate(){
        if(mEmail.isEmpty()){
            errEmail.setValue(getApplication().getResources().getString(R.string.error_email_empty));
        } else {
            errEmail.setValue("");
        }

        return !mEmail.isEmpty();
    }
}