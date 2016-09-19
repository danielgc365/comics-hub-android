package org.yarnapps.comicshub.providers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.items.MangaChapter;
import org.yarnapps.comicshub.items.MangaInfo;
import org.yarnapps.comicshub.items.MangaPage;
import org.yarnapps.comicshub.items.MangaSummary;
import org.yarnapps.comicshub.lists.MangaList;

import java.util.ArrayList;

public class ComiCastleProvider extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_popular, R.string.sort_alphabetical, R.string.sort_latest};
    protected static final String sortUrls[] = {"views", "name", "last_update"};

    protected static final int genres[] = {R.string.genre_all, R.string.genre_marvel, R.string.genre_DC, R.string.genre_Vertigo, R.string.genre_darkHorse, R.string.genre_DynamiteEntertainment, R.string.genre_Zenescope, R.string.genre_avatarPress, R.string.genre_max, R.string.genre_boomStudios, R.string.genre_valiant, R.string.oniPress, R.string.genre_idw, R.string.genre_actionLab,
            R.string.genre_teenTitans, R.string.genre_justiceLeague, R.string.genre_Xmen, R.string.genre_Avengers, R.string.genre_runaways,
            R.string.genre_Batman, R.string.genre_nightwing, R.string.genre_flash, R.string.genre_greenLantern, R.string.genre_deadpool, R.string.genre_dareDevil, R.string.genre_spiderMan};
    protected static final String genreUrls[] = {"", "marvel", "dc", "vertigo", "dark%20horse", "dynamite%20entertainment", "zenescope", "avatar%20press", "max", "boom!%20studios", "valiant", "oni%20press", "idw", "action%20lab",
            "teen%20titans", "justice%20league","x-men", "avengers", "runaways", "batman", "nightwing", "flash", "green%20lantern", "deadpool", "daredevil", "spider-man"};

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document;

        String mainUrl = "http://comicastle.org/manga-list.html?listType=pagination&page=" + (page + 1)
                + "&artist=&author=&name=&genre=" + (genreUrls[genre])
                + "&sort=" + (sortUrls[sort])
                + "&sort_type=DESC";
        document = getPage(mainUrl);

        MangaInfo manga;
        Elements elements = document.body().select("div.media");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            try {
                Elements subtitle = o.select("span");
                manga.subtitle = subtitle.text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            Elements elementsInner = o.select("small");
            //Elements aTags = elementsInner.select("a");
            manga.genres = elementsInner.text();
            manga.path = "http://comicastle.org/" + o.select("a").first().attr("href");
            manga.preview = "http://comicastle.org/" + o.select("img").first().attr("src");
            manga.provider = ComiCastleProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            Document document = getPage(mangaInfo.path);
            Element e = document.body();
            Elements paragraphs = e.select("p");
            summary.description = paragraphs.get(1).text();
            MangaChapter chapter;
            Elements es = e.select("[class=table table-hover]");
            Elements table = es.select("tr");
            for (Element o : table) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = "http://comicastle.org/" + o.select("a").attr("href");
                chapter.provider = summary.provider;
                summary.chapters.add(chapter);
            }
            summary.readLink = summary.chapters.get(0).readLink;
            summary.chapters.enumerate();
            return summary;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        try {
            Document document = getPage(readLink);
            MangaPage page;
            Elements e = document.body().select("select");
            for (Element o : e.get(1).select("option")) {
                page = new MangaPage("http://comicastle.org/" + o.attr("value"));
                page.provider = ComiCastleProvider.class;
                pages.add(page);
            }
            if (pages.size() != 0) {
                pages.remove(pages.size() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pages;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.path);
            Elements es = document.select("img.chapter-img");
            String temp = es.attr("abs:src");
            String temp2 = temp.replace(" ", "%20");
            return temp2;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.empty();
        }
        MangaList list = new MangaList();

        query = query.replace(" ", "%20");

        String searchUrl = "http://comicastle.org/manga-list.html?listType=pagination&page=" + (page + 1)
                + "&artist=&author=&name=" + (query)
                + "&genre=&sort=views&sort_type=DESC";
        Document document = getPage(searchUrl);

        MangaInfo manga;
        Elements elements = document.body().select("div.media");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            try {
                Elements subtitle = o.select("span");
                manga.subtitle = subtitle.text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            Elements elementsInner = o.select("small");
            //Elements aTags = elementsInner.select("a");
            manga.genres = elementsInner.text();
            manga.path = "http://comicastle.org/" + o.select("a").first().attr("href");
            manga.preview = "http://comicastle.org/" + o.select("img").first().attr("src");
            manga.provider = ComiCastleProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public String getName() {
        return "Comicastle";
    }

    @Override
    public boolean hasGenres() {
        return true;
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return super.getTitles(context, genres);
    }

}

