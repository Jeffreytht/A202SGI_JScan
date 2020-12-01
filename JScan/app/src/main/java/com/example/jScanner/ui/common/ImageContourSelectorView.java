package com.example.jScanner.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.jScanner.R;
import com.example.jScanner.utility.ImageProcessing;

import org.opencv.core.Point;

public class ImageContourSelectorView extends View {

    // Image
    private Bitmap mBitmap = null;
    private Bitmap mCroppedBitmap = null;
    private Point[] mPoints;

    private final int RADIUS_OF_POINT = 30;
    private final Matrix matrix = new Matrix();
    private final RectF mBitmapRect = new RectF();
    private final RectF mCroppedBitmapRect = new RectF();
    private final RectF mCanvasRect = new RectF();
    private final Paint paint = new Paint();
    private final Context mContext;

    // Calibration
    private float mScaleFactor = 1.0f;
    private final android.graphics.Point mOffset = new android.graphics.Point(0, 0);

    // Event
    private Point selectedPoint;

    //Buffer
    private final int[] mPointCoordinateBuffer = new int[8];

    public ImageContourSelectorView(Context context) {
        super(context);
        this.mContext = context;
    }

    public void initScene(Bitmap bitmap, Point[] contour) {
        this.mBitmap = bitmap;
        this.mPoints = contour;
        invalidate();
    }

    private void initCroppedBitmap(int xTouch, int yTouch) {

    /*
           Assuming that x1, y1 is (0,0) of cropped image and x2,y2 is (0,0) of original big image

           (x1, y1)
                X-------------+
                |             |
                | (x2,y2)     |
                |     +-------|----------+
                |     |       |          |
                |     |       |          |
                +-----+-------+          |
                      |                  |
                      |                  |
                      |                  |
                      |                  |
                      +------------------+

            As the (0,0) of both image is different, we need to calculate the offsetX and offsetY
            Formula
            --------
            offsetX = x2 - x1
            offsetY = y2 - y1
     */

        final float CROP_FRAME = 0.15f;
        int cropWidth = (int) (mBitmapRect.width() * 2 * CROP_FRAME);
        int cropHeight = (int) (mBitmapRect.width() * 2 * CROP_FRAME);
        int oriStartX = (int) (xTouch - mBitmapRect.width() * CROP_FRAME);
        int oriStartY = (int) (yTouch - mBitmapRect.width() * CROP_FRAME);
        int oriWidthHeight = (int) (mBitmapRect.width() * 2 * CROP_FRAME);

        int finalStartX = Math.max(Math.min(oriStartX, mBitmap.getWidth()), 0);
        int finalStartY = Math.max(Math.min(oriStartY, mBitmap.getHeight()), 0);
        int finalWidth = Math.min(oriWidthHeight, mBitmap.getWidth() - finalStartX);
        int finalHeight = Math.min(oriWidthHeight, mBitmap.getHeight() - finalStartY);

        int offsetX = finalStartX - oriStartX;
        int offsetY = finalStartY - oriStartY;


        Bitmap tempBp = Bitmap.createBitmap(
                mBitmap
                , finalStartX
                , finalStartY
                , finalWidth
                , finalHeight
        );

        mCroppedBitmap = Bitmap.createBitmap(
                cropWidth
                , cropHeight
                , Bitmap.Config.ARGB_8888);
        mCroppedBitmap.eraseColor(ContextCompat.getColor(mContext, R.color.colorSecondaryBackground));
        Canvas canvas = new Canvas(mCroppedBitmap);

        canvas.drawBitmap(tempBp, offsetX, offsetY, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handle = false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                int xTouch = (int) event.getX();
                int yTouch = (int) event.getY();

                selectedPoint = null;

                // Times 2 for tolerance
                int minDistance = RADIUS_OF_POINT * 2;
                for (Point s : mPoints) {
                    // sqrt((x2 - x1) ^ 2 + (y2 - y1) ^ 2))
                    int pointToTouchDiff = (int) Math.sqrt(Math.pow(xTouch - pointXtoCanvasX((int) s.x), 2) + Math.pow(yTouch - pointYtoCanvasY((int) s.y), 2));
                    if (pointToTouchDiff < minDistance) {
                        minDistance = pointToTouchDiff;
                        selectedPoint = s;
                    }
                }

                if (selectedPoint != null) {
                    initCroppedBitmap((int) selectedPoint.x, (int) selectedPoint.y);
                    handle = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                xTouch = (int) Math.max(Math.min(event.getX(), mBitmapRect.right), mBitmapRect.left);
                yTouch = (int) Math.max(Math.min(event.getY(), mBitmapRect.bottom), mBitmapRect.top);

                if (selectedPoint != null) {
                    selectedPoint.set(new double[]{canvasXtoPointX(xTouch), canvasYtoPointY(yTouch)});
                    initCroppedBitmap((int) selectedPoint.x, (int) selectedPoint.y);
                    handle = true;
                }
                break;

            case MotionEvent.ACTION_UP:
                selectedPoint = null;
                handle = true;
                break;
        }

        if (handle)
            invalidate();

        return super.onTouchEvent(event) || handle;
    }

    private void updateBitmapToCanvasDiff() {
        final float BITMAP_MARGIN_X = 0.1f;

        // Set canvas margin so that user can locate cross hair at border easily
        mCanvasRect.set(getWidth() * BITMAP_MARGIN_X, getHeight() * BITMAP_MARGIN_X, getWidth() - getWidth() * BITMAP_MARGIN_X, getHeight() - getHeight() * BITMAP_MARGIN_X);
        mBitmapRect.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());

        // Map bitmap space to canvas space
        matrix.setRectToRect(mBitmapRect, mCanvasRect, Matrix.ScaleToFit.CENTER);
        matrix.mapRect(mBitmapRect);

        if (selectedPoint != null)
            mCroppedBitmapRect.set(200, 200, mCroppedBitmap.getWidth() + 200, mCroppedBitmap.getHeight() + 200);
        mScaleFactor = (float) getWidth() * (1 - BITMAP_MARGIN_X * 2) / mBitmap.getWidth();
        mOffset.set((int) (mBitmapRect.left), (int) mBitmapRect.top);
    }

