package com.example.jScanner.ui.signin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jScanner.R;

public class SignInViewModel extends AndroidViewModel {
    private String mEmail = "";
    private String mPassword = "";
    private final MutableLiveData<String> mEmailErrorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> mPasswordErrorMessage = new MutableLiveData<>();

    public SignInViewModel(@NonNull Application application) {
        super(application);
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
        validateEmail();
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
        validatePassword();
    }

    public boolean validateEmail(){
        if(mEmail.isEmpty()){
            mEmailErrorMessage.setValue(getApplication().getResources().getString(R.string.error_email_empty));
            return false;
        } else {
            mEmailErrorMessage.setValue("");
            return true;
        }
    }

    public boolean validatePassword(){
        if(mPassword.isEmpty()){
            mPasswordErrorMessage.setValue(getApplication().getResources().getString(R.string.error_password_empty));
            return false;
        } else {
            mPasswordErrorMessage.setValue("");
            return true;
        }
    }

    public LiveData<String> getEmailErrorMessage(){
        return this.mEmailErrorMessage;
    }

    public LiveData<String> getPasswordErrorMessage(){
        return this.mPasswordErrorMessage;
    }

    public boolean validate(){
        return  validateEmail() && validatePassword();
    }
}