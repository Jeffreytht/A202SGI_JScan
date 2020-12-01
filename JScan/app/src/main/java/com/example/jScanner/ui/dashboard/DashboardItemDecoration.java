package com.example.jScanner.ui.dashboard;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardItemDecoration extends RecyclerView.ItemDecoration {
    private final int columns;
    private final int margin;

    public DashboardItemDecoration(@IntRange(from = 0) int margin, @IntRange(from = 0) int columns, DisplayMetrics displayMetrics) {
        this.margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margin, displayMetrics);
        this.columns = columns;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildLayoutPosition(view);
        //set right margin to all
        outRect.right = margin;
        //set bottom margin to all
        outRect.bottom = margin;
        //we only add top margin to the first row
        if (position < columns) {
            outRect.top = margin;
        }
        //add left margin only to the first column
        if (position % columns == 0) {
            outRect.left = margin;
        }
    }
}
