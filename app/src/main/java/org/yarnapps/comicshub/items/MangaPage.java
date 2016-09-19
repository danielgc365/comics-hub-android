package org.yarnapps.comicshub.items;

import org.yarnapps.comicshub.providers.MangaProvider;

public class MangaPage {

    public int id;
    public String path;
    public Class<? extends MangaProvider> provider;

    public MangaPage() {
    }

    public MangaPage(String path) {
        this.path = path;
    }
}
