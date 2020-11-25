package com.example.jScanner.ui.profile;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.jScanner.R;
import com.example.jScanner.utility.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FragmentProfile extends Fragment implements View.OnClickListener {

    private ImageView iv_profileImage;
    private TextView tv_email, tv_username;
    private Button btn_logout, btnResetPassword;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        iv_profileImage  = view.findViewById(R.id.iv_profile_image);
        tv_username      = view.findViewById(R.id.tv_username);
        tv_email         = view.findViewById(R.id.tv_email);
        btn_logout       = view.findViewById(R.id.mdBtnLogout);
        btnResetPassword = view.findViewById(R.id.mdBtnResetPassword);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        btnResetPassword.setVisibility(User.isGoogleProvider() ? View.GONE : View.VISIBLE);
        btnResetPassword.setOnClickListener(this);
        btn_logout      .setOnClickListener(this);
        tv_email.setText(User.getEmail());
        tv_username.setText(User.getProfileName());
        Glide.with(this).load(User.getHDProfileImage()).into(iv_profileImage);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btn_logout.getId()) {
            User.signOut();
            NavHostFragment.findNavController(this).navigate(R.id.action_fragment_profile_to_fragment_sign_in);
        } else if(v.getId() == btnResetPassword.getId()){
            User.resetPasswordWithEmail(User.getEmail());
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Reset password")
                    .setMessage("Your password reset email has been sent to " + User.getEmail())
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }
}