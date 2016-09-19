package org.yarnapps.comicshub.items;

import android.os.Bundle;

import org.yarnapps.comicshub.providers.LocalMangaProvider;

public class MangaChapter {

    public int id;
    public String name;
    public int number;
    public String readLink;
    public Class<?> provider;

    public MangaChapter() {
        number = -1;
    }

    public MangaChapter(Bundle bundle) {
        id = bundle.getInt("id");
        name = bundle.getString("name");
        readLink = bundle.getString("readLink");
        number = bundle.getInt("number");
        try {
            provider = Class.forName(bundle.getString("provider"));
        } catch (ClassNotFoundException e) {
            provider = LocalMangaProvider.class;
        }
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("name", name);
        bundle.putString("readLink", readLink);
        bundle.putInt("number", number);
        bundle.putString("provider", provider.getName());
        return bundle;
    }

}
