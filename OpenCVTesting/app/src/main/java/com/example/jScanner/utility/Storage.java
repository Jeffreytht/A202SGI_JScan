package com.example.jScanner.utility;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.jScanner.Callback.CommonResultListener;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

public class Storage {
    private final FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private final static Storage mInstance = new Storage();
    private final static String IMAGE_PREFIX_PATH = "images/";
    private final static String DOCUMENT_PREFIX_PATH = "files/";

    public static void uploadImage(@NonNull String path, @NonNull Bitmap bitmap, @Nullable CommonResultListener<Boolean> uploadImageListener) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        StorageReference ref = mInstance.mStorage.getReference().child(IMAGE_PREFIX_PATH + path);
        UploadTask task = ref.putBytes(bos.toByteArray());
        if(uploadImageListener != null)
            task.addOnCompleteListener(result -> uploadImageListener.onResultReceived(true));
    }

    public static void uploadImages(@Nonnull ScannedDocument scannedDocument, @NonNull CommonResultListener<Boolean> uploadImagesListener) {
        final List<ScannedImage> scannedImageLinkedList = scannedDocument.getScannedImageList();
        int seq = 1;

        CommonResultListener<Boolean> uploadImageListener = new CommonResultListener<Boolean>() {
            private final int mTotalImages = scannedImageLinkedList.size();
            private int mCurrTotalImages = 0;

            @Override
            public void onResultReceived(Boolean result) {
                mCurrTotalImages++;

                if(mCurrTotalImages >= mTotalImages){
                    uploadImagesListener.onResultReceived(true);
                }
            }
        };

        uploadImage(scannedDocument.getId() + "/cover.bmp", scannedImageLinkedList.get(0).getFinalImage(), uploadImageListener);

        for (ScannedImage si : scannedImageLinkedList) {
            Storage.uploadImage(scannedDocument.getId() + "/" + seq + ".bmp", si.getOriImage(), uploadImageListener);
            seq++;
        }
    }

    public static void getImage(@NonNull String path, @NonNull final CommonResultListener<Task<Uri>> commonResultListener) {
        StorageReference ref = mInstance.mStorage.getReference().child(IMAGE_PREFIX_PATH + path);
        ref.getDownloadUrl().addOnCompleteListener(commonResultListener::onResultReceived);
    }

    public static void getPDFUri(@NonNull String path, @NonNull final CommonResultListener<Uri> commonResultListener){
        StorageReference ref = mInstance.mStorage.getReference().child(DOCUMENT_PREFIX_PATH + path + ".pdf");
        ref.getDownloadUrl().continueWith(result ->{
            commonResultListener.onResultReceived(result.getResult());
            return null;
        });
    }

    public static void uploadPDF(@NonNull String path, @NonNull byte[] pdf) {
        try {
            StorageReference ref = mInstance.mStorage.getReference().child(DOCUMENT_PREFIX_PATH + path);
            ref.putBytes(pdf);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void downloadImage(@NonNull String path, @NonNull final CommonResultListener<byte[]> commonResultListener) {
        StorageReference ref = mInstance.mStorage.getReference().child(IMAGE_PREFIX_PATH + path);
        final long ONE_MEGABYTE = 1024 * 1024;
        ref.getBytes(10 * ONE_MEGABYTE).addOnCompleteListener(result ->
                commonResultListener.onResultReceived(result.isSuccessful() ? result.getResult() : null)
        );
    }

    public static void downloadPDF(@NonNull ScannedDocument scannedDocument, @NonNull CommonResultListener<File> listener) {
        try {
            File localFile = File.createTempFile("document", ".pdf");
            StorageReference ref = mInstance.mStorage.getReference().child(DOCUMENT_PREFIX_PATH + scannedDocument.getId() + ".pdf");
            ref.getFile(localFile).addOnSuccessListener(success -> listener.onResultReceived(localFile)).addOnFailureListener(failure -> listener.onResultReceived(null));
        } catch (IOException ex) {
            listener.onResultReceived(null);
        }
    }
}
