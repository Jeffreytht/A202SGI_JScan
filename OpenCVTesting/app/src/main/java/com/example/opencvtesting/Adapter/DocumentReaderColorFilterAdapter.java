package com.example.opencvtesting.Adapter;

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

import com.example.opencvtesting.Callback.DocumentColorFilterCallback;
import com.example.opencvtesting.Model.ScannedImage;
import com.example.opencvtesting.R;
import com.example.opencvtesting.utility.ImageProcessing;

import java.util.ArrayList;

public class DocumentReaderColorFilterAdapter extends  RecyclerView.Adapter<DocumentReaderColorFilterAdapter.ColorHolder>{

    private Context mContext;
    private ArrayList<Bitmap> mFilteredBitmap;
    private static final int NUM_OF_FILTER = 3;
    private static final String[] mFilterName = {"Original Color", "Grayscale", "Whiteboard"};
    private DocumentColorFilterCallback mDocumentColorFilterCallback;
    private ScannedImage mScannedImage;

    public DocumentReaderColorFilterAdapter(DocumentColorFilterCallback documentColorFIlterCallback) {
        this.mFilteredBitmap = new ArrayList<>();
        this.mDocumentColorFilterCallback = documentColorFIlterCallback;
    }

    public void setData(Context context, ScannedImage scannedImage)
    {
        // Clear buffer
        mFilteredBitmap.clear();
        this.mScannedImage = scannedImage;

        // Index 0 is original image
        Bitmap bmp = scannedImage.getFinalImage();
        mFilteredBitmap.add(bmp);
        for(int i = 1 ; i < NUM_OF_FILTER; i++)
            mFilteredBitmap.add(ImageProcessing.colorFiltering(bmp, 1 << i));
        mContext = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ColorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ColorHolder(
                LayoutInflater.from(mContext).inflate(R.layout.document_reader_color_filter,parent,false)
                ,mDocumentColorFilterCallback
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ColorHolder holder, int position) {
        holder.setData(mFilteredBitmap.get(position), mFilterName[position], position);
    }

    @Override
    public int getItemCount() {
        return mFilteredBitmap.size();
    }

    public class ColorHolder extends RecyclerView.ViewHolder{

        private ImageView mFilteredImage;
        private TextView mTextViewFilteredName;
        private DocumentColorFilterCallback mDocumentColorFilterCallback;

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
