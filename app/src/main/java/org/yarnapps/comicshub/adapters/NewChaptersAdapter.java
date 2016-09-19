package org.yarnapps.comicshub.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.activities.MangaPreviewActivity;
import org.yarnapps.comicshub.components.AsyncImageView;
import org.yarnapps.comicshub.items.MangaInfo;
import org.yarnapps.comicshub.lists.MangaList;

public class NewChaptersAdapter extends RecyclerView.Adapter<NewChaptersAdapter.UpdatesHolder> {

    private final MangaList mDataset;

    public NewChaptersAdapter(MangaList dataset) {
        this.mDataset = dataset;
    }

    @Override
    public UpdatesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UpdatesHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_updates, parent, false));
    }

    @Override
    public void onBindViewHolder(UpdatesHolder holder, int position) {
        holder.fill(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    static class UpdatesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextViewTitle;
        private final AsyncImageView mAsyncImageView;
        private final TextView mTextViewBadge;
        private final TextView mTextViewSubtitle;
        private MangaInfo mData;

        public UpdatesHolder(View itemView) {
            super(itemView);
            mAsyncImageView = (AsyncImageView) itemView.findViewById(R.id.imageView);
            mTextViewTitle = (TextView) itemView.findViewById(R.id.textView_title);
            mTextViewSubtitle = (TextView) itemView.findViewById(R.id.textView_subtitle);
            mTextViewBadge = (TextView) itemView.findViewById(R.id.textView_badge);
            itemView.setOnClickListener(this);
        }

        public void fill(MangaInfo manga) {
            mData = manga;
            mAsyncImageView.setImageAsync(mData.preview);
            mTextViewTitle.setText(mData.name);
            mTextViewSubtitle.setText(mData.subtitle);
            mTextViewBadge.setText(mData.extra);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, MangaPreviewActivity.class);
            intent.putExtras(mData.toBundle());
            context.startActivity(intent);
        }
    }
}
