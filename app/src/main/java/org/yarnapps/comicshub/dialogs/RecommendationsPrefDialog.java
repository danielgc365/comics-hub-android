package org.yarnapps.comicshub.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;

import org.yarnapps.comicshub.R;

public class RecommendationsPrefDialog implements View.OnClickListener {

    private final Dialog mDialog;
    private final FilterSortDialog.Callback mCallback;

    public RecommendationsPrefDialog(final Context context, FilterSortDialog.Callback callback) {
        @SuppressLint("InflateParams")
        View contentView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_recommendprefs, null);
        final CheckedTextView checkedTextViewFav = (CheckedTextView) contentView.findViewById(R.id.checkedTextView_fav);
        final CheckedTextView checkedTextViewHist = (CheckedTextView) contentView.findViewById(R.id.checkedTextView_hist);
        final CheckedTextView checkedTextViewMatch = (CheckedTextView) contentView.findViewById(R.id.checkedTextView_match);

        checkedTextViewFav.setOnClickListener(this);
        checkedTextViewHist.setOnClickListener(this);
        checkedTextViewMatch.setOnClickListener(this);

        final SharedPreferences prefs = context.getSharedPreferences("recommendations", Context.MODE_PRIVATE);
        checkedTextViewFav.setChecked(prefs.getBoolean("fav", true));
        checkedTextViewHist.setChecked(prefs.getBoolean("hist", true));
        checkedTextViewMatch.setChecked(prefs.getBoolean("match", false));

        mDialog = new AlertDialog.Builder(context)
                .setView(contentView)
                .setCancelable(true)
                .setTitle(R.string.action_recommendations)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit()
                                .putBoolean("fav", checkedTextViewFav.isChecked())
                                .putBoolean("hist", checkedTextViewHist.isChecked())
                                .putBoolean("match", checkedTextViewMatch.isChecked())
                                .apply();
                        mCallback.onApply(
                                0, checkedTextViewMatch.isChecked() ? 100 : 50, null, null
                        );
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        mCallback = callback;
    }

    public void show() {
        mDialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v instanceof Checkable) {
            ((Checkable)v).toggle();
        }
    }
}
