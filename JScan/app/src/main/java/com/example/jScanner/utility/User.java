package com.example.jScanner.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jScanner.Callback.StatusResultListener;
import com.example.jScanner.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.List;
import java.util.Objects;


public class User implements OnCompleteListener<AuthResult>{

    private static User instance;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private GoogleSignInClient mSignInClient;
    private final MutableLiveData<StatusResultListener> mSignInResult = new MutableLiveData<>();

    public static void init(@NonNull Context context){
        instance = new User();
        FirebaseApp.initializeApp(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        instance.mSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public static void signInWithEmailAndPassword(@NonNull String email, @NonNull String password){
        instance.mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(instance);
    }

    public static void resetPasswordWithEmail(@NonNull String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            final boolean isSignInSuccess = task.isSuccessful();
            final String message;

            if (!task.isSuccessful()) {
                message = Objects.requireNonNull(task.getException()).getMessage();
            } else {
                message = "An email has been sent to your email account. Kindly check your email to reset the password.";
            }

            instance.mSignInResult.setValue(new StatusResultListener() {
                @Override
                public boolean isSuccess() {
                    return isSignInSuccess;
                }

                @Override
                public String getErrorMessage() {
                    return message;
                }
            });
        });
    }

    public static void signUpNewUser(@NonNull String email, @NonNull String password){
        instance.mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(instance);
    }

    @NonNull
    public static LiveData<StatusResultListener> getSignInResult(){
        return instance.mSignInResult;
    }

    public static void signOut(){
        instance.mAuth.signOut();
        instance.mSignInClient.signOut();
        instance.mSignInResult.setValue(new StatusResultListener() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getErrorMessage() {
                return "Log out successfully";
            }
        });
    }

    @Nullable
    public static Uri getHDProfileImage(){
        Uri profile = getProfileImage();
        if(profile == null) return null;

        String url = getProfileImage().toString();
        if(Objects.requireNonNull(getUser()).getProviderId().contains("google"))
            return Uri.parse(url.replace("s96-c", "s400-c"));
        return profile;
    }

    @Nullable
    public static Uri getProfileImage(){
        return getUser() == null ? null : getUser().getPhotoUrl();
    }


    public static void signInWithGoogle(@NonNull Intent data){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(Objects.requireNonNull(account).getIdToken(), null);
            instance.mAuth.signInWithCredential(credential).addOnCompleteListener(instance);
        } catch (final ApiException e) {
            instance.mSignInResult.setValue(new StatusResultListener() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getErrorMessage() {
                    return e.getMessage();
                }
            });
        }
    }

    public static boolean isGoogleProvider(){
        List<? extends UserInfo> userInfoList = getUser().getProviderData();

        for(UserInfo userInfo: userInfoList)
            if (userInfo.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) return true;
        return false;
    }

    @NonNull
    public static Intent getGoogleSignInIntent(){
        return instance.mSignInClient.getSignInIntent();
    }

    @Nullable
    public static FirebaseUser getUser(){
        return instance.mAuth.getCurrentUser();
    }

    @NonNull
    public static String getEmail(){
        return getUser() == null ? "" : Objects.requireNonNull(getUser().getEmail());
    }

    @NonNull
    public static String getProfileName(){
        return getUser() == null ? "" : (getUser().getDisplayName()) == null ? "" : getUser().getDisplayName();
    }


    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {

        final boolean isSignInSuccess = task.isSuccessful();
        final String errorMessage;

        if (task.isSuccessful()) {
            errorMessage = "";
            Database.insertNewUser(Objects.requireNonNull(User.getUser()));
        } else if(task.getException() != null){
            errorMessage = task.getException().getMessage();
        } else {
            errorMessage = "";
        }

        mSignInResult.setValue(new StatusResultListener() {
            @Override
            public boolean isSuccess() {
                return isSignInSuccess;
            }

            @Override
            public String getErrorMessage() {
                return errorMessage;
            }
        });
    }
}
