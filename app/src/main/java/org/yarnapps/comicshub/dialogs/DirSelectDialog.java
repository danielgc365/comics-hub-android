package org.yarnapps.comicshub.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.adapters.DirAdapter;
import org.yarnapps.comicshub.utils.LayoutUtils;
import org.yarnapps.comicshub.utils.MangaStore;

import java.io.File;

public class DirSelectDialog implements DialogInterface.OnClickListener, AdapterView.OnItemClickListener {

    private final AlertDialog mDialog;
    private final DirAdapter mAdapter;
    private final TextView mHeaderUp;
    private OnDirSelectListener mDirSelectListener;

    public DirSelectDialog(final Context context) {
        ListView listView = new ListView(context);
        mAdapter = new DirAdapter(context, MangaStore.getMangasDir(context));
        mHeaderUp = (TextView) View.inflate(context, R.layout.item_dir, null);
        mHeaderUp.setCompoundDrawablesWithIntrinsicBounds(LayoutUtils.getThemedIcons(context, R.drawable.ic_return_dark)[0],
                null, null, null);
        mHeaderUp.setMaxLines(2);
        mHeaderUp.setText(mAdapter.getCurrentDir().getPath());
        listView.addHeaderView(mHeaderUp);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        mDialog = new AlertDialog.Builder(context)
                .setView(listView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, this)
                .setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDirSelectListener != null) {
                            mDirSelectListener.onDirSelected(context.getExternalFilesDir("saved"));
                        }
                    }
                })
                .setCancelable(true)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mDirSelectListener != null) {
            mDirSelectListener.onDirSelected(mAdapter.getCurrentDir());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            File dir = mAdapter.getCurrentDir().getParentFile();
            if (dir != null) {
                mAdapter.setCurrentDir(mAdapter.getCurrentDir().getParentFile());
            }
        } else {
            mAdapter.setCurrentDir(mAdapter.getItem(position - 1));
        }
        mHeaderUp.setText(mAdapter.getCurrentDir().getPath());
        mAdapter.notifyDataSetChanged();
    }

    public DirSelectDialog setDirSelectListener(OnDirSelectListener dirSelectListener) {
        this.mDirSelectListener = dirSelectListener;
        return this;
    }

    public void show() {
        mDialog.show();
    }

    public interface OnDirSelectListener {
        void onDirSelected(File dir);
    }

}
