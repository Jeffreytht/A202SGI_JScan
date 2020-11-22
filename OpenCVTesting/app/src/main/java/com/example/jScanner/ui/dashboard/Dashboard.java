package com.example.jScanner.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.R;
import com.example.jScanner.utility.Database;
import com.example.jScanner.utility.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Dashboard extends Fragment implements View.OnClickListener{

    private DashboardViewModel mViewModel;
    private RecyclerView mRvDocumentList;
    private FloatingActionButton mFabScanner;
    private ArrayList<ScannedDocument> testData = new ArrayList<ScannedDocument>(){{
        add(new ScannedDocument("", "260CDE Assignment"));
        add(new ScannedDocument("", "260CDE Assignment"));
        add(new ScannedDocument("", "260CDE Assignment"));
        add(new ScannedDocument("", "260CDE Assignment"));
        add(new ScannedDocument("", "260CDE Assignment"));
    }};

    public static Dashboard newInstance() {
        return new Dashboard();
    }

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

        DocumentAdapter adapter = new DocumentAdapter(testData, requireContext());
        mRvDocumentList.addItemDecoration(new DashboardItemDecoration(16, 1,getResources().getDisplayMetrics()));
        mRvDocumentList.setAdapter(adapter);
        mRvDocumentList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false));
        Database.getDocument(User.getUser(), null);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == mFabScanner.getId()){
            NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_fragment_scanner);
        }
    }
}