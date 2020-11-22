package com.example.jScanner.ui.documentScanner.document_reader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.R;

import java.util.LinkedList;
import java.util.List;

public class DocumentPreviewAdapter extends RecyclerView.Adapter<DocumentPreviewAdapter.DocumentHolder> {
    private List<ScannedImage>mScannedImage;
    private Context mContext;
    private final ViewPager2 mParent;

    public  DocumentPreviewAdapter(ViewPager2 parent)
    {
        mScannedImage = new LinkedList<>();
        mParent = parent;
    }

    public void setData(Context context, List<ScannedImage> scannedImages)
    {
        mScannedImage = scannedImages;
        mContext = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentHolder(LayoutInflater.from(mContext).inflate(R.layout.item_document_reader_image, parent, false),mParent);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentHolder holder, int position) {
        holder.setData(mScannedImage.get(position));
    }

    @Override
    public int getItemCount() {
        return mScannedImage.size();
    }


    public static class DocumentHolder extends  RecyclerView.ViewHolder implements View.OnTouchListener {

        private final ImageView iv;
        private final Matrix matrix = new Matrix();
        private final Matrix savedMatrix = new Matrix();
        private final Matrix bufferMatrix = new Matrix();
        private final PointF start = new PointF();
        private final PointF mid = new PointF();
        private float oldDist = 1f;

        private final float []matrixValues = new float[9];
        static final int NONE           = 0;
        static final int DRAG           = 1;
        static final int ZOOM           = 2;
        static final int SCALE_X        = 0; // ROW 0, COL 0
        static final int TRANSFORM_X    = 2; // ROW 0, COL 2
        static final int SCALE_Y        = 4; // ROW 1, COL 1 // idx = (1 * 3) + 1 = 4
        static final int TRANSFORM_Y    = 5; // ROW 1, COL 2 // idx = (1 * 3 + 2) = 5
        int mode = NONE;

        private int imageIntrinsicHeight;
        private int imageIntrinsicWidth;
        private static int imageViewHeight;
        private static int imageViewWidth;
        private final ViewPager2 mParent;

        @SuppressLint("ClickableViewAccessibility")
        public DocumentHolder( @NonNull View itemView, ViewPager2 parent)  {
            super(itemView);
            mParent = parent;
            iv = itemView.findViewById(R.id.imageView);
            iv.setScaleType(ImageView.ScaleType.MATRIX);
            iv.setOnTouchListener(this);

        }

