package com.example.jScanner.ui.splashScreen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.jScanner.R;

public class SplashScreen extends Fragment {

    private final String [] PERMISSIONS = {Manifest.permission.CAMERA};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(getActivity() == null)return;
        AppCompatActivity parActivity = (AppCompatActivity) getActivity();
        for (String permission: PERMISSIONS) {
            if(parActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{permission},1);
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                NavHostFragment.findNavController(SplashScreen.this).navigate(R.id.action_fragment_splashScreen_to_signIn);
            }
        },1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != 1) return;

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
            requireActivity().finish();
    }
}