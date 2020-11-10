package com.example.jScanner.ui.documentScanner.document_reader;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.jScanner.Callback.DocumentColorFilterCallback;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.R;

import java.util.LinkedList;

public class DocumentReaderFragment extends Fragment implements View.OnClickListener, DocumentColorFilterCallback, View.OnFocusChangeListener, ScannedImageFinishPreComputeCallback {

    private DocumentPreviewAdapter mDocumentPreviewAdapter;
    private ViewPager2 mViewPagerDocumentPreview;
    private Button mButtonRotateLeft, mButtonRotateRight, mButtonCrop, mButtonColorFilter, mButtonReorder;
    private RecyclerView mRecyclerViewColorFilter;
    private DocumentReaderColorFilterAdapter mDocumentReaderColorFilterAdapter;
    private DocumentReaderViewModel mViewModel;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document_reader, container, false);
        setHasOptionsMenu(true);
        mViewPagerDocumentPreview   = view.findViewById(R.id.viewPager);
        mButtonCrop                 = view.findViewById(R.id.button_crop);
        mButtonRotateLeft           = view.findViewById(R.id.button_rotateLeft);
        mButtonRotateRight          = view.findViewById(R.id.button_rotateRight);
        mButtonColorFilter          = view.findViewById(R.id.button_colorFilter);
        mButtonReorder              = view.findViewById(R.id.button_reorder);
        mRecyclerViewColorFilter    = view.findViewById(R.id.recyclerView_colorFilter);
        mViewModel                  = ViewModelProviders.of(this).get(DocumentReaderViewModel.class);
        mViewModel.setBackStackEntry(NavHostFragment.findNavController(this),getViewLifecycleOwner());

        mButtonRotateLeft   .setOnClickListener(this);
        mButtonRotateRight  .setOnClickListener(this);
        mButtonCrop         .setOnClickListener(this);
        mButtonColorFilter  .setOnClickListener(this);
        mButtonReorder      .setOnClickListener(this);

        mDocumentReaderColorFilterAdapter = new DocumentReaderColorFilterAdapter(this);
        mRecyclerViewColorFilter.setAdapter(mDocumentReaderColorFilterAdapter);
        mRecyclerViewColorFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        mRecyclerViewColorFilter.setVisibility(View.INVISIBLE);

        mDocumentPreviewAdapter = new DocumentPreviewAdapter(mViewPagerDocumentPreview);
        mViewPagerDocumentPreview.setAdapter(mDocumentPreviewAdapter);

        mViewModel.getScannedDocument().observe(getViewLifecycleOwner(), new Observer<ScannedDocument>() {
            @Override
            public void onChanged(ScannedDocument scannedDocument) {
                mDocumentPreviewAdapter.setData(getContext(), scannedDocument.getScannedImageList());
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
                mDocumentReaderColorFilterAdapter.setData(getContext(),mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        if(getArguments()!= null) {
            mViewModel.initViewModel(getArguments(), this);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_document_reader,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save){
            mViewModel.createPDF(getContext().getFilesDir().getPath());
        }

        return super.onOptionsItemSelected(item);
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
            mDocumentReaderColorFilterAdapter.setData(getContext(), mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
        }
        else if(id == mButtonRotateRight.getId())
        {
            mViewModel.rotateBitmapNContour(mViewPagerDocumentPreview.getCurrentItem(), 90);
            mDocumentPreviewAdapter.notifyDataSetChanged();
            mDocumentReaderColorFilterAdapter.setData(getContext(), mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
        }
        else if(id == mButtonColorFilter.getId())
        {
            if(mRecyclerViewColorFilter.getVisibility() == View.INVISIBLE)
                mRecyclerViewColorFilter.setVisibility(View.VISIBLE);
            else
                mRecyclerViewColorFilter.setVisibility(View.INVISIBLE);
        }
        else if(id == mButtonReorder.getId()){
            LinkedList<ScannedImage> scannedImageLinkedList = mViewModel.getScannedDocument().getValue().getScannedImageList();
            DocumentReaderFragmentDirections.ActionFragmentDocumentReaderToDocumentArrangeFragment action = DocumentReaderFragmentDirections.actionFragmentDocumentReaderToDocumentArrangeFragment(scannedImageLinkedList.toArray(new ScannedImage[scannedImageLinkedList.size()]));
            NavHostFragment.findNavController(this).navigate(action);
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

    @Override
    public void refreshUi() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDocumentReaderColorFilterAdapter.setData(getContext(),mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.stopAsync();
    }
}