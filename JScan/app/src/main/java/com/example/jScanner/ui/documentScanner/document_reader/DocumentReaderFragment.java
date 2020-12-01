package com.example.jScanner.ui.documentScanner.document_reader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import com.example.jScanner.Callback.CommonResultListener;
import com.example.jScanner.Callback.ProgressDialogListener;
import com.example.jScanner.MainActivity;
import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.R;
import com.example.jScanner.utility.Database;
import com.example.jScanner.utility.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

public class DocumentReaderFragment extends Fragment implements View.OnClickListener, CommonResultListener<Integer>, View.OnFocusChangeListener, ProgressDialogListener {

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
        mViewPagerDocumentPreview = view.findViewById(R.id.viewPager);
        mButtonCrop = view.findViewById(R.id.button_crop);
        mButtonRotateLeft = view.findViewById(R.id.button_rotateLeft);
        mButtonRotateRight = view.findViewById(R.id.button_rotateRight);
        mButtonColorFilter = view.findViewById(R.id.button_colorFilter);
        mButtonReorder = view.findViewById(R.id.button_reorder);
        mButtonRemove = view.findViewById(R.id.button_remove);
        mRecyclerViewColorFilter = view.findViewById(R.id.recyclerView_colorFilter);
        mViewModel = new ViewModelProvider(this).get(DocumentReaderViewModel.class);
        mViewModel.setBackStackEntry(NavHostFragment.findNavController(this), getViewLifecycleOwner());

        mButtonRotateLeft.setOnClickListener(this);
        mButtonRotateRight.setOnClickListener(this);
        mButtonCrop.setOnClickListener(this);
        mButtonColorFilter.setOnClickListener(this);
        mButtonReorder.setOnClickListener(this);
        mButtonRemove.setOnClickListener(this);

        mDocumentReaderColorFilterAdapter = new DocumentReaderColorFilterAdapter(this);
        mRecyclerViewColorFilter.setAdapter(mDocumentReaderColorFilterAdapter);
        mRecyclerViewColorFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewColorFilter.setVisibility(View.INVISIBLE);

        mDocumentPreviewAdapter = new DocumentPreviewAdapter(mViewPagerDocumentPreview);
        mViewPagerDocumentPreview.setAdapter(mDocumentPreviewAdapter);

