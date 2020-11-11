package com.example.jScanner.utility;

import android.graphics.Bitmap;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class Storage {
    private final FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private final static Storage mInstance = new Storage();
    private final static String IMAGE_PREFIX_PATH = "images/";
    private final static String DOCUMENT_PREFIX_PATH = "files/";

    public static void uploadImage(String path, Bitmap bitmap){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bos);
        byte[] bmpArr = bos.toByteArray();

        StorageReference ref = mInstance.mStorage.getReference().child(IMAGE_PREFIX_PATH + path);
        ref.putBytes(bmpArr);
    }

    public static void uploadPDF(String path, byte[] pdf){
        StorageReference ref = mInstance.mStorage.getReference().child(DOCUMENT_PREFIX_PATH + path);
        ref.putBytes(pdf);
    }

}
