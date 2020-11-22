package com.example.jScanner.utility;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.jScanner.Callback.CommonResultListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class Storage {
    private final FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private final static Storage mInstance = new Storage();
    private final static String IMAGE_PREFIX_PATH       = "images/";
    private final static String DOCUMENT_PREFIX_PATH    = "files/";

    public static void uploadImage(@NonNull String path, @NonNull Bitmap bitmap){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bos);
        StorageReference ref = mInstance.mStorage.getReference().child(IMAGE_PREFIX_PATH + path);
        ref.putBytes(bos.toByteArray());
    }

    public static void getImage(@NonNull String path, @NonNull final CommonResultListener<Task<Uri>> commonResultListener){
        StorageReference ref = mInstance.mStorage.getReference().child(IMAGE_PREFIX_PATH + path);
        ref.getDownloadUrl().addOnCompleteListener(commonResultListener::onResultReceived);
    }

    public static void uploadPDF(@NonNull String path, @NonNull byte[] pdf){
        StorageReference ref = mInstance.mStorage.getReference().child(DOCUMENT_PREFIX_PATH + path);
        ref.putBytes(pdf);
    }

}
