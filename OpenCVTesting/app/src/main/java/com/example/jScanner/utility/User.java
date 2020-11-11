package com.example.jScanner.utility;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jScanner.Callback.SignInResult;
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



public class User implements OnCompleteListener<AuthResult>{

    private static User instance;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private GoogleSignInClient mSignInClient;
    private final MutableLiveData<SignInResult> mSignInResult = new MutableLiveData<>();

    public static void init(Context context){
        instance = new User();
        FirebaseApp.initializeApp(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        instance.mSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public static void signInWithEmailAndPassword(String email, String password){
        instance.mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(instance);
    }

    public static void resetPasswordWithEmail(String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final boolean isSignInSuccess = task.isSuccessful();
                final String message;

                if (!task.isSuccessful()) {
                    message = task.getException().getMessage();
                } else {
                    message = "An email has been sent to your email account. Kindly check your email to reset the password.";
                }

                instance.mSignInResult.setValue(new SignInResult() {
                    @Override
                    public boolean isSuccess() {
                        return isSignInSuccess;
                    }

                    @Override
                    public String getErrorMessage() {
                        return message;
                    }
                });
            }
        });
    }

    public static void signUpNewUser( String email, String password){
        instance.mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(instance);
    }

    public static LiveData<SignInResult> getSignInResult(){
        return instance.mSignInResult;
    }


    public static void signInWithGoogle(Intent data){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            instance.mAuth.signInWithCredential(credential).addOnCompleteListener(instance);
        } catch (final ApiException e) {
            instance.mSignInResult.setValue(new SignInResult() {
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

    public static Intent getGoogleSignInIntent(){
        return instance.mSignInClient.getSignInIntent();
    }

    public static FirebaseUser getUser(){
        return instance.mAuth.getCurrentUser();
    }


    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {

        final boolean isSignInSuccess = task.isSuccessful();
        final String errorMessage;

        if (task.isSuccessful()) {
            errorMessage = "";
            Database.insertNewUser(User.getUser());
        } else if(task.getException() != null){
            errorMessage = task.getException().getMessage();
        } else {
            errorMessage = "";
        }

        mSignInResult.setValue(new SignInResult() {
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
