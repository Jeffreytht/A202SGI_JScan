package com.example.jScanner.ui.documentScanner.document_reader;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.R;
import com.example.jScanner.ui.documentScanner.document_arrange.ItemTouchHelperViewHolder;

public class DocumentArrangeViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
    private final TextView tvPageNumber;
    private final ImageView mIvDocument;

    public DocumentArrangeViewHolder(@NonNull View itemView) {
        super(itemView);
        tvPageNumber = itemView.findViewById(R.id.tv_pageNumber);
        mIvDocument = itemView.findViewById(R.id.iv_document);
    }

    public void setData(Bitmap bitmap, int pageNumber){
        mIvDocument.setImageBitmap(bitmap);
        tvPageNumber.setText(String.valueOf(pageNumber));
    }

    @Override
    public void onItemMove(int newPosition) {
        tvPageNumber.setText(String.valueOf(newPosition + 1));
    }
}