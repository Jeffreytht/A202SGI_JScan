package com.example.jScanner.ui.documentScanner.document_arrange;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.R;

import java.util.LinkedList;

public class DocumentArrangeFragment extends Fragment {

    private DocumentArrangeViewModel mViewModel;
    private RecyclerView mRvDocumentArrange;
    private DocumentArrangeAdapter mAdapter;

    public static DocumentArrangeFragment newInstance() {
        return new DocumentArrangeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_document_arrange, container, false);
        mRvDocumentArrange = view.findViewById(R.id.rv_document_arrange);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DocumentArrangeViewModel.class);

        if(getArguments() != null){
            mViewModel.init(getArguments());
            mAdapter = new DocumentArrangeAdapter(getContext(), mViewModel.getScannedImageList());
            mRvDocumentArrange.setAdapter(mAdapter);
            mRvDocumentArrange.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

    }

}