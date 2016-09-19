package org.yarnapps.comicshub.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

public class AutoHeightLayout extends FrameCheckLayout {

    private static final double ASPECT_RATIO = 1.6f;

    public AutoHeightLayout(Context context) {
        super(context);
    }

    public AutoHeightLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHeightLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AutoHeightLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int calculatedHeight = (int) (originalWidth * ASPECT_RATIO);
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.EXACTLY)
        );
    }
}
