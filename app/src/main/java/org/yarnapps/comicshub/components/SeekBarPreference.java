package org.yarnapps.comicshub.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.yarnapps.comicshub.R;

public class SeekBarPreference extends Preference implements AppCompatSeekBar.OnSeekBarChangeListener {

    private TextView mTextView;
    private AppCompatSeekBar mSeekBar;
    private int mValue;
    private int mMax;
    private Drawable mIcon;
    private boolean mValueSet;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SeekBarPreferenceAttrs);
        mIcon = a.getDrawable(R.styleable.SeekBarPreferenceAttrs_iconDrawable);
        mMax = a.getInt(R.styleable.SeekBarPreferenceAttrs_max, 100);
        mValue = 20;
        a.recycle();
        mValueSet = false;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected View onCreateView(ViewGroup parent) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(
                getContext())
                .inflate(R.layout.pref_seekbar, parent, false);
        ((TextView) layout.findViewById(R.id.title)).setText(getTitle());
        mSeekBar = (AppCompatSeekBar) layout.findViewById(R.id.seekBar);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
        mSeekBar.setOnSeekBarChangeListener(this);
        ImageView imageView = (ImageView) layout.findViewById(R.id.icon);
        if (mIcon != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(mIcon);
        }
        mTextView = (TextView) layout.findViewById(R.id.value);
        mTextView.setText(String.valueOf(mValue));
        return layout;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        mTextView.setText(String.valueOf(progress));
        mTextView.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        setValue(seekBar.getProgress());
    }

    public void setValue(int value) {
        // Always persist/notify the first time.
        final boolean changed = mValue != value;
        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistInt(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mValue) : (int) defaultValue);
    }
}