package com.example.jScanner.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.Callback.BiResultListener;
import com.example.jScanner.Callback.CommonResultListener;
import com.example.jScanner.Callback.ProgressDialogListener;
import com.example.jScanner.Callback.StatusResultListener;
import com.example.jScanner.MainActivity;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.R;
import com.example.jScanner.utility.Database;
import com.example.jScanner.utility.Storage;
import com.example.jScanner.utility.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class Dashboard extends Fragment implements View.OnClickListener, ProgressDialogListener, DashboardItemListener, BiResultListener<StatusResultListener, ScannedDocument> {

    private DashboardViewModel mViewModel;
    private RecyclerView mRvDocumentList;
    private FloatingActionButton mFabScanner;
    private DocumentAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mRvDocumentList = view.findViewById(R.id.rv_document_list);
        mFabScanner = view.findViewById(R.id.fab_scanner);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        mFabScanner.setOnClickListener(this);

        mAdapter = new DocumentAdapter(mViewModel.getScannedDocument().getValue(), requireContext(), this);
        mRvDocumentList.addItemDecoration(new DashboardItemDecoration(16, 1,getResources().getDisplayMetrics()));
        mRvDocumentList.setAdapter(mAdapter);
        mRvDocumentList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false));

        mViewModel.getScannedDocument().observe(getViewLifecycleOwner(), documents -> {
            mAdapter.notifyDataSetChanged();
        });

        mViewModel.initDocument(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == mFabScanner.getId()){
            NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_fragment_scanner);
        }
    }


    @Override
    public void onShowProgressDialog(String message) {
        ((MainActivity) requireActivity()).showProgressDialog(message);
    }

    @Override
    public void onUpdateProgressDialog(String message) {
        ((MainActivity) requireActivity()).updateProgressDialog(message);
    }

    @Override
    public void onDismissProgressDialog() {
        ((MainActivity) requireActivity()).dismissProgressDialog();
    }

    @Override
    public void onImageClicked(ScannedDocument scannedDocument) {
        Storage.downloadPDF(scannedDocument, file -> {
            if(file != null) {
                Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", file);
                Intent sharePDF = new Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/pdf");
                sharePDF.putExtra(Intent.EXTRA_STREAM, uri);
                sharePDF.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharePDF.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(Intent.createChooser(sharePDF,"Share "));

            } else {
                Toast.makeText(requireContext(), "Error Downloading PDF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onShareClicked(ScannedDocument scannedDocument) {
        Storage.downloadPDF(scannedDocument, file -> {
            if(file != null) {
                Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", file);
                Intent sharePDF = new Intent(Intent.ACTION_SEND).setDataAndType(uri, "application/pdf");
                sharePDF.putExtra(Intent.EXTRA_STREAM, uri);
                sharePDF.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharePDF.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(Intent.createChooser(sharePDF,"Share "));

            } else {
                Toast.makeText(requireContext(), "Error Downloading PDF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRenameClicked(ScannedDocument scannedDocument) {
        final EditText mEditTextFileName = new EditText(getContext());
        float density = getResources().getDisplayMetrics().density;

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding((int) (16 * density), 0, (int) (16 * density), 0);

        mEditTextFileName.setHint("File name");
        mEditTextFileName.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        mEditTextFileName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mEditTextFileName.setText(scannedDocument.getName());

        linearLayout.addView(mEditTextFileName);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Rename")
                .setView(linearLayout)
                .setPositiveButton("Save", (dialog, which) -> {
                    scannedDocument.setName(mEditTextFileName.getText().toString());
                    Database.updateDocument(User.getUser(), scannedDocument);
                    mAdapter.notifyItemChanged(mViewModel.indexOfScannedDocument(scannedDocument));
                })
                .show();
    }

    @Override
    public void onModifyClicked(ScannedDocument scannedDocument) {
        Database.getFullDocument(User.getUser(),scannedDocument,this, this);
    }

    @Override
    public void onDeleteClicked(ScannedDocument scannedDocument) {
        Database.removeDocument(User.getUser(), scannedDocument);
        this.mViewModel.removeDocument(scannedDocument);
        this.mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResultReceived(StatusResultListener result, ScannedDocument scannedDocument) {
        if(!result.isSuccess()){
            Toast.makeText(requireContext(), result.getErrorMessage(),Toast.LENGTH_SHORT).show();
        } else {
            DashboardDirections.ActionDashboardToFragmentScanner actionDashboardToFragmentScanner = DashboardDirections.actionDashboardToFragmentScanner();
            actionDashboardToFragmentScanner.setScannedDocument(scannedDocument);
            NavHostFragment.findNavController(this).navigate(actionDashboardToFragmentScanner);
        }
    }
}