package com.example.jScanner.ui.signin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.jScanner.MainActivity;
import com.example.jScanner.R;
import com.example.jScanner.utility.User;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;


public class SignIn extends Fragment implements View.OnClickListener{

    private SignInViewModel mViewModel;
    private SignInButton mGoogleSignInButton;
    private MaterialButton mSignInButton, mBtnSignUpNow, mBtnForgotPassword;
    private TextInputEditText mEmailEditText;
    private TextInputEditText mPasswordEditText;

    private final int GOOGLE_SIGN_IN_REQUEST_CODE = 1000;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        mSignInButton       = view.findViewById(R.id.mdBtnResetPassword);
        mEmailEditText      = view.findViewById(R.id.mdEditText_email);
        mPasswordEditText   = view.findViewById(R.id.mdEditText_password);
        mBtnSignUpNow       = view.findViewById(R.id.mdBtnSignUpNow);
        mGoogleSignInButton = view.findViewById(R.id.btnGoogleSignIn);
        mBtnForgotPassword  = view.findViewById(R.id.mdBtnForgetPassword);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SignInViewModel.class);
        mGoogleSignInButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);
        mBtnSignUpNow.setOnClickListener(this);
        mBtnForgotPassword.setOnClickListener(this);

        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(mEmailEditText.getText() == null)
                    mViewModel.setEmail("");
                else
                    mViewModel.setEmail(mEmailEditText.getText().toString());
            }
        });

        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(mPasswordEditText.getText() == null)
                    mViewModel.setPassword("");
                else
                    mViewModel.setPassword(mPasswordEditText.getText().toString());
            }
        });

        mViewModel.getEmailErrorMessage().observe(getViewLifecycleOwner(), s -> mEmailEditText.setError(s.isEmpty() ? null : s));

        mViewModel.getPasswordErrorMessage().observe(getViewLifecycleOwner(), s -> mPasswordEditText.setError(s.isEmpty() ? null : s));

User.getSignInResult().observe(getViewLifecycleOwner(), statusResultListener -> {
    ((MainActivity) requireActivity()).dismissProgressDialog();

    if(User.getUser() != null && statusResultListener.isSuccess()){
        Toast.makeText(getContext(), R.string.msg_signInSuccess, Toast.LENGTH_SHORT).show();
        NavController navController =  NavHostFragment.findNavController(SignIn.this);
        navController.navigate(R.id.action_fragment_sign_in_to_dashboard);
    } else if(User.getUser() == null && !statusResultListener.isSuccess()) {
        Toast.makeText(getContext(), statusResultListener.getErrorMessage(), Toast.LENGTH_LONG).show();
    }
});
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == mSignInButton.getId()){
            if(mViewModel.validate()) {
                ((MainActivity) requireActivity()).showProgressDialog("Signing in");
                User.signInWithEmailAndPassword(mViewModel.getEmail(), mViewModel.getPassword());
            }
        } else if(v.getId() == mGoogleSignInButton.getId()){
            Intent intent = User.getGoogleSignInIntent();
            startActivityForResult(intent,GOOGLE_SIGN_IN_REQUEST_CODE);
        } else if(v.getId() == mBtnSignUpNow.getId()){
            NavHostFragment.findNavController(this).navigate(R.id.action_fragment_sign_in_to_signUpFragment);
        } else if(v.getId() == mBtnForgotPassword.getId()){
            NavHostFragment.findNavController(this).navigate(R.id.action_fragment_sign_in_to_forgotPassword);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN_IN_REQUEST_CODE){
            ((MainActivity) requireActivity()).showProgressDialog("Signing in");
            User.signInWithGoogle(data);
        }
    }
}