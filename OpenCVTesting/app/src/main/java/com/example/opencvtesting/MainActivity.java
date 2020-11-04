package com.example.opencvtesting;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener{

    private NavHostFragment mFragement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.fragment_scanner, R.id.fragment_navigation_image_contour_selector, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
       // NavigationUI.setupWithNavController(navView, navController);
        navController.addOnDestinationChangedListener(this);

    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        final int destId = destination.getId();
        if(destId ==  R.id.fragment_splashScreen)
            hideActionBar();
        else if(destId == R.id.fragment_documentReader)
            showActionBar();
        else if(destId == R.id.fragment_navigation_image_contour_selector)
            showActionBar();
        else if(destId == R.id.fragment_scanner)
            showActionBar();
    }

    private void hideActionBar(){
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
    }

    private void showActionBar(){
        if(getSupportActionBar() != null)
            getSupportActionBar().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
                || super.onSupportNavigateUp();

    }
}
