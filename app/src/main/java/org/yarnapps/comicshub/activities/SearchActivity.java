package org.yarnapps.comicshub.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.yarnapps.comicshub.MangaListLoader;
import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.helpers.ListModeHelper;
import org.yarnapps.comicshub.items.ThumbSize;
import org.yarnapps.comicshub.lists.MangaList;
import org.yarnapps.comicshub.providers.MangaProvider;
import org.yarnapps.comicshub.providers.staff.MangaProviderManager;
import org.yarnapps.comicshub.utils.InternalLinkMovement;
import org.yarnapps.comicshub.utils.LayoutUtils;

public class SearchActivity extends BaseAppActivity implements
        View.OnClickListener, MangaListLoader.OnContentLoadListener,
        ListModeHelper.OnListModeListener, InternalLinkMovement.OnLinkClickListener {

    //views
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mTextViewHolder;
    //utils
    private MangaListLoader mLoader;
    private MangaProvider mProvider;
    private ListModeHelper mListModeHelper;
    //data
    private String mQuery;
    @Nullable
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mTextViewHolder.setMovementMethod(new InternalLinkMovement(this));
        Bundle extras = getIntent().getExtras();
        mQuery = extras.getString("query");
        mTitle = extras.getString("title");
        int provider = extras.getInt("provider");
        mProvider = new MangaProviderManager(this).getProvider(provider);
        if (mProvider == null) {
            finish();
            return;
        }
        if (mTitle != null) {
            setTitle(mQuery);
        }
        enableHomeAsUp();
        setSubtitle(mTitle == null ? mQuery : mTitle);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mLoader = new MangaListLoader(mRecyclerView, this);
        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();
        updateContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menu.findItem(R.id.action_search).setVisible(mTitle == null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
    }

    @Override
    protected void onDestroy() {
        mListModeHelper.disable();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_listmode:
                mListModeHelper.showDialog();
                return true;
            case R.id.action_search:
                onClick(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(SearchActivity.this, MultipleSearchActivity.class)
                .putExtra("query", mQuery));
    }

    @Override
    public void onContentLoaded(boolean success) {
        mProgressBar.setVisibility(View.GONE);
        if (mLoader.getContentSize() == 0) {
            mTextViewHolder.setVisibility(View.VISIBLE);
            Snackbar.make(mRecyclerView, R.string.no_manga_found,Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.more, this).show();
        }
    }

    @Override
    public void onLoadingStarts(int page) {
        if (page == 0) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mTextViewHolder.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public MangaList onContentNeeded(int page) {
        try {
            return mProvider.search(mQuery, page);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onListModeChanged(boolean grid, int sizeMode) {
        int spans;
        ThumbSize thumbSize;
        switch (sizeMode) {
            case -1:
                spans = LayoutUtils.isTabletLandscape(this) ? 2 : 1;
                thumbSize = ThumbSize.THUMB_SIZE_LIST;
                break;
            case 0:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_SMALL);
                break;
            case 1:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_MEDIUM);
                break;
            case 2:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_LARGE);
                break;
            default:
                return;
        }
        mLoader.updateLayout(grid, spans, thumbSize);
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (url) {
            case "update":
                updateContent();
                break;
        }
    }

    private void updateContent() {
        if (checkConnection()) {
            mLoader.loadContent(mProvider.isMultiPage(), true);
        } else {
            mLoader.clearItemsLazy();
            mTextViewHolder.setText(Html.fromHtml(getString(R.string.no_network_connection_html)));
            mTextViewHolder.setVisibility(View.VISIBLE);
        }
    }
}
