package com.example.jScanner.ui.documentScanner.document_reader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.ui.documentScanner.image_contour_selector.ImageContourSelectorViewModel;
import com.example.jScanner.utility.ImageProcessing;
import com.example.jScanner.utility.PDFBuilder;

import org.opencv.core.Point;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

// Callback from async to insert data
interface ScannedImagePreComputation {
    void updateImageBuffer(int scannedImageId, Bitmap[] bitmaps);
}


// Callback to update ui
interface ScannedImageFinishPreComputeCallback {
    void refreshUi(int curr, int total);
}


public class DocumentReaderViewModel extends ViewModel {

    // Asynchronous Task for image processing. To improve the performance of app and prevent main Ui thread freeze.
    private class ImagePreComputation extends AsyncTask<ScannedDocument, Integer, Void> {

        private final ScannedImageFinishPreComputeCallback mCallback;
        private final List<ScannedImage> mScannedImageList;

        public ImagePreComputation(List<ScannedImage> scannedImageList, ScannedImageFinishPreComputeCallback callback) {
            this.mCallback = callback;
            this.mScannedImageList = scannedImageList;
        }

        @Override
        protected Void doInBackground(ScannedDocument... scannedDocuments) {
            int totalImage = mScannedImageList.size();

            for (int imageIdx = 0; imageIdx < totalImage; imageIdx++) {
                ScannedImage si = mScannedImageList.get(imageIdx);
                mScannedImageBuffer.put(si.getId(), si.getAllFilterBitmap());
                mCallback.refreshUi(imageIdx + 1, totalImage);
            }
            return null;
        }
    }

    private final MutableLiveData<ScannedDocument> mScannedDocument;
    private final HashMap<Integer, Bitmap[]> mScannedImageBuffer = new HashMap<>();
    private ImagePreComputation mImagePreComputationAsync;

    //Buffer
    private int mViewPagerCurrentIndex;

    public DocumentReaderViewModel() {
        this.mScannedDocument = new MutableLiveData<>(new ScannedDocument());
        this.mScannedDocument.setValue(new ScannedDocument());
        mViewPagerCurrentIndex = 0;
    }

    public void initViewModel(Bundle bundle, ScannedImageFinishPreComputeCallback callback) {
        DocumentReaderFragmentArgs args = DocumentReaderFragmentArgs.fromBundle(bundle);
        mScannedDocument.setValue(args.getScannedDocument());
        mImagePreComputationAsync = new ImagePreComputation(args.getScannedDocument().getScannedImageList(), callback);
        mImagePreComputationAsync.execute();
    }

    public int getViewPagerCurrentIndex() {
        return mViewPagerCurrentIndex;
    }

    LiveData<ScannedDocument> getScannedDocument() {
        return mScannedDocument;
    }

    public void setDocumentName(String documentName){
        mScannedDocument.getValue().setName(documentName);
    }

    public boolean isDocumentNameSet(){
        String name = mScannedDocument.getValue().getName();
        return name != null && !name.isEmpty();
    }

    public String getDocumentName(){
        return mScannedDocument.getValue().getName();
    }

    public void rotateBitmapNContour(int index, int degree) {
        if (mScannedDocument.getValue() == null)
            return;

        ScannedImage scannedImage = mScannedDocument.getValue().getScannedImageList().get(index);
        Bitmap []bufferImage = mScannedImageBuffer.get(scannedImage.getId());
        Bitmap[] bmpArr = {scannedImage.getOriImage(), bufferImage[0], bufferImage[1], bufferImage[2]};
        ImageProcessing.rotateBitmapNContour(bmpArr, scannedImage.getContour(), degree);
        mScannedImageBuffer.put(scannedImage.getId(), new Bitmap[]{bmpArr[1], bmpArr[2], bmpArr[3]} );
        scannedImage.setOriImage(bmpArr[0]);
    }

    public void stopAsync() {
        if (mImagePreComputationAsync.getStatus() == AsyncTask.Status.RUNNING)
            mImagePreComputationAsync.cancel(true);
    }

    public byte[] createPDF(String path, String fileName){
        return PDFBuilder.PDFToBytes(mScannedDocument.getValue().getScannedImageList());
    }

    public void createPDF(String path, String fileName, byte[] bytes){

        if(!fileName.endsWith(".pdf")) fileName += ".pdf";
        final File file = new File(path, fileName);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setViewPagerCurrentIndex(int viewPagerCurrentIndex) {
        mViewPagerCurrentIndex = viewPagerCurrentIndex;
    }

    ScannedImage getCurrentSelectedImage() {
        return mScannedDocument.getValue().getScannedImageList().get(mViewPagerCurrentIndex);
    }


    public int getNumOfScannedImage(){
        return (mScannedDocument.getValue() != null) ? mScannedDocument.getValue().getScannedImageList().size() : 0;
    }


    Bitmap[] getCurrentSelectedFilteredImage() {
        if (mScannedDocument.getValue() == null) return null;
        int scannedDocumentId = mScannedDocument.getValue().getScannedImageList().get(getViewPagerCurrentIndex()).getId();
        if (mScannedImageBuffer.containsKey(scannedDocumentId))
            return mScannedImageBuffer.get(scannedDocumentId);
        return null;
    }

    void removeScannedImage(ScannedImage image){
        if(mScannedDocument.getValue() != null) {
            mScannedDocument.getValue().getScannedImageList().remove(image);
            mScannedImageBuffer.remove(image.getId());
        }
    }

    void setBackStackEntry(final NavController navController, LifecycleOwner owner) {
        NavBackStackEntry backStack = navController.getCurrentBackStackEntry();
        if (backStack != null) {

            final SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();
            final Bitmap oriBitmapLiveData = savedStateHandle.get(ImageContourSelectorViewModel.TAG_ORIGINAL_BITMAP);
            final Point[] contourLiveData = savedStateHandle.get(ImageContourSelectorViewModel.TAG_CONTOUR);
            final MutableLiveData<Integer> flagLiveData = savedStateHandle.getLiveData(ImageContourSelectorViewModel.TAG_FLAG_ADD_TO_DOCUMENT);

            flagLiveData.observe(owner, new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    if (mScannedDocument.getValue() != null) {
                        if (integer == ImageContourSelectorViewModel.ADD_IMAGE) {
                            ScannedImage scannedImage = mScannedDocument.getValue().getScannedImageList().get(mViewPagerCurrentIndex);
                            scannedImage.setOriImage(oriBitmapLiveData);
                            scannedImage.setContour(contourLiveData);
                            mScannedImageBuffer.put(scannedImage.getId(), scannedImage.getAllFilterBitmap());
                            clearBuffer(navController);
                        } else if (integer == ImageContourSelectorViewModel.DISCARD_IMAGE) {
                            clearBuffer(navController);
                        }
                    }
                }
            });
        }
    }

    void clearBuffer(NavController navController) {
        NavBackStackEntry backStack = navController.getCurrentBackStackEntry();
        if (backStack != null) {
            final SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_ORIGINAL_BITMAP);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_EDITED_BITMAP);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_CONTOUR);
            savedStateHandle.remove(ImageContourSelectorViewModel.TAG_FLAG_ADD_TO_DOCUMENT);
        }
    }

}