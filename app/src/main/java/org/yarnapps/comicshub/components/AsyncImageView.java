package org.yarnapps.comicshub.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import org.yarnapps.comicshub.OpenMangaApplication;
import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.items.ThumbSize;

public class AsyncImageView extends ImageView {

    private static Drawable mDrawableHolder;
    @Nullable
    private String mUrl = null;

    public AsyncImageView(Context context) {
        this(context, null);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init(){
        if(mDrawableHolder == null) {
            mDrawableHolder = ContextCompat.getDrawable(getContext(), R.drawable.placeholder);
        }
    }

    public void setImageAsync(@Nullable String url) {
        setImageThumbAsync(url, null, true);
    }

    public void setImageAsync(@Nullable String url, boolean useHolder) {
        setImageThumbAsync(url, null, useHolder);
    }

    public void setImageThumbAsync(@Nullable String url, @Nullable ThumbSize size) {
        setImageThumbAsync(url, size, true);
    }

    public void setImageThumbAsync(@Nullable String url, @Nullable ThumbSize size, boolean useHolder) {
        if (mUrl != null && mUrl.equals(url)) {
            return;
        }
        if (useHolder) {
            setImageDrawable(mDrawableHolder);
        }
        mUrl = (url != null && url.charAt(0) == '/') ? "file://" + url : url;
        if (size != null && getMeasuredWidth() == 0) {
            ImageLoader.getInstance().displayImage(mUrl, this, new ImageSize(size.getWidth(), size.getHeight()));
        } else {
            ImageLoader.getInstance().displayImage(mUrl, this);
        }
    }

    public void updateImageAsync(String url) {
        if (mUrl != null && mUrl.equals(url)) {
            return;
        }
        mUrl = (url != null && url.charAt(0) == '/') ? "file://" + url : url;
        ImageLoader.getInstance().displayImage(mUrl, this, OpenMangaApplication
                .getImageLoaderOptionsBuilder()
                .displayer(new SimpleBitmapDisplayer())
        .build());
    }
}