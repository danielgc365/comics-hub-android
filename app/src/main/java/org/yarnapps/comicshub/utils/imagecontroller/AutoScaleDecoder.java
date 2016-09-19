package org.yarnapps.comicshub.utils.imagecontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.yarnapps.comicshub.OpenMangaApplication;

public class AutoScaleDecoder extends SkiaImageDecoder {

    @Override
    public Bitmap decode(Context context, Uri uri) throws Exception {
        return ImageLoader.getInstance().loadImageSync(uri.toString(),
                OpenMangaApplication.getImageLoaderOptionsBuilder()
                        .imageScaleType(ImageScaleType.NONE_SAFE)
                        .build());
    }
}
