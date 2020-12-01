package com.example.jScanner.ui.documentScanner.document_arrange;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.R;


public class DocumentArrangeFragment extends Fragment {

    private RecyclerView mRvDocumentArrange;

    public static final String TAG_SCANNED_IMAGE_LIST = "ScannedImageLinkedList";

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
        DocumentArrangeViewModel mViewModel = new ViewModelProvider(this).get(DocumentArrangeViewModel.class);

        if(getArguments() != null){
            mViewModel.init(getArguments());
            DocumentArrangeAdapter mAdapter = new DocumentArrangeAdapter(getContext(), mViewModel.getScannedImageList());
            ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRvDocumentArrange);
            mRvDocumentArrange.setAdapter(mAdapter);
            mRvDocumentArrange.setLayoutManager(new GridLayoutManager(getContext(), 2));
//            mRvDocumentArrange.addItemDecoration(new GridSpacingItemDecoration(2,50,true));
        }

        NavBackStackEntry backStack = NavHostFragment.findNavController(this).getPreviousBackStackEntry();
        if(backStack != null)
        {
            SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();
            savedStateHandle.set(TAG_SCANNED_IMAGE_LIST, mViewModel.getScannedImageList());
        }
    }

}