package com.example.jScanner.ui.documentScanner.image_contour_selector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.jScanner.R;
import com.example.jScanner.ui.common.ImageContourSelectorView;

import org.opencv.core.Point;


public class ImageContourSelectorFragment extends Fragment implements View.OnClickListener {

    private ImageContourSelectorViewModel imageContourSelectorViewModel;
    private ImageContourSelectorView mImageEditorView;
    private Button mButtonDone, mButtonRotateLeft, mButtonRotateRight;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_image_contour_selector, container, false);
        mButtonRotateLeft = root.findViewById(R.id.button_rotateLeft);
        mButtonRotateRight = root.findViewById(R.id.button_rotateRight);
        mButtonDone = root.findViewById(R.id.button_done);

        mButtonDone.setOnClickListener(this);
        mButtonRotateLeft.setOnClickListener(this);
        mButtonRotateRight.setOnClickListener(this);

        imageContourSelectorViewModel = new ViewModelProvider(this).get(ImageContourSelectorViewModel.class);


        if(getArguments() != null) {
            imageContourSelectorViewModel.initViewModel(getArguments());
        }

        mImageEditorView = new ImageContourSelectorView(getActivity());
        mImageEditorView.initScene(imageContourSelectorViewModel.getBitmap().getValue(), imageContourSelectorViewModel.getContour().getValue());
        ((FrameLayout)root.findViewById(R.id.container)).addView(mImageEditorView);

        imageContourSelectorViewModel.getBitmap().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                mImageEditorView.initScene(bitmap, imageContourSelectorViewModel.getContour().getValue());
            }
        });

        imageContourSelectorViewModel.getContour().observe(getViewLifecycleOwner(), new Observer<Point[]>() {
            @Override
            public void onChanged(Point[] points) {
                mImageEditorView.initScene(imageContourSelectorViewModel.getBitmap().getValue(), points);
            }
        });
        return root;
    }




    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == mButtonDone.getId())
        {
            NavController navController = NavHostFragment.findNavController(this);
            imageContourSelectorViewModel.notifyChangesToParentFragment(navController, ImageContourSelectorViewModel.ADD_IMAGE);
            navController.popBackStack();
        }
        else if(id == mButtonRotateLeft.getId())
        {
            imageContourSelectorViewModel.rotateBitmapNContour(-90);
        }
        else if(id == mButtonRotateRight.getId())
        {
            imageContourSelectorViewModel.rotateBitmapNContour(90);
        }
    }
}