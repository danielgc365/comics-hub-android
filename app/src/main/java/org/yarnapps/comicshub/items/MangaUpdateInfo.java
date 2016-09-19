package org.yarnapps.comicshub.items;

public class MangaUpdateInfo {

    public int mangaId;
    public String mangaName;

    public int lastChapters;
    public int chapters;

    public MangaUpdateInfo() {
    }

    public MangaUpdateInfo(int mangaId) {
        this.mangaId = mangaId;
    }

    public int getNewChapters() {
        return lastChapters > 0 ? chapters - lastChapters : 0;
    }
}
