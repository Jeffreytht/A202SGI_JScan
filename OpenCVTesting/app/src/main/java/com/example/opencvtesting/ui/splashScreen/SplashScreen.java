package com.example.opencvtesting.ui.splashScreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.opencvtesting.R;

public class SplashScreen extends Fragment {

    private final String [] PERMISSIONS = {Manifest.permission.CAMERA};
    public static SplashScreen newInstance() {
        return new SplashScreen();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash_screen_fragment, container, false);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission: PERMISSIONS) {
                if(parActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{permission},1);
                }
            }

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NavHostFragment.findNavController(SplashScreen.this).navigate(R.id.action_splashScreen_to_navigation_scanner);
            }
        },1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != 1) return;

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
            getActivity().finish();
    }
}