    private int pointXtoCanvasX(int x) {
        return (int) (x * mScaleFactor + mOffset.x);
    }

    private int pointYtoCanvasY(int y) {
        return (int) (y * mScaleFactor + mOffset.y);
    }

    // x1 * scaleFactor + offset = x2
    // x1 * scaleFactor = x2 - offset
    // x1 = (x2 - offset) / scaleFactor
    private int canvasXtoPointX(int x) {
        return (int) ((x - mOffset.x) / mScaleFactor);
    }

    private int canvasYtoPointY(int y) {
        return (int) ((y - mOffset.y) / mScaleFactor);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("ImageEditor", "Drawing");
        updateBitmapToCanvasDiff(); // Must call function

        canvas.drawBitmap(mBitmap, matrix, null);

        // Draw point (contour)
        // Expected sequence = TL -> TR -> BR -> BL
        // Obtained sequence = TL -> TR -> BL -> BR
        // Swap BL and BR
        ImageProcessing.sortPoint(mPoints); // Sort the points ease draw line
        Point temp = mPoints[3];
        mPoints[3] = mPoints[2];
        mPoints[2] = temp;

        // Init buffer point
        // Graphic item
        int totalPoint = 4;
        for (int i = 0; i < totalPoint; i++) {
            mPointCoordinateBuffer[i * 2] = pointXtoCanvasX((int) mPoints[i].x);
            mPointCoordinateBuffer[i * 2 + 1] = pointYtoCanvasY((int) mPoints[i].y);
        }

        // Draw point border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ContextCompat.getColor(mContext, R.color.colorCyan));
        paint.setStrokeWidth(5);
        for (int i = 0; i < totalPoint; i++) {
            canvas.drawCircle(mPointCoordinateBuffer[i * 2]
                    , mPointCoordinateBuffer[i * 2 + 1]
                    , RADIUS_OF_POINT
                    , paint);

            canvas.drawLine(mPointCoordinateBuffer[i * 2]
                    , mPointCoordinateBuffer[i * 2 + 1]
                    , mPointCoordinateBuffer[((i + 1) * 2) % (totalPoint * 2)]
                    , mPointCoordinateBuffer[(((i + 1) * 2) + 1) % (totalPoint * 2)], paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(mContext, R.color.colorWhiteTransparent));
        for (int i = 0; i < totalPoint; i++) {
            canvas.drawCircle(mPointCoordinateBuffer[i * 2]
                    , mPointCoordinateBuffer[i * 2 + 1]
                    , RADIUS_OF_POINT
                    , paint);
        }

        // Draw cross hair and crop image
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ContextCompat.getColor(mContext, R.color.colorCyan));
        if (selectedPoint != null) {
            // 20% because count from center
            int crossHairLength = (int) (mCroppedBitmapRect.width() * 0.1);
            int centerX = (int) mCroppedBitmapRect.centerX();
            int centerY = (int) mCroppedBitmapRect.centerY();
            canvas.drawBitmap(mCroppedBitmap, null, mCroppedBitmapRect, null);
            canvas.drawLine(centerX - crossHairLength
                    , centerY
                    , centerX + crossHairLength
                    , centerY
                    , paint);
            canvas.drawLine(centerX
                    , centerY - crossHairLength
                    , centerX
                    , centerY + crossHairLength
                    , paint);

            canvas.drawRect(mCroppedBitmapRect, paint);
        }
    }
}