        public void setData(ScannedImage scannedImage)
        {
            Bitmap bitmap = scannedImage.getFinalImage();
            iv.setImageBitmap(bitmap);

            ViewTreeObserver vto = iv.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    iv.getViewTreeObserver().removeOnPreDrawListener(this);
                    fitMatrixCenter();
                    iv.setImageMatrix(matrix);
                    return true;
                }
            });
        }


        private void fitMatrixCenter(){
            matrix.set(new Matrix());
            imageViewHeight = iv.getMeasuredHeight();
            imageViewWidth = iv.getMeasuredWidth();
            imageIntrinsicHeight = iv.getDrawable().getIntrinsicHeight();
            imageIntrinsicWidth = iv.getDrawable().getIntrinsicWidth();
            matrix.postTranslate((imageViewWidth - (float)imageIntrinsicWidth) / 2, (imageViewHeight - (float)imageIntrinsicHeight) / 2);
        }

        private float getScaleToFillScreenWidth(){return (float)imageViewWidth / imageIntrinsicWidth;}
        private float getScaleToFillScreenHeight() {return (float)imageViewHeight / imageIntrinsicHeight;}

        private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float)Math.sqrt(x * x + y * y);
        }

        private void midPoint(PointF point, MotionEvent event) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }

        private float getImageRight(Matrix matrix){
           RectF rect = new RectF(0,0,imageIntrinsicWidth,imageIntrinsicHeight);
            matrix.mapRect(rect);
            return  rect.right;
        }

        private float getImageBottom(Matrix matrix){
            RectF rect = new RectF(0,0,imageIntrinsicWidth,imageIntrinsicHeight);
            matrix.mapRect(rect);
            return  rect.bottom;
        }

        private void setEnableParentSwipe(Matrix savedMatrix, float x, float y){
            savedMatrix.getValues(matrixValues);
            float left = matrixValues[TRANSFORM_X];
            float top = matrixValues[TRANSFORM_Y];
            float right = getImageRight(savedMatrix);
            float bottom = getImageBottom(savedMatrix);
            mParent.setUserInputEnabled((x < left || x > right) || (y < top || y > bottom));
        }

        /*
        FORMULA FOR TRANSFORM X AND Y

        Step 1: Understand matrix

        (TransformX,                                                          Max for transformX
         TransformY)                                                (0,0)      |<----------->|
              +----------------------+                                +--------+-------------+
              |    (0,0)             |                                |        |             |
              |      +--------+      |                                |        |             |
              |      |        |      |                                |   IV   |             |
              |      |        |      |    Align TransformX &          |        |             |
              |      |   IV   |      |    TransformY to (0,0)         +------- +             |  -
              |      |        |      |    -------------------->       |                      |  ^
              |      +--------+      |                                |                      |  |
              |           (IV.width  |                                |       Image          |  | Max for
              |           ,IV.height)|                                |                      |  | Transform Y
              |                      |                                |                      |  v
              +----------------------+                                +----------------------+  -

              To align TransformX to (0,0)
              = -transformX

              To get image width after align
              = getRight(matrix) - transformX

              To get maxValue for transformX
              = getRight(matrix) - transformX -iv.width

              To get transformX.   Note: Since we coordinate based on top left instead of top right
              = -(getRight(matrix)- transformX - iv.width)
         */


        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView view = (ImageView) v;

            // Get current image height and width after scale
            matrix.getValues(matrixValues);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    setEnableParentSwipe(savedMatrix,event.getX(), event.getY());
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mParent.setUserInputEnabled(false);
                    mode = NONE;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        float dx = event.getX() - start.x;
                        float dy = event.getY() - start.y;

                        boolean isFillScreenWidth = matrixValues[SCALE_X] <= getScaleToFillScreenWidth();
                        boolean isFillScreenHeight = matrixValues[SCALE_Y] <= getScaleToFillScreenHeight();

                        if(isFillScreenWidth)
                            dx = 0;
                        if(isFillScreenHeight)
                            dy = 0;

                        matrix.set(savedMatrix);
                        matrix.postTranslate(dx, dy);
                        matrix.getValues(matrixValues);

                        // If no fill screen width or height, cannot translate
                        if(!isFillScreenWidth)
                            matrixValues[TRANSFORM_X] = Math.max(Math.min(0.0f, matrixValues[TRANSFORM_X]), -(getImageRight(matrix) - matrixValues[TRANSFORM_X] - imageViewWidth));
                        if(!isFillScreenHeight)
                            matrixValues[TRANSFORM_Y] = Math.max(Math.min(0.0f, matrixValues[TRANSFORM_Y]), -(getImageBottom(matrix) - matrixValues[TRANSFORM_Y] - imageViewHeight));
                        matrix.setValues(matrixValues);

                    } else if (mode == ZOOM) {

                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            float scale = newDist / oldDist;
                            float midX = mid.x;
                            float midY = mid.y;

                            bufferMatrix.set(savedMatrix);
                            bufferMatrix.postScale(scale, scale, midX, midY);
                            bufferMatrix.getValues(matrixValues);

                            float absScaleX = matrixValues[SCALE_X];
                            float absScaleY = matrixValues[SCALE_Y];

                            if(absScaleX > 1 && absScaleY > 1) {
                                matrix.set(savedMatrix);

                                boolean isFillScreenWidth = absScaleX >= getScaleToFillScreenWidth();
                                boolean isFillScreenHeight = absScaleY >= getScaleToFillScreenHeight();

                                // If do not fill screen width or height, change the midpoint of zoom to center
                                if(!isFillScreenWidth)
                                    midX = imageViewWidth / 2.0f;
                                if(!isFillScreenHeight)
                                    midY = imageViewHeight / 2.0f;

                                matrix.postScale(scale, scale, midX, midY);
                                matrix.getValues(matrixValues);

                                // If the image scale until cannot fill the screen width of height, we should move the image to center
                                if(isFillScreenWidth) {
                                    matrixValues[TRANSFORM_X] = Math.max(Math.min(0.0f, matrixValues[TRANSFORM_X]), -(getImageRight(matrix) - matrixValues[TRANSFORM_X] - imageViewWidth));
                                } else {
                                    float mid = (matrixValues[TRANSFORM_X] + getImageRight(matrix)) / 2;
                                    float offsetToCenter =  mid - (imageViewWidth / 2.0f);
                                    matrixValues[TRANSFORM_X] = matrixValues[TRANSFORM_X] - offsetToCenter;
                                }

                                if(isFillScreenHeight) {
                                    matrixValues[TRANSFORM_Y] = Math.max(Math.min(0.0f, matrixValues[TRANSFORM_Y]), -(getImageBottom(matrix) - matrixValues[TRANSFORM_Y] - imageViewHeight));
                                } else {
                                    float mid = (matrixValues[TRANSFORM_Y] + getImageBottom(matrix)) / 2;
                                    float offsetToCenter =  mid - (imageViewHeight / 2.0f);
                                    matrixValues[TRANSFORM_Y] = matrixValues[TRANSFORM_Y] - offsetToCenter;
                                }

                                matrix.setValues(matrixValues);
                            }
                        }
                    }
                    break;
            }

            view.setImageMatrix(matrix);
            return true;
        }

    }
}
