package org.yarnapps.comicshub.providers.staff;

import android.support.annotation.NonNull;

import org.yarnapps.comicshub.providers.MangaProvider;

public class ProviderSummary {
    public String name;
    @NonNull
    public Class<? extends MangaProvider> aClass;
    public int lang;

    public ProviderSummary(String name, @NonNull Class<? extends MangaProvider> aClass, int lang) {
        this.name = name;
        this.aClass = aClass;
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProviderSummary that = (ProviderSummary) o;

        return aClass.equals(that.aClass);

    }

    @Override
    public int hashCode() {
        return aClass.hashCode();
    }
}
