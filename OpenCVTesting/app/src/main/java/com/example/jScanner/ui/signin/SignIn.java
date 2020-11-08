package com.example.jScanner.ui.signin;
import com.example.jScanner.utility.Firebase;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jScanner.R;
import com.google.android.material.textfield.TextInputEditText;

public class SignIn extends Fragment implements View.OnClickListener{

    private SignInViewModel mViewModel;
    private SignInButton mGoogleSignInButton;
    private MaterialButton mSignInButton;
    private TextInputEditText mEmailEditText;
    private TextInputEditText mPasswordEditText;

    private final int GOOGLE_SIGN_IN_REQUEST_CODE = 1000;

    public static SignIn newInstance() {
        return new SignIn();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        mSignInButton       = view.findViewById(R.id.mdBtnSignIn);
        mEmailEditText      = view.findViewById(R.id.mdEditText_email);
        mPasswordEditText   = view.findViewById(R.id.mdEditText_password);
        mGoogleSignInButton = view.findViewById(R.id.btnGoogleSignIn);
        mGoogleSignInButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SignInViewModel.class);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == mSignInButton.getId()){
            Firebase.signUpNewUser(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
        } else if(v.getId() == mGoogleSignInButton.getId()){
            Intent intent = Firebase.getGoogleSignInIntent();
            startActivityForResult(intent,GOOGLE_SIGN_IN_REQUEST_CODE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SIGN_IN_REQUEST_CODE){
            Firebase.signInWithGoogle(data);
        }
    }


}