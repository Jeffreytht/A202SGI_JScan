package com.example.jScanner.ui.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import com.example.jScanner.Callback.CommonResultListener;

import org.opencv.android.JavaCameraView;


public class ScannerCameraView extends JavaCameraView implements android.hardware.Camera.PictureCallback {
    private static final String TAG = "JavaCameraView";
    private CommonResultListener<Bitmap> mCallBack = null;

    public ScannerCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void AllocateCache() {
        Log.d(TAG, "Allocating cache");
        super.AllocateCache();
    }

    public void takePicture() {
        takePicture(null);
    }

    public void takePicture(CommonResultListener<Bitmap> cameraViewCallback) {
        Log.i(TAG, "Taking picture");
        if (cameraViewCallback != null)
            this.mCallBack = cameraViewCallback;

        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        if (mCallBack != null)
            mCallBack.onResultReceived(BitmapFactory.decodeByteArray(data, 0, data.length));
    }
}
