package org.yarnapps.comicshub.items;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.yarnapps.comicshub.lists.ChaptersList;

public class MangaSummary extends MangaInfo {

    @Deprecated
    public String readLink;
    public String description;
    @NonNull
    public ChaptersList chapters;

    public MangaSummary(MangaInfo mangaInfo) {
        id = mangaInfo.id;
        this.name = mangaInfo.name;
        this.genres = mangaInfo.genres;
        this.path = mangaInfo.path;
        this.preview = mangaInfo.preview;
        this.subtitle = mangaInfo.subtitle;
        this.provider = mangaInfo.provider;
        this.status = mangaInfo.status;
        this.extra = mangaInfo.extra;
        this.description = "";
        this.readLink = "";
        this.chapters = new ChaptersList();
    }

    public MangaSummary(MangaSummary mangaSummary) {
        id = mangaSummary.id;
        this.name = mangaSummary.name;
        this.genres = mangaSummary.genres;
        this.path = mangaSummary.path;
        this.preview = mangaSummary.preview;
        this.subtitle = mangaSummary.subtitle;
        this.provider = mangaSummary.provider;
        this.description = mangaSummary.description;
        this.readLink = mangaSummary.readLink;
        this.status = mangaSummary.status;
        this.extra = mangaSummary.extra;
        this.chapters = new ChaptersList(mangaSummary.chapters);
    }

    public MangaSummary(Bundle bundle) {
        super(bundle);
        this.readLink = bundle.getString("readlink");
        this.description = bundle.getString("description");
        chapters = new ChaptersList(bundle);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = super.toBundle();
        bundle.putString("readlink", readLink);
        bundle.putString("description", description);
        bundle.putAll(chapters.toBundle());
        return bundle;
    }

    public String getReadLink() {
        return readLink;
    }

    public String getDescription() {
        return description;
    }

    @NonNull
    public ChaptersList getChapters() {
        return chapters;
    }

    /**
     * Uses then manga has only one chapter
     * If provider doesn't support table of contents
     */
    public void addDefaultChapter() {
        MangaChapter chapter = new MangaChapter();
        chapter.provider = this.provider;
        chapter.name = this.name;
        chapter.readLink = this.readLink;
        chapters.add(chapter);
    }
}