        mViewModel.getScannedDocument().observe(getViewLifecycleOwner(), scannedDocument -> mDocumentPreviewAdapter.setData(getContext(), scannedDocument.getScannedImageList()));

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
                mDocumentReaderColorFilterAdapter.setData(getContext(), mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        if (getArguments() != null) {
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
        inflater.inflate(R.menu.menu_document_reader, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save) {
            final EditText mEditTextFileName = new EditText(getContext());
            float density = getResources().getDisplayMetrics().density;

            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setPadding((int) (16 * density), 0, (int) (16 * density), 0);

            mEditTextFileName.setHint("File name");
            mEditTextFileName.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            mEditTextFileName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mEditTextFileName.setText(mViewModel.getDocumentName());

            linearLayout.addView(mEditTextFileName);

            ProgressDialogListener progressDialogListener = new ProgressDialogListener() {
                @Override
                public void onShowProgressDialog(String message) {
                    requireActivity().runOnUiThread(() -> ((MainActivity) requireActivity()).showProgressDialog(message));
                }

                @Override
                public void onUpdateProgressDialog(String message) {
                    requireActivity().runOnUiThread(() -> ((MainActivity) requireActivity()).updateProgressDialog(message));
                }

                @Override
                public void onDismissProgressDialog() {
                    requireActivity().runOnUiThread(() -> {
                        ((MainActivity) requireActivity()).dismissProgressDialog();
                        NavHostFragment.findNavController(DocumentReaderFragment.this).popBackStack(R.id.fragment_dashboard, false);
                    });
                }
            };

            if (mViewModel.isNewDocument())
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Save as")
                        .setView(linearLayout)
                        .setPositiveButton("Save", (dialog, which) -> {
                            mViewModel.setDocumentName(mEditTextFileName.getText().toString());
                            Database.saveDocument(User.getUser(), Objects.requireNonNull(mViewModel.getScannedDocument().getValue()), progressDialogListener);
                        })
                        .show();
            else {
                Database.saveDocument(User.getUser(), Objects.requireNonNull(mViewModel.getScannedDocument().getValue()), progressDialogListener);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == mButtonCrop.getId()) {
            ScannedImage selectedImage = mViewModel.getCurrentSelectedImage();
            DocumentReaderFragmentDirections.ActionDocumentReaderFragmentToNavigationImageContourSelector action
                    = DocumentReaderFragmentDirections.actionDocumentReaderFragmentToNavigationImageContourSelector(selectedImage.getOriImage(), selectedImage.getContour());
            NavHostFragment.findNavController(this).navigate(action);

        } else if (id == mButtonRotateLeft.getId()) {
            mViewModel.rotateBitmapNContour(mViewPagerDocumentPreview.getCurrentItem(), -90);
            mDocumentPreviewAdapter.notifyDataSetChanged();
            mDocumentReaderColorFilterAdapter.setData(getContext(), mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
        } else if (id == mButtonRotateRight.getId()) {
            mViewModel.rotateBitmapNContour(mViewPagerDocumentPreview.getCurrentItem(), 90);
            mDocumentPreviewAdapter.notifyDataSetChanged();
            mDocumentReaderColorFilterAdapter.setData(getContext(), mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
        } else if (id == mButtonColorFilter.getId()) {
            if (mRecyclerViewColorFilter.getVisibility() == View.INVISIBLE) {
                mDocumentReaderColorFilterAdapter.setData(getContext(), mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
                mRecyclerViewColorFilter.setVisibility(View.VISIBLE);
            }
            else
                mRecyclerViewColorFilter.setVisibility(View.INVISIBLE);
        } else if (id == mButtonReorder.getId()) {
            if (mViewModel.getScannedDocument().getValue() != null) {
                DocumentReaderFragmentDirections.ActionFragmentDocumentReaderToDocumentArrangeFragment action = DocumentReaderFragmentDirections.actionFragmentDocumentReaderToDocumentArrangeFragment(mViewModel.getScannedDocument().getValue());
                NavHostFragment.findNavController(this).navigate(action);
            }
        } else if (id == mButtonRemove.getId()) {
            mViewModel.removeScannedImage(mViewModel.getCurrentSelectedImage());
            int totalImage = mViewModel.getNumOfScannedImage();
            if (totalImage == 0) {
                NavHostFragment.findNavController(this).popBackStack();
            } else {
                int currIndex = mViewModel.getViewPagerCurrentIndex();
                mDocumentPreviewAdapter.notifyItemRemoved(currIndex);

                if (currIndex == totalImage)
                    mViewModel.setViewPagerCurrentIndex(currIndex - 1);

                mDocumentReaderColorFilterAdapter.setData(getContext(), mViewModel.getCurrentSelectedImage(), mViewModel.getCurrentSelectedFilteredImage());
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus)
            mRecyclerViewColorFilter.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.stopAsync();
    }

    @Override
    public void onResultReceived(Integer type) {
        mViewModel.getCurrentSelectedImage().setFilter(type);
        mDocumentReaderColorFilterAdapter.notifyDataSetChanged();
        mDocumentPreviewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShowProgressDialog(String message) {
        requireActivity().runOnUiThread(()-> ((MainActivity)requireActivity()).showProgressDialog(message));

    }

    @Override
    public void onUpdateProgressDialog(String message) {
        requireActivity().runOnUiThread(()-> ((MainActivity)requireActivity()).updateProgressDialog(message));
    }

    @Override
    public void onDismissProgressDialog() {
        requireActivity().runOnUiThread(()-> ((MainActivity)requireActivity()).dismissProgressDialog());
    }
}