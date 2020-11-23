package com.example.jScanner.ui.forgotPassword;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.jScanner.Callback.StatusResultListener;
import com.example.jScanner.R;
import com.example.jScanner.utility.User;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPassword extends Fragment {

    private ForgotPasswordViewModel mViewModel;
    private Button mButtonResetPassword;
    private TextInputEditText mEditTextEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        mButtonResetPassword = view.findViewById(R.id.mdBtnResetPassword);
        mEditTextEmail       = view.findViewById(R.id.mdEditText_email);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);
        mEditTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mEditTextEmail.getText() != null)
                    mViewModel.setEmail(mEditTextEmail.getText().toString());
                else
                    mViewModel.setEmail("");
            }
        });

        mButtonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewModel.validate()) {
                    User.resetPasswordWithEmail(mViewModel.getEmail());
                }
            }
        });

        mViewModel.getErrEmail().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mEditTextEmail.setError((s.isEmpty() )? null : s);
            }
        });

        User.getSignInResult().observe(getViewLifecycleOwner(), new Observer<StatusResultListener>() {
            @Override
            public void onChanged(StatusResultListener statusResultListener) {
                Toast.makeText(getContext(), statusResultListener.getErrorMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

}