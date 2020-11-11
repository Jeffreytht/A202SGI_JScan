package com.example.jScanner.ui.documentScanner.document_arrange;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.jScanner.R;


public class DocumentArrangeFragment extends Fragment {

    private DocumentArrangeViewModel mViewModel;
    private RecyclerView mRvDocumentArrange;
    private DocumentArrangeAdapter mAdapter;

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
        mViewModel = new ViewModelProvider(this).get(DocumentArrangeViewModel.class);

        if(getArguments() != null){
            mViewModel.init(getArguments());
            mAdapter = new DocumentArrangeAdapter(getContext(), mViewModel.getScannedImageList());
            ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRvDocumentArrange);
            mRvDocumentArrange.setAdapter(mAdapter);
            mRvDocumentArrange.setLayoutManager(new GridLayoutManager(getContext(), 2));
            mRvDocumentArrange.addItemDecoration(new GridSpacingItemDecoration(2,16,true));
        }

        NavBackStackEntry backStack = NavHostFragment.findNavController(this).getPreviousBackStackEntry();
        if(backStack != null)
        {
            SavedStateHandle savedStateHandle = backStack.getSavedStateHandle();
            savedStateHandle.set(TAG_SCANNED_IMAGE_LIST, mViewModel.getScannedImageList());
        }
    }

}