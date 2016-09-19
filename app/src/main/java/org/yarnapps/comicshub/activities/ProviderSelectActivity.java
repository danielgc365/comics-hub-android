package org.yarnapps.comicshub.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.adapters.ProvidersAdapter;
import org.yarnapps.comicshub.components.DividerItemDecoration;

public class ProviderSelectActivity extends BaseAppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provselect);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ProvidersAdapter(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
    }
}
