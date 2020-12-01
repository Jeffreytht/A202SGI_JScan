package com.example.jScanner.ui.documentScanner.document_arrange;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.R;
import com.example.jScanner.ui.documentScanner.document_reader.DocumentArrangeViewHolder;

import java.util.Collections;
import java.util.List;

public class DocumentArrangeAdapter extends RecyclerView.Adapter<DocumentArrangeViewHolder> implements ItemTouchHelperAdapter{

    private final Context mContext;
    private final List<ScannedImage> mScannedDocumentLinkedList;
    private final ItemTouchHelperViewHolder[] mCallback;

    public DocumentArrangeAdapter(Context context, List<ScannedImage> scannedDocumentLinkedList) {
        mContext = context;
        mScannedDocumentLinkedList = scannedDocumentLinkedList;
        mCallback = new ItemTouchHelperViewHolder[mScannedDocumentLinkedList.size()];
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
        mCallback[position] = holder;
    }

    @Override
    public int getItemCount() {
        return mScannedDocumentLinkedList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mScannedDocumentLinkedList, i, i + 1);
                moveCallBack(i, i+1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mScannedDocumentLinkedList, i, i - 1);
                moveCallBack(i, i-1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    private void moveCallBack(int oldPos, int newPos){
        mCallback[oldPos].onItemMove(newPos);
        mCallback[newPos].onItemMove(oldPos);
        ItemTouchHelperViewHolder temp = mCallback[oldPos];
        mCallback[oldPos] = mCallback[newPos];
        mCallback[newPos] = temp;
    }



}
