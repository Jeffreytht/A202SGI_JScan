package com.example.jScanner.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.R;

import java.util.ArrayList;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentViewHolder> {
    private final Context mContext;
    private final ArrayList<ScannedDocument> mScannedDocumentArrayList;
    private final DashboardItemListener mDashboardItemListener;

    public DocumentAdapter(ArrayList<ScannedDocument> scannedDocumentArrayList, Context context, DashboardItemListener dashboardItemListener) {
        this.mContext = context;
        this.mScannedDocumentArrayList = scannedDocumentArrayList;
        this.mDashboardItemListener = dashboardItemListener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_document_list, parent, false), mContext, mDashboardItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.setViewHolder(mScannedDocumentArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return mScannedDocumentArrayList.size();
    }

}
