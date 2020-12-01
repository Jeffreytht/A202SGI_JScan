package com.example.jScanner.ui.forgotPassword;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jScanner.R;
import com.example.jScanner.utility.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

        mButtonResetPassword.setOnClickListener(v -> {
            if(mViewModel.validate()) {
                User.resetPasswordWithEmail(mViewModel.getEmail());
            }
        });

        mViewModel.getErrEmail().observe(getViewLifecycleOwner(), s -> mEditTextEmail.setError((s.isEmpty() )? null : s));

        User.getSignInResult().observe(getViewLifecycleOwner(),
                statusResultListener -> new MaterialAlertDialogBuilder(requireContext())
                        . setTitle("Reset password")
                        . setMessage(statusResultListener.getErrorMessage())
                        . setPositiveButton("Ok", (dialog, which)-> dialog.dismiss())
                        . show());
    }

}