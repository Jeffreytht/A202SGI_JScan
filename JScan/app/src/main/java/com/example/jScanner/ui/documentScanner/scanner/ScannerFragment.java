package com.example.jScanner.ui.documentScanner.scanner;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.jScanner.Callback.CommonResultListener;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.R;
import com.example.jScanner.ui.common.ScannerCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


public class ScannerFragment extends Fragment implements View.OnClickListener, CameraBridgeViewBase.CvCameraViewListener2, CommonResultListener<Bitmap> {

    private ScannerViewModel scannerViewModel;
    private ScannerCameraView mJavaCameraView;
    private ImageButton mBtnSnap;
    private ImageButton mBtnDocument;
    private TextView mTextViewTotalImage;

    private final BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(getActivity()) {
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
        scannerViewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
        scannerViewModel.setBackStackEntry(navController,getViewLifecycleOwner());
        mJavaCameraView = root.findViewById(R.id.my_camera_view);
        mJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mJavaCameraView.setCvCameraViewListener(this);
        mBtnSnap = root.findViewById(R.id.btnCapture);
        mBtnSnap.setOnClickListener(this);
        mBtnDocument = root.findViewById(R.id.btnDocument);
        mBtnDocument.setOnClickListener(this);
        mTextViewTotalImage = root.findViewById(R.id.textView_totalImage);
        mTextViewTotalImage.setVisibility(View.INVISIBLE);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        scannerViewModel.getButtonBitmap().observe(getViewLifecycleOwner(), bitmap -> {
            mBtnDocument.setImageBitmap(bitmap);
            mTextViewTotalImage.setText(String.valueOf(scannerViewModel.getTotalScannedImage()));
            mTextViewTotalImage.setVisibility(View.VISIBLE);
        });
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
        scannerViewModel.setRGBA(inputFrame.rgba(),getContext());
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

        if(getArguments() != null){
            scannerViewModel.initViewModel(getArguments());
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == mBtnSnap.getId()) {
            mJavaCameraView.takePicture(this);
        }
        else if(v.getId() == mBtnDocument.getId())
        {
            final ScannedDocument scannedDocument = scannerViewModel.getScannedDocument();
            if(scannedDocument.getTotalPages() == 0) return;

            ScannerFragmentDirections.ActionNavigationScannerToDocumentReaderFragment action
                    = ScannerFragmentDirections.actionNavigationScannerToDocumentReaderFragment(scannedDocument);
            NavHostFragment.findNavController(this).navigate(action);
        }
    }

    @Override
    public void onResultReceived(Bitmap result) {
        scannerViewModel.setReceivedBitmap(scannerViewModel.getRGBA());

        NavController navController = NavHostFragment.findNavController(this);
        ScannerFragmentDirections.ActionScannerToNavigationImageSelector action
                = ScannerFragmentDirections.actionScannerToNavigationImageSelector(scannerViewModel.getReceivedBitmap(), null);
        navController.navigate(action);
        this.mJavaCameraView.disableView();
    }
}