package com.example.jScanner.ui.documentScanner.document_arrange;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.R;

import java.util.LinkedList;

public class DocumentArrangeAdapter extends RecyclerView.Adapter<DocumentArrangeAdapter.DocumentArrangeViewHolder> {

    private final Context mContext;
    private final LinkedList<ScannedImage> mScannedDocumentLinkedList;

    public DocumentArrangeAdapter(Context context, LinkedList<ScannedImage> scannedDocumentLinkedList) {
        mContext = context;
        mScannedDocumentLinkedList = scannedDocumentLinkedList;
    }

    @NonNull
    @Override
    public DocumentArrangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_document_arrange, parent, false);

        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) itemView.getLayoutParams();
        lp.width = parent.getMeasuredWidth() / 2;
        lp.height = (int)(lp.width * 1.4142);
        itemView.setLayoutParams(lp);
        return new DocumentArrangeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentArrangeViewHolder holder, int position) {
        holder.setData(mScannedDocumentLinkedList.get(position).getFinalImage(), position + 1);
    }

    @Override
    public int getItemCount() {
        return mScannedDocumentLinkedList.size();
    }

    public class DocumentArrangeViewHolder extends RecyclerView.ViewHolder{
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
    }
}
