package com.example.jScanner.ui.documentScanner.document_reader;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.jScanner.Callback.DatabaseCallback;
import com.example.jScanner.Callback.DocumentColorFilterCallback;
import com.example.jScanner.MainActivity;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.R;
import com.example.jScanner.utility.Database;
import com.example.jScanner.utility.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.Blob;

import java.util.LinkedList;
import java.util.Map;

public class DocumentReaderFragment extends Fragment implements View.OnClickListener, DocumentColorFilterCallback, View.OnFocusChangeListener, ScannedImageFinishPreComputeCallback {

    private DocumentPreviewAdapter mDocumentPreviewAdapter;
    private ViewPager2 mViewPagerDocumentPreview;
    private Button mButtonRotateLeft, mButtonRotateRight, mButtonCrop, mButtonColorFilter, mButtonReorder, mButtonRemove;
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
        mButtonRemove               = view.findViewById(R.id.button_remove);
        mRecyclerViewColorFilter    = view.findViewById(R.id.recyclerView_colorFilter);
        mViewModel                  = ViewModelProviders.of(this).get(DocumentReaderViewModel.class);
        mViewModel.setBackStackEntry(NavHostFragment.findNavController(this),getViewLifecycleOwner());

        mButtonRotateLeft   .setOnClickListener(this);
        mButtonRotateRight  .setOnClickListener(this);
        mButtonCrop         .setOnClickListener(this);
        mButtonColorFilter  .setOnClickListener(this);
        mButtonReorder      .setOnClickListener(this);
        mButtonRemove       .setOnClickListener(this);

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
            final EditText mEditTextFileName = new EditText(getContext());
            float density = getResources().getDisplayMetrics().density;

            LinearLayout linearLayout =new LinearLayout(getContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setPadding((int)(16 * density),0,(int)(16 * density),0);

            mEditTextFileName.setHint("File name");

            mEditTextFileName.setTextColor(getContext().getColor(R.color.colorPrimary));
            mEditTextFileName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if(mViewModel.isDocumentNameSet())
                mEditTextFileName.setText(mViewModel.getDocumentName());

            linearLayout.addView(mEditTextFileName);

            new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Save as")
                    .setView(linearLayout)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MainActivity) getActivity()).showProgressDialog("Creating PDF");

                            mViewModel.setDocumentName(mEditTextFileName.getText().toString());
                            Database.insertNewDocument(User.getUser(), mViewModel.getScannedDocument().getValue());

                            ((MainActivity) getActivity()).dismissProgressDialog();
                        }
                    }).show();
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
            if(mViewModel.getScannedDocument().getValue() != null) {
                DocumentReaderFragmentDirections.ActionFragmentDocumentReaderToDocumentArrangeFragment action = DocumentReaderFragmentDirections.actionFragmentDocumentReaderToDocumentArrangeFragment(mViewModel.getScannedDocument().getValue());
                NavHostFragment.findNavController(this).navigate(action);
            }
        } else if(id == mButtonRemove.getId()){
            mViewModel.removeScannedImage(mViewModel.getCurrentSelectedImage());
            int totalImage = mViewModel.getNumOfScannedImage();
            if(totalImage == 0) {
                NavHostFragment.findNavController(this).popBackStack();
            } else {
                int currIndex = mViewModel.getViewPagerCurrentIndex();
                mDocumentPreviewAdapter.notifyItemRemoved(currIndex);

                if(currIndex == totalImage)
                    mViewModel.setViewPagerCurrentIndex(currIndex - 1);

                mDocumentReaderColorFilterAdapter.setData(getContext(), mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
            }
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
    public void refreshUi(final int curr,final int total) {
        final MainActivity activity = (MainActivity)getActivity();
        if(activity == null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(curr == total){
                    mDocumentReaderColorFilterAdapter.setData(getContext(),mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
                    activity.dismissProgressDialog();
                } else {
                    activity.updateProgressDialog("Page " + curr + " of " + total);
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.stopAsync();
    }

}