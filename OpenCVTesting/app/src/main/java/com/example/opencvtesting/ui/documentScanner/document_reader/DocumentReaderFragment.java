package com.example.opencvtesting.ui.documentScanner.document_reader;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.opencvtesting.Adapter.DocumentPreviewAdapter;
import com.example.opencvtesting.Adapter.DocumentReaderColorFilterAdapter;
import com.example.opencvtesting.Callback.DocumentColorFilterCallback;
import com.example.opencvtesting.Model.ScannedDocument;
import com.example.opencvtesting.Model.ScannedImage;
import com.example.opencvtesting.R;

public class DocumentReaderFragment extends Fragment implements View.OnClickListener, DocumentColorFilterCallback, View.OnFocusChangeListener {

    private DocumentPreviewAdapter mDocumentPreviewAdapter;
    private ViewPager2 mViewPagerDocumentPreview;
    private Button mButtonRotateLeft, mButtonRotateRight, mButtonCrop, mButtonColorFilter;
    private RecyclerView mRecyclerViewColorFilter;
    private DocumentReaderColorFilterAdapter mDocumentReaderColorFilterAdapter;
    private DocumentReaderViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.document_reader_fragment, container, false);

        mViewPagerDocumentPreview   = view.findViewById(R.id.viewPager);
        mButtonCrop                 = view.findViewById(R.id.button_crop);
        mButtonRotateLeft           = view.findViewById(R.id.button_rotateLeft);
        mButtonRotateRight          = view.findViewById(R.id.button_rotateRight);
        mButtonColorFilter          = view.findViewById(R.id.button_colorFilter);
        mRecyclerViewColorFilter    = view.findViewById(R.id.recyclerView_colorFilter);
        mViewModel                  = ViewModelProviders.of(this).get(DocumentReaderViewModel.class);
        mViewModel.setBackStackEntry(NavHostFragment.findNavController(this),getViewLifecycleOwner());

        mButtonRotateLeft   .setOnClickListener(this);
        mButtonRotateRight  .setOnClickListener(this);
        mButtonCrop         .setOnClickListener(this);
        mButtonColorFilter  .setOnClickListener(this);

        mDocumentReaderColorFilterAdapter = new DocumentReaderColorFilterAdapter(this);
        mRecyclerViewColorFilter.setAdapter(mDocumentReaderColorFilterAdapter);
        mRecyclerViewColorFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        mRecyclerViewColorFilter.setVisibility(View.INVISIBLE);

        mDocumentPreviewAdapter = new DocumentPreviewAdapter();
        mViewPagerDocumentPreview.setAdapter(mDocumentPreviewAdapter);


        mViewModel.getScannedDocument().observe(getViewLifecycleOwner(), new Observer<ScannedDocument>() {
            @Override
            public void onChanged(ScannedDocument scannedDocument) {
                mDocumentPreviewAdapter.setData(getContext(), scannedDocument.getScannedImageList());
                mDocumentReaderColorFilterAdapter.setData(getContext(),mViewModel.getCurrentSelectedImage());
            }
        });

        mRecyclerViewColorFilter.setOnFocusChangeListener(this);
        mViewPagerDocumentPreview.registerOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mViewModel.setViewPagerCurrentIndex(position);
                mDocumentReaderColorFilterAdapter.setData(getContext(),mViewModel.getCurrentSelectedImage());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        if(getArguments()!= null) {
            mViewModel.initViewModel(getArguments());
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == mButtonCrop.getId())
        {
            ScannedImage selectedImage = mViewModel.getCurrentSelectedImage();
            DocumentReaderFragmentDirections.ActionDocumentReaderFragmentToNavigationImageContourSelector action
                    = DocumentReaderFragmentDirections.actionDocumentReaderFragmentToNavigationImageContourSelector(selectedImage.getOriImage(), selectedImage.getContour());
            NavHostFragment.findNavController(this).navigate(action);
        }
        else if(id == mButtonRotateLeft.getId())
        {
            mViewModel.rotateBitmapNContour(mViewPagerDocumentPreview.getCurrentItem(), -90);
            mDocumentPreviewAdapter.notifyDataSetChanged();
        }
        else if(id == mButtonRotateRight.getId())
        {
            mViewModel.rotateBitmapNContour(mViewPagerDocumentPreview.getCurrentItem(), 90);
            mDocumentPreviewAdapter.notifyDataSetChanged();
        }
        else if(id == mButtonColorFilter.getId())
        {
            if(mRecyclerViewColorFilter.getVisibility() == View.INVISIBLE)
                mRecyclerViewColorFilter.setVisibility(View.VISIBLE);
            else
                mRecyclerViewColorFilter.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void changeColorFilter(int type) {
        mViewModel.getCurrentSelectedImage().setFilter(type);
        mDocumentReaderColorFilterAdapter.notifyDataSetChanged();
        mDocumentPreviewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus)
            mRecyclerViewColorFilter.setVisibility(View.INVISIBLE);
    }
}