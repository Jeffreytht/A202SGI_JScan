package com.example.jScanner.ui.signin;
import com.example.jScanner.Callback.SignInResult;
import com.example.jScanner.utility.User;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.button.MaterialButton;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.jScanner.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Optional;

public class SignIn extends Fragment implements View.OnClickListener{

    private SignInViewModel mViewModel;
    private SignInButton mGoogleSignInButton;
    private MaterialButton mSignInButton;
    private TextInputEditText mEmailEditText;
    private TextInputEditText mPasswordEditText;

    private final int GOOGLE_SIGN_IN_REQUEST_CODE = 1000;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        mSignInButton       = view.findViewById(R.id.mdBtnSignIn);
        mEmailEditText      = view.findViewById(R.id.mdEditText_email);
        mPasswordEditText   = view.findViewById(R.id.mdEditText_password);
        mGoogleSignInButton = view.findViewById(R.id.btnGoogleSignIn);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SignInViewModel.class);
        mGoogleSignInButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);

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

        mViewModel.getEmailErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mEmailEditText.setError(s.isEmpty() ? null : s);
            }
        });

        mViewModel.getPasswordErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mPasswordEditText.setError(s.isEmpty() ? null : s);
            }
        });

        User.getSignInResult().observe(getViewLifecycleOwner(), new Observer<SignInResult>() {
            @Override
            public void onChanged(SignInResult signInResult) {
                if(signInResult.isSuccess()){
                    Toast.makeText(getContext(), "Sign in successfully", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(SignIn.this).navigate(R.id.action_signIn_to_fragment_scanner);
                } else {
                    Toast.makeText(getContext(), signInResult.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == mSignInButton.getId()){
            if(mViewModel.validate())
                User.signInWithEmailAndPassword(mViewModel.getEmail(), mViewModel.getPassword());

        } else if(v.getId() == mGoogleSignInButton.getId()){
            Intent intent = User.getGoogleSignInIntent();
            startActivityForResult(intent,GOOGLE_SIGN_IN_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN_IN_REQUEST_CODE){
            User.signInWithGoogle(data);
        }
    }


}