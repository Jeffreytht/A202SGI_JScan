package com.example.jScanner.ui.dashboard;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.R;
import com.google.type.DateTime;

import java.text.DateFormat;
import java.util.Date;

public class DocumentViewHolder extends RecyclerView.ViewHolder {
    private final TextView mTvDocument;
    private final TextView mTvDate;
    private final ImageView mIvDocumentImage;
    private final DateFormat mDateFormat = DateFormat.getDateInstance();
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
        mIvDocumentImage = itemView.findViewById(R.id.iv_document_image);
    }

    public void setViewHolder(ScannedDocument scannedDocument){
        mTvDocument.setText(scannedDocument.getName());
        mTvDate.setText(mDateFormat.format(new Date(scannedDocument.getDate())));
        mIvDocumentImage.setImageURI(scannedDocument.getCoverUri());
        Glide.with(itemView).load(scannedDocument.getCoverUri()).into(mIvDocumentImage);
    }
}
