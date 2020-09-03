package com.example.opencvtesting.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.opencvtesting.Model.ScannedImage;
import com.example.opencvtesting.R;
import com.example.opencvtesting.utility.ImageProcessing;

import java.util.LinkedList;

public class DocumentPreviewAdapter extends RecyclerView.Adapter<DocumentPreviewAdapter.DocumentHolder> {
    private LinkedList<ScannedImage>mScannedImage;
    private Context mContext;

    public  DocumentPreviewAdapter()
    {
        mScannedImage = new LinkedList<>();
    }

    public void setData(Context context, LinkedList<ScannedImage>scannedImages)
    {
        mScannedImage = scannedImages;
        mContext = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentHolder(LayoutInflater.from(mContext).inflate(R.layout.viewtemp,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentHolder holder, int position) {
        holder.setData(mScannedImage.get(position));
    }

    @Override
    public int getItemCount() {
        return mScannedImage.size();
    }

    public class DocumentHolder extends  RecyclerView.ViewHolder {

        private ImageView iv;

        public DocumentHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.imageView);
        }

        public void setData(ScannedImage scannedImage)
        {
            iv.setImageBitmap(ImageProcessing.colorFiltering(scannedImage.getFinalImage(), scannedImage.getFilter()));
        }
    }
}
