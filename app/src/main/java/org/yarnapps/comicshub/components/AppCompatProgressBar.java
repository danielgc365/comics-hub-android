package org.yarnapps.comicshub.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import org.yarnapps.comicshub.utils.LayoutUtils;

public class AppCompatProgressBar extends ProgressBar {

    private PorterDuffColorFilter mColorFilter;

    public AppCompatProgressBar(Context context) {
        super(context);
        init(context);
    }

    public AppCompatProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AppCompatProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AppCompatProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mColorFilter = new PorterDuffColorFilter(LayoutUtils.getAccentColor(context), PorterDuff.Mode.SRC_IN);
        applyColorFilter(getProgressDrawable());
        applyColorFilter(getIndeterminateDrawable());
    }

    private void applyColorFilter(@Nullable Drawable d) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && d != null) {
            d.setColorFilter(mColorFilter);
        }
    }

    @Override
    public void setProgressDrawable(Drawable d) {
        applyColorFilter(d);
        super.setProgressDrawable(d);
    }

    @Override
    public void setIndeterminateDrawable(Drawable d) {
        applyColorFilter(d);
        super.setIndeterminateDrawable(d);
    }
}
