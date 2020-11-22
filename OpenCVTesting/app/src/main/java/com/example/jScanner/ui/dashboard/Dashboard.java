package com.example.jScanner.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.Callback.ProgressDialogListener;
import com.example.jScanner.MainActivity;
import com.example.jScanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class Dashboard extends Fragment implements View.OnClickListener, ProgressDialogListener {

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

        mAdapter = new DocumentAdapter(mViewModel.getScannedDocument().getValue(), requireContext());
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
}