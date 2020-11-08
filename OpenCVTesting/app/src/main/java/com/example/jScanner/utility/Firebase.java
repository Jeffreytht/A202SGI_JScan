package com.example.jScanner.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

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

public class Firebase extends Activity {

    private static Firebase instance;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private GoogleSignInClient mSignInClient;

    public static void init(Context context){
        instance = new Firebase();
        FirebaseApp.initializeApp(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        instance.mSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public static void signUpNewUser(String email, String password){
        instance.mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("DEBUGGING", "createUserWithEmail:success");
                    FirebaseUser user = instance.mAuth.getCurrentUser();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("DEBUGGING", "createUserWithEmail:failure" + task.getException().getMessage());
                }
            }
        });
    }

    public static void signInWithGoogle(Intent data){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d("DEBUGGING", "firebaseAuthWithGoogle:" + account.getId());

            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            instance.mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("DEBUGGING", "signInWithCredential:success");
                                FirebaseUser user = instance.mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.d("DEBUGGING", "signInWithCredential:failure", task.getException());
                            }
                        }
                    });

        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Log.d("DEBUGGING", "Google sign in failed", e);
        }
    }

    public static Intent getGoogleSignInIntent(){
        return instance.mSignInClient.getSignInIntent();
    }


}
