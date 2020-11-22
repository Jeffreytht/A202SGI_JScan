package com.example.jScanner.ui.dashboard;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.R;

public class DocumentViewHolder extends RecyclerView.ViewHolder {
    private final TextView mTvDocument;
    private final TextView mTvDate;
    private final Button mBtnShare;
    private final Button mBtnRename;
    private final Button mBtnModify;

    public DocumentViewHolder(@NonNull View itemView) {
        super(itemView);
        mTvDocument = itemView.findViewById(R.id.tv_document_name);
        mTvDate     = itemView.findViewById(R.id.tv_date);
        mBtnModify  = itemView.findViewById(R.id.btn_modify);
        mBtnRename  = itemView.findViewById(R.id.btn_rename);
        mBtnShare   = itemView.findViewById(R.id.btn_share);
    }

    public void setViewHolder(ScannedDocument scannedDocument){
        mTvDocument.setText(scannedDocument.getName());
        mTvDate.setText("18/11/2020");
    }
}
