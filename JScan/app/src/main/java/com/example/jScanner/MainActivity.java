package com.example.jScanner;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.jScanner.utility.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener{

    ProgressDialog mProgressDialog;
    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User.init(this);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.fragment_dashboard,
                R.id.fragment_profile
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        navController.addOnDestinationChangedListener(this);
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        final int destId = destination.getId();
        if(destId == R.id.fragment_dashboard || destId == R.id.fragment_profile){
            showBottomNav();
        } else {
            hideBottomNav();
        }

        if(destId == R.id.fragment_sign_in || destId == R.id.fragment_splashScreen || destId == R.id.forgotPassword || destId == R.id.fragment_sign_up) {
            hideActionBar();
        } else {
            showActionBar();
        }
    }

    private void hideActionBar(){
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
    }

    private void showActionBar(){
        if(getSupportActionBar() != null)
            getSupportActionBar().show();
    }

    private void showBottomNav(){
        navView.setVisibility(View.VISIBLE);
    }

    private void hideBottomNav(){
        navView.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
                || super.onSupportNavigateUp();
    }

    public void showProgressDialog(CharSequence s){
        if(mProgressDialog == null){
            mProgressDialog = ProgressDialog.show(this,"",s);
        }
    }

    public void updateProgressDialog(CharSequence s){
        if(mProgressDialog == null) showProgressDialog(s);
        else{
            mProgressDialog.setMessage(s);
        }
    }

    public void dismissProgressDialog(){
        if(mProgressDialog != null)
            mProgressDialog.dismiss();
        mProgressDialog = null;
    }
}
