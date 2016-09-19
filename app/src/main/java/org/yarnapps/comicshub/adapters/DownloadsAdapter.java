package org.yarnapps.comicshub.adapters;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.components.AsyncImageView;
import org.yarnapps.comicshub.items.DownloadInfo;
import org.yarnapps.comicshub.items.ThumbSize;
import org.yarnapps.comicshub.services.DownloadService;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadHolder>
        implements ServiceConnection, DownloadService.OnProgressUpdateListener {

    private final Intent mIntent;
    @Nullable
    private DownloadService.DownloadBinder mBinder;
    private final RecyclerView mRecyclerView;

    public DownloadsAdapter(RecyclerView recyclerView) {
        mIntent = new Intent(recyclerView.getContext(), DownloadService.class);
        mBinder = null;
        mRecyclerView = recyclerView;
    }

    public void enable() {
        mRecyclerView.getContext().bindService(mIntent, this, 0);
    }

    public void disable() {
        if (mBinder != null) {
            mBinder.removeListener(this);
        }
        mRecyclerView.getContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (DownloadService.DownloadBinder) service;
        mBinder.addListener(this);
        notifyDataSetChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
        notifyDataSetChanged();
    }

    @Override
    public DownloadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownloadHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false));
    }

    @Override
    public void onBindViewHolder(DownloadHolder holder, int position) {
        if (mBinder != null) {
            holder.fill(mBinder.getItem(position));
        }
    }

    @Override
    public int getItemCount() {
        return mBinder != null ? mBinder.getCount() : 0;
    }

    @Override
    public void onProgressUpdated(int position) {
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(position);
        if (mBinder != null && holder != null && holder instanceof DownloadHolder) {
            DownloadInfo item = mBinder.getItem(position);
            if (item.pos < item.max) {
                ((DownloadHolder) holder).updateProgress(
                        item.pos * 100 + item.getChapterProgressPercent(),
                        item.max * 100,
                        item.chaptersProgresses[item.pos],
                        item.chaptersSizes[item.pos],
                        item.chapters.get(item.pos).name
                );
            } else {
                notifyItemChanged(position);
            }
        }
    }

    @Override
    public void onDataUpdated() {
        notifyDataSetChanged();
    }

    public boolean isPaused() {
        return mBinder != null && mBinder.isPaused();
    }

    public void setTaskPaused(boolean paused) {
        if (mBinder != null) {
            mBinder.setPaused(paused);
        }
    }

    protected static class DownloadHolder extends RecyclerView.ViewHolder {
        private final AsyncImageView mAsyncImageView;
        private final TextView mTextViewTitle;
        private final TextView mTextViewSubtitle;
        private final TextView mTextViewState;
        private final TextView mTextViewPercent;
        private final ProgressBar mProgressBarPrimary;
        private final ProgressBar mProgressBarSecondary;

        public DownloadHolder(View itemView) {
            super(itemView);
            mAsyncImageView = (AsyncImageView) itemView.findViewById(R.id.imageView);
            mTextViewTitle = (TextView) itemView.findViewById(R.id.textView_title);
            mTextViewSubtitle = (TextView) itemView.findViewById(R.id.textView_subtitle);
            mTextViewState = (TextView) itemView.findViewById(R.id.textView_state);
            mProgressBarPrimary = (ProgressBar) itemView.findViewById(R.id.progressBar_primary);
            mProgressBarSecondary = (ProgressBar) itemView.findViewById(R.id.progressBar_secondary);
            mTextViewPercent = (TextView) itemView.findViewById(R.id.textView_percent);
        }

        @SuppressLint("SetTextI18n")
        public void fill(DownloadInfo data) {
            mTextViewTitle.setText(data.name);
            mAsyncImageView.setImageThumbAsync(data.preview, ThumbSize.THUMB_SIZE_LIST);
            switch (data.state) {
                case DownloadInfo.STATE_IDLE:
                    mTextViewState.setText(R.string.queue);
                    break;
                case DownloadInfo.STATE_FINISHED:
                    mTextViewState.setText(R.string.completed);
                    break;
                case DownloadInfo.STATE_RUNNING:
                    mTextViewState.setText(R.string.saving_manga);
                    break;
            }
            if (data.pos < data.max) {
                updateProgress(data.pos, data.max, data.chaptersProgresses[data.pos], data.chaptersSizes[data.pos],
                        data.chapters.get(data.pos).name);
            } else {
                mProgressBarPrimary.setProgress(mProgressBarPrimary.getMax());
                mProgressBarSecondary.setProgress(mProgressBarSecondary.getMax());
                mTextViewPercent.setText("100%");
                mTextViewSubtitle.setText(itemView.getContext().getString(R.string.chapters_total, data.max));
            }
        }

        /**
         *
         * @param tPos current chapter
         * @param tMax chapters count
         * @param cPos current page
         * @param cMax pages count
         * @param subtitle chapter name
         */
        @SuppressLint("SetTextI18n")
        public void updateProgress(int tPos, int tMax, int cPos, int cMax, String subtitle) {
            mProgressBarPrimary.setMax(tMax);
            mProgressBarPrimary.setProgress(tPos);
            mProgressBarSecondary.setMax(cMax);
            mProgressBarSecondary.setProgress(cPos);
            mTextViewSubtitle.setText(subtitle);
            mTextViewPercent.setText((tMax == 0 ? 0 : tPos * 100 / tMax) + "%");
        }
    }
}
