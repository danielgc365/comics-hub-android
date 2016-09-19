package org.yarnapps.comicshub.components.pager;

import android.support.v4.view.ViewPager;
import android.view.View;

public class VerticalPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        if (position < -1) { // [-Infinity,-1)
            view.setAlpha(0);
        } else if (position <= 1) { // [-1,1]
            view.setAlpha(1);
            view.setTranslationX(view.getWidth() * -position);
            float yPosition = position * view.getHeight();
            view.setTranslationY(yPosition);
        } else { // (1,+Infinity]
            view.setAlpha(0);
        }
    }
}