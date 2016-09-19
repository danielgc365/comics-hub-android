package org.yarnapps.comicshub.adapters;

import android.support.v7.widget.RecyclerView;

public interface OnItemLongClickListener<VH extends RecyclerView.ViewHolder> {
    boolean onItemLongClick(VH viewHolder);
}
