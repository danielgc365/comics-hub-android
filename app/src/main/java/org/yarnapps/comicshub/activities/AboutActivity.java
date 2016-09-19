package org.yarnapps.comicshub.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.utils.AppHelper;
import org.yarnapps.comicshub.utils.InternalLinkMovement;

public class AboutActivity extends BaseAppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsUp();
        TextView textView = (TextView) findViewById(R.id.textView);
        assert textView != null;
        textView.setText(Html.fromHtml(AppHelper.getRawString(this, R.raw.about)));
        textView.setMovementMethod(new InternalLinkMovement(null));
    }
}
