package com.example.jScanner.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.R;

import java.text.DateFormat;
import java.util.Date;

public class DocumentViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    private final TextView mTvDocument;
    private final TextView mTvDate;
    private final ImageView mIvDocumentImage;
    private final DateFormat mDateFormat = DateFormat.getDateInstance();
    private final Button mBtnShare;
    private final Button mBtnRename;
    private final Button mBtnModify;
    private final Button mBtnMore;
    private final DashboardItemListener mDashboardItemListener;
    private final Context mContext;

    public DocumentViewHolder(@NonNull View itemView, @NonNull Context context, @NonNull DashboardItemListener dashboardItemListener) {
        super(itemView);
        mContext = context;
        mDashboardItemListener = dashboardItemListener;
        mTvDocument = itemView.findViewById(R.id.tv_document_name);
        mTvDate = itemView.findViewById(R.id.tv_date);
        mBtnModify = itemView.findViewById(R.id.btn_modify);
        mBtnRename = itemView.findViewById(R.id.btn_rename);
        mBtnShare = itemView.findViewById(R.id.btn_share);
        mBtnMore = itemView.findViewById(R.id.btn_more);
        mIvDocumentImage = itemView.findViewById(R.id.iv_document_image);
        mBtnMore.setOnCreateContextMenuListener(this);
    }

    public void setViewHolder(ScannedDocument scannedDocument) {
        mTvDocument.setText(scannedDocument.getName());
        mTvDate.setText(mDateFormat.format(new Date(scannedDocument.getDate())));
        mIvDocumentImage.setImageURI(scannedDocument.getCoverUri());
        Glide.with(itemView).load(scannedDocument.getCoverUri()).into(mIvDocumentImage);

        mBtnModify.setOnClickListener(view -> mDashboardItemListener.onModifyClicked(scannedDocument));
        mBtnRename.setOnClickListener(view -> mDashboardItemListener.onRenameClicked(scannedDocument));
        mBtnShare.setOnClickListener(view -> mDashboardItemListener.onShareClicked(scannedDocument));
        mBtnMore.setOnClickListener(
                view -> {
                    PopupMenu popupMenu = new PopupMenu(mContext, mBtnMore);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_document_list, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.menu_delete)
                            mDashboardItemListener.onDeleteClicked(scannedDocument);
                        else if (item.getItemId() == R.id.menu_shareable_link)
                            mDashboardItemListener.onShareableLinkClicked(scannedDocument);

                        return true;
                    });

                    popupMenu.show();
                }
        );
        mIvDocumentImage.setOnClickListener(view -> mDashboardItemListener.onImageClicked(scannedDocument));
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = ((Activity) mContext).getMenuInflater();
        inflater.inflate(R.menu.menu_document_list, menu);
    }
}
