package org.yarnapps.comicshub.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.items.MangaPage;
import org.yarnapps.comicshub.utils.FileLogger;
import org.yarnapps.comicshub.utils.InternalLinkMovement;
import org.yarnapps.comicshub.utils.imagecontroller.PageLoadAbs;

import java.util.ArrayList;

public class PagerReaderAdapter extends PagerAdapter implements InternalLinkMovement.OnLinkClickListener {

    public static final int SCALE_FIT = 0;
    public static final int SCALE_CROP = 1;
    public static final int SCALE_AUTO = 2;
    public static final int SCALE_SRC = 3;

    private final LayoutInflater inflater;
    private final ArrayList<MangaPage> pages;
    private boolean isLandOrientation, isLight;
    private final InternalLinkMovement mLinkMovement;
    private int mScaleMode = SCALE_FIT;
    private boolean mTile;

    public PagerReaderAdapter(Context context, ArrayList<MangaPage> mangaPages) {
        inflater = LayoutInflater.from(context);
        pages = mangaPages;
        mLinkMovement = new InternalLinkMovement(this);
        isLight = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("theme", "0").equals("0");
        mTile = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("tiling", true);
    }

    public void setIsLandOrientation(boolean isLandOrientation) {
        this.isLandOrientation = isLandOrientation;
        notifyDataSetChanged();
    }

    public void setTiling(boolean tile) {
        mTile = tile;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public int getItemPosition(Object object){
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    public void setScaleMode(int scaleMode) {
        mScaleMode = scaleMode;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.item_page, container, false);
        if (isLight) {
            view.setBackgroundColor(Color.WHITE);
        }
        MangaPage page = getItem(position);
        PageHolder holder = new PageHolder();
        holder.position = position;
        holder.scale = mScaleMode;
        holder.land = isLandOrientation;
        holder.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        holder.ssiv = (SubsamplingScaleImageView) view.findViewById(R.id.ssiv);
        holder.ssiv.setScaleAndCenter(isLandOrientation ? holder.ssiv.getMaxScale() -.2f : holder.ssiv.getMinScale(), new PointF(0,0));
        holder.textView = (TextView) view.findViewById(R.id.textView_holder);
        holder.textView.setMovementMethod(mLinkMovement);
        holder.textView.setTag(holder);
        holder.loadTask = new PageLoad(holder, page, mTile);
        holder.loadTask.load();
        view.setTag(holder);
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (!(object instanceof View)) {
            return;
        }
        View view = (View) object;
        PageHolder holder = (PageHolder) view.getTag();
        if (holder != null) {
            if (holder.loadTask != null) {
                holder.loadTask.cancel();
            }
            holder.ssiv.recycle();
        }
        container.removeView(view);
    }

    public MangaPage getItem(int position) {
        return pages.get(position);
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (url) {
            case "retry":
                Object tag = view.getTag();
                if (tag == null || !(tag instanceof PageHolder)) {
                    return;
                }
                final PageHolder holder = (PageHolder) tag;
                MangaPage page = pages.get(holder.position);
                holder.loadTask = new PageLoad(holder, page, mTile);
                holder.loadTask.load();
                break;
        }
    }

    private static class PageHolder {
        ProgressBar progressBar;
        SubsamplingScaleImageView ssiv;
        TextView textView;
        @Nullable
        PageLoad loadTask;
        int position;
        boolean land;
        int scale;
    }

    private static class PageLoad extends PageLoadAbs {
        private final PageHolder viewHolder;

        PageLoad(PageHolder viewHolder, MangaPage page, boolean tile){
            super(page, viewHolder.ssiv, tile);
            this.viewHolder = viewHolder;
        }

        @Override
        protected void preLoad(){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setIndeterminate(true);
            viewHolder.textView.setVisibility(View.GONE);
        }

        @Override
        protected void onLoadingComplete() {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.ssiv.setDoubleTapZoomScale((viewHolder.ssiv.getMaxScale() + viewHolder.ssiv.getMinScale()) / 2.f);
            viewHolder.ssiv.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
            switch (viewHolder.scale) {
                case SCALE_FIT:
                    viewHolder.ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                    break;
                case SCALE_CROP:
                    viewHolder.ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                    break;
                case SCALE_AUTO:
                    viewHolder.ssiv.setMinimumScaleType(
                            (viewHolder.ssiv.getSWidth() > viewHolder.ssiv.getSHeight()) == viewHolder.land ?
                                    SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                                    : SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP
                    );
                    break;
                case SCALE_SRC:
                    viewHolder.ssiv.setScaleAndCenter(viewHolder.ssiv.getMaxScale(), new PointF(0, 0));
                    break;
            }
        }

        @Override
        public void onLoadingFailed(Exception e) {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.textView.setVisibility(View.VISIBLE);
            viewHolder.textView.setText(
                    Html.fromHtml(
                            viewHolder.textView.getContext().getString(
                                    R.string.error_loadimage_html,
                                    FileLogger.getInstance().getFailMessage(viewHolder.textView.getContext(), e)
                            )
                    )
            );
        }

        @Override
        public void onProgressUpdate(int current, int total) {
            int progress = (current * 100 / total);
            viewHolder.progressBar.setIndeterminate(false);
            viewHolder.progressBar.setProgress(progress);
        }
    }
}
