package com.example.jScanner.ui.documentScanner.document_reader;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;

import java.util.HashMap;
import java.util.List;

// Asynchronous Task for image processing. To improve the performance of app and prevent main Ui thread freeze.
public class ImagePreComputation extends AsyncTask<ScannedDocument, Integer, Void> {

    private final ScannedImageFinishPreComputeCallback mCallback;
    private final List<ScannedImage> mScannedImageList;
    private final HashMap<ScannedImage, Bitmap[]> mScannedImageBuffer;

    public ImagePreComputation(List<ScannedImage> scannedImageList, ScannedImageFinishPreComputeCallback callback, HashMap<ScannedImage, Bitmap[]> scannedImageBuffer) {
        this.mCallback = callback;
        this.mScannedImageList = scannedImageList;
        this.mScannedImageBuffer = scannedImageBuffer;
    }

    @Override
    protected Void doInBackground(ScannedDocument... scannedDocuments) {
        int totalImage = mScannedImageList.size();

        for (int imageIdx = 0; imageIdx < totalImage; imageIdx++) {
            ScannedImage si = mScannedImageList.get(imageIdx);
            mScannedImageBuffer.put(si, si.getFilteredBitmaps());
            mCallback.refreshUi(imageIdx + 1, totalImage);
        }
        return null;
    }
}
