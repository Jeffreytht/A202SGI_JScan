package com.example.opencvtesting.ui.documentScanner.scanner;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opencvtesting.Callback.CameraViewCallback;
import com.example.opencvtesting.Model.ScannedDocument;
import com.example.opencvtesting.Model.ScannedImage;
import com.example.opencvtesting.R;
import com.example.opencvtesting.ui.common.ScannerCameraView;
import com.example.opencvtesting.utility.ImageProcessing;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


public class ScannerFragment extends Fragment implements View.OnClickListener, CameraBridgeViewBase.CvCameraViewListener2, CameraViewCallback {

    private ScannerViewModel scannerViewModel;
    private ScannerCameraView mJavaCameraView;
    private ImageButton mBtnSnap;
    private Button mBtnDocument;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            if (status == BaseLoaderCallback.SUCCESS) {
                mJavaCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scanner, container, false);
        NavController navController = NavHostFragment.findNavController(this);
        scannerViewModel = ViewModelProviders.of(this).get(ScannerViewModel.class);
        scannerViewModel.setBackStackEntry(navController,getViewLifecycleOwner());
        mJavaCameraView = root.findViewById(R.id.my_camera_view);
        mJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mJavaCameraView.setCvCameraViewListener(this);
        mBtnSnap = root.findViewById(R.id.btnCapture);
        mBtnSnap.setOnClickListener(this);
        mBtnDocument = root.findViewById(R.id.btnDocument);
        mBtnDocument.setOnClickListener(this);
        return root;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        scannerViewModel.setRGBA(width, height);
    }

    @Override
    public void onCameraViewStopped() {
        scannerViewModel.releaseRGBA();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        Mat test = new Mat(); // Development
        scannerViewModel.setRGBA(inputFrame.rgba(),getContext());
//        scannerViewModel.getRGBA().copyTo(test);
//        ImageProcessing.adaptiveThreshold(test);
        return scannerViewModel.getRGBA();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mJavaCameraView != null)
            mJavaCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug())
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        else
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getActivity(),baseLoaderCallback);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == mBtnSnap.getId()) {
            mJavaCameraView.takePicture(this);
        }
        else if(v.getId() == mBtnDocument.getId())
        {
            final ScannedDocument scannedDocument = scannerViewModel.getScannedDocument();
            if(scannedDocument.isEmpty()) return;

            ScannerFragmentDirections.ActionNavigationScannerToDocumentReaderFragment action
                    = ScannerFragmentDirections.actionNavigationScannerToDocumentReaderFragment(scannedDocument);
            NavHostFragment.findNavController(this).navigate(action);
        }
    }

    @Override
    public void receiveBitmap(Bitmap bitmap) {
        scannerViewModel.setReceivedBitmap(scannerViewModel.getRGBA(), getContext());

        NavController navController = NavHostFragment.findNavController(this);
        ScannerFragmentDirections.ActionScannerToNavigationImageSelector action
                = ScannerFragmentDirections.actionScannerToNavigationImageSelector(scannerViewModel.getReceivedBitmap(), null);
        navController.navigate(action);
        this.mJavaCameraView.disableView();
    }
}