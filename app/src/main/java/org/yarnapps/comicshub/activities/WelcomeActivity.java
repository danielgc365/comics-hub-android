package org.yarnapps.comicshub.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;

import org.yarnapps.comicshub.BuildConfig;
import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.utils.AppHelper;

public class WelcomeActivity extends BaseAppActivity {

    private static final int WELCOME_CHANGELOG = 1;

    /**
     *
     * @param context
     * @return true if first call
     */
    public static boolean show(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(WelcomeActivity.class.getName(), MODE_PRIVATE);
        int version = BuildConfig.VERSION_CODE;
        int lastVersion = prefs.getInt("version", -1);
        if (lastVersion < version) {
            if (lastVersion != -1 && version % 2 == 0 && prefs.getBoolean("showChangelog", true)) {
                context.startActivity(
                        new Intent(context, WelcomeActivity.class)
                                .putExtra("mode", WELCOME_CHANGELOG)
                );
            }
            prefs.edit().putInt("version", version).apply();
        }
        return lastVersion == -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //init view

        //---------
        int mode = getIntent().getIntExtra("mode", WELCOME_CHANGELOG);
        switch (mode) {
            case WELCOME_CHANGELOG:
                findViewById(R.id.page_changelog).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textView)).setText(
                        Html.fromHtml(AppHelper.getRawString(this, R.raw.changelog))
                );
                findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                findViewById(R.id.checkBox_showChangelog).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v instanceof Checkable) {
                            getSharedPreferences(WelcomeActivity.class.getName(), MODE_PRIVATE)
                                    .edit().putBoolean("showChangelog", ((Checkable) v).isChecked()).apply();
                        }
                    }
                });
                break;
            default:
                finish();
        }
    }
}
