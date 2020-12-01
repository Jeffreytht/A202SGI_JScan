package com.example.jScanner.ui.signup;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.jScanner.MainActivity;
import com.example.jScanner.R;
import com.example.jScanner.utility.User;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpFragment extends Fragment {

    private SignUpViewModel mViewModel;
    private TextInputEditText mEmailEditText, mPasswordEditText, mConfirmEditText;
    private Button mSignUpButton;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_sign_up, container, false);
        mEmailEditText      = view.findViewById(R.id.mdEditText_email);
        mPasswordEditText   = view.findViewById(R.id.mdEditText_password);
        mConfirmEditText    = view.findViewById(R.id.mdEditText_ConfirmPassword);
        mSignUpButton       = view.findViewById(R.id.mdBtnResetPassword);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewModel.validate()) {
                    ((MainActivity) getActivity()).showProgressDialog("");
                    User.signUpNewUser(mViewModel.getEmail(), mViewModel.getPassword());
                }
            }
        });

        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(mEmailEditText.getText() == null){
                    mViewModel.setEmail("");
                } else{
                    mViewModel.setEmail(mEmailEditText.getText().toString());
                }
            }
        });

        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(mPasswordEditText.getText() == null){
                    mViewModel.setPassword("");
                } else {
                    mViewModel.setPassword(mPasswordEditText.getText().toString());
                }
            }
        });

        mConfirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mConfirmEditText.getText() == null){
                    mViewModel.setConfirm("");
                } else {
                    mViewModel.setConfirm(mConfirmEditText.getText().toString());
                }
            }
        });

mViewModel.getErrConfirm().observe(
    getViewLifecycleOwner(),
    error -> mConfirmEditText.setError((error.isEmpty())? null : error)
);

mViewModel.getErrEmail().observe(
    getViewLifecycleOwner(),
    error -> mEmailEditText.setError((error.isEmpty()) ? null : error)
);

mViewModel.getErrPassword().observe(
    getViewLifecycleOwner(),
    error -> mPasswordEditText.setError((error.isEmpty()) ? null : error)
);

User.getSignInResult().observe(getViewLifecycleOwner(), statusResultListener -> {
    ((MainActivity) getActivity()).dismissProgressDialog();
    if(statusResultListener.isSuccess()){
        Toast.makeText(getContext(), R.string.msg_signUpSuccess, Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(SignUpFragment.this).popBackStack();
    } else {
        Toast.makeText(getContext(), statusResultListener.getErrorMessage(), Toast.LENGTH_LONG).show();
    }
});

    }

}