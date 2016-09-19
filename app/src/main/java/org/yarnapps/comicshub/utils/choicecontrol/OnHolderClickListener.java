package org.yarnapps.comicshub.utils.choicecontrol;

import android.support.v7.widget.RecyclerView;

public interface OnHolderClickListener {
    boolean onClick(RecyclerView.ViewHolder viewHolder);
    boolean onLongClick(RecyclerView.ViewHolder viewHolder);
}
