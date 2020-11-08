package com.example.jScanner.ui.documentScanner.document_reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jScanner.Callback.DocumentColorFilterCallback;
import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.R;
import com.example.jScanner.utility.ImageProcessing;

import java.util.ArrayList;
import java.util.Arrays;

public class DocumentReaderColorFilterAdapter extends  RecyclerView.Adapter<DocumentReaderColorFilterAdapter.ColorHolder>{

    private Context mContext;
    private final ArrayList<Bitmap> mFilteredBitmap;
    private final DocumentColorFilterCallback mDocumentColorFilterCallback;
    private ScannedImage mScannedImage;

    public DocumentReaderColorFilterAdapter(DocumentColorFilterCallback documentColorFIlterCallback) {
        this.mFilteredBitmap = new ArrayList<>();
        this.mDocumentColorFilterCallback = documentColorFIlterCallback;
    }

    public void setData(Context context, ScannedImage scannedImage, Bitmap[] bitmap)
    {
        if(bitmap == null) return;

        mFilteredBitmap.clear();
        mScannedImage = scannedImage;
        mFilteredBitmap.addAll(Arrays.asList(bitmap));
        mContext = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ColorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ColorHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_document_reader_color_filter,parent,false)
                ,mDocumentColorFilterCallback
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ColorHolder holder, int position) {
        holder.setData(mFilteredBitmap.get(position), ImageProcessing.FILTER_TYPE.get(1 << position), position);
    }

    @Override
    public int getItemCount() {
        return mFilteredBitmap.size();
    }

    public class ColorHolder extends RecyclerView.ViewHolder{

        private final ImageView mFilteredImage;
        private final TextView mTextViewFilteredName;
        private final DocumentColorFilterCallback mDocumentColorFilterCallback;

        public ColorHolder(@NonNull View itemView, DocumentColorFilterCallback documentColorFilterCallback) {
            super(itemView);
            mFilteredImage = itemView.findViewById(R.id.imageView_filtered_image);
            mTextViewFilteredName = itemView.findViewById(R.id.textView_filtered_name);
            this.mDocumentColorFilterCallback = documentColorFilterCallback;
        }

        public void setData(@NonNull Bitmap bitmap, String filterName, final int position)
        {
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            float aspectRatio = (float)bitmap.getHeight() / bitmap.getWidth();
            final int expectedWidth = (int)(75 *  displayMetrics.density);
            final int expectedHeight = (int)(expectedWidth  * aspectRatio);

            if((1 << position) != mScannedImage.getFilter())
                mFilteredImage.setBackground(mContext.getResources().getDrawable(R.drawable.document_reader_image_border));
            else
                mFilteredImage.setBackground(mContext.getResources().getDrawable(R.drawable.document_reader_selected_image_border));

            mFilteredImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap,expectedWidth,expectedHeight,false));
            mTextViewFilteredName.setText(filterName);
            mFilteredImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilteredImage.setBackground(mContext.getResources().getDrawable(R.drawable.document_reader_selected_image_border));
                    mDocumentColorFilterCallback.changeColorFilter(1 << position);
                }
            });
        }
    }
}
