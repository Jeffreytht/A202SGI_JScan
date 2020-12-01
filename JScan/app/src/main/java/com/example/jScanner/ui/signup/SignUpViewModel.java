package com.example.jScanner.ui.signup;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jScanner.R;

public class SignUpViewModel extends AndroidViewModel {
    private String mEmail;
    private String mPassword;
    private String mConfirm;

    private final MutableLiveData<String>errorEmail = new MutableLiveData<>();
    private final MutableLiveData<String>errorPassword = new MutableLiveData<>();
    private final MutableLiveData<String>errorConfirm = new MutableLiveData<>();

    public SignUpViewModel(@NonNull Application application) {
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

    public void setConfirm(String confirm) {
        this.mConfirm = confirm;
        errorConfirm.setValue("");
    }

    public LiveData<String> getErrEmail(){
        return this.errorEmail;
    }

    public LiveData<String> getErrPassword(){
        return this.errorPassword;
    }

    public LiveData<String> getErrConfirm(){
        return this.errorConfirm;
    }

    private boolean validateEmail(){
        if(mEmail.isEmpty()){
            errorEmail.setValue(getApplication().getResources().getString(R.string.error_email_empty));
            return false;
        } else{
            errorEmail.setValue("");
            return true;
        }
    }

    private boolean validatePassword(){
        if(mPassword.isEmpty()){
            errorPassword.setValue(getApplication().getResources().getString(R.string.error_password_empty));
            return false;
        } else {
            errorPassword.setValue("");
            return true;
        }
    }

    private boolean validateConfirm(){
        if(mConfirm.isEmpty()) {
            errorConfirm.setValue(getApplication().getResources().getString(R.string.error_confirm_empty));
            return false;
        }

        if(mPassword.equals(mConfirm)){
            errorConfirm.setValue("");
            return true;
        } else {
            errorConfirm.setValue(getApplication().getResources().getString(R.string.error_confirm_not_match));
            return false;
        }
    }

    public boolean validate(){
        return validateEmail() && validatePassword() && validateConfirm();
    }
}