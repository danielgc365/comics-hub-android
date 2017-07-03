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

public class ReadComics extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_popular};
    protected static final String sortUrls[] = {""};

    // Create tags for comic genres
    protected static final int genres[] = {R.string.genre_all, R.string.genre_marvel, R.string.genre_DC, R.string.genre_Vertigo, R.string.genre_darkHorse,
            R.string.genre_action, R.string.genre_adventure, R.string.genre_comedy, R.string.genre_crime, R.string.genre_cyborgs, R.string.genre_demons,
            R.string.genre_drama, R.string.genre_fantasy, R.string.genre_gore, R.string.genre_graphicsNovels, R.string.genre_historical, R.string.genre_horror,
            R.string.genre_magic, R.string.genre_martialarts, R.string.genre_mature, R.string.genre_mecha, R.string.genre_military, R.string.genre_movieCinematic,
            R.string.genre_mystery, R.string.genre_mythology, R.string.genre_psychological, R.string.genre_robots, R.string.genre_romance, R.string.genre_sci_fi,
            R.string.genre_sports, R.string.genre_spy, R.string.genre_supernatural, R.string.genre_suspense};
    // Create strings that will be added to the URLs
    protected static final String genreUrls[] = {"", "Marvel", "DC+Comics", "Vertigo", "Dark+Horse", "Action",
            "Adventure", "Comedy", "Crime", "Cyborgs", "Demons", "Drama", "Fantasy", "Gore", "Graphic+Novels", "Historical",
            "Horror", "Magic", "Martial+Arts", "Mature", "Mecha", "Military", "Movie+Cinematic+Link", "Mystery", "Mythology", "Psychological",
            "Robots", "Romance", "Science+Fiction", "Sports", "Spy", "Supernatural", "Suspense"};


    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document;


        if(genre != 0){ //If sorted by genre
            if(page == 0){
                document = getPage("http://www.readcomics.tv/advanced-search?key=&wg=" + (genreUrls[genre])
                        + "&wog=&status=");
            } else{
                document = getPage("http://www.readcomics.tv/advanced-search?key=&wg=" + (genreUrls[genre])
                        + "&wog=&status=&page=" +(page + 1));
            }
        }
        else if(page == 0) { //Regular sort
            document = getPage("http://www.readcomics.tv/popular-comic/");
        }
        else{
            document = getPage("http://www.readcomics.tv/popular-comic/" + (page + 1));
        }

        MangaInfo manga;
        Elements elements = document.body().select("div.manga-box");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            try {
                manga.subtitle = o.select("div.detail").first().text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            manga.genres = o.select("a.tags").text();
            manga.path = o.select("a").first().attr("href");
            manga.preview = o.select("img").first().attr("src");
            manga.provider = ReadComics.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    // Go to the URL of the comic and read the HTML to find important info about the comic
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            Document document = getPage(mangaInfo.path);
            Element e = document.body();
            summary.description = e.getElementsByClass("pdesc").text();
            summary.preview = e.getElementById("series_image").absUrl("src");
            MangaChapter chapter;
            Elements es = e.getElementsByClass("ch-name");
            for (Element o : es) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = o.attr("href");
                chapter.provider = summary.provider;
                summary.chapters.add(chapter);
            }
            Elements nextChPages = e.select("div.general-nav");
            Elements nextChPagesLink = nextChPages.select("a");
            if(nextChPages.size() != 0){
                int ChPagesIndex = 2;
                while(nextChPagesLink.size() >= ChPagesIndex) {
                    try{
                        Document documentinner = getPage(mangaInfo.path + "/" + ChPagesIndex);
                        Element body = documentinner.body();
                        Elements ess = body.getElementsByClass("ch-name");
                        for (Element o : ess) {
                            chapter = new MangaChapter();
                            chapter.name = o.text();
                            chapter.readLink = o.attr("href");
                            chapter.provider = summary.provider;
                            summary.chapters.add(chapter);
                        }
                        ChPagesIndex++;
                    } catch(Exception ex){
                        break;
                    }
                }
            }
            summary.readLink = summary.chapters.get(0).readLink;
            summary.chapters.enumerate();
            return summary;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    // Create the URL links for the comic pages
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        try {
            Document document = getPage(readLink);
            MangaPage page;
            Element e = document.body().getElementById("asset_2");
            for (Element o : e.select("option")) {
                page = new MangaPage(o.attr("value"));
                page.provider = ReadComics.class;
                pages.add(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pages;
    }

    @Override
    // Get the page image
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.path);
            String temp = document.body().getElementById("main_img").attr("src");
            return temp;
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
        String searchUrl = "http://www.readcomics.tv/comic-search?key=" + query;
        Document document = getPage(searchUrl);
        MangaInfo manga;
        Elements elements = document.body().select("div.manga-box");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            try {
                manga.subtitle = o.select("div.detail").first().text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            manga.genres = o.select("a.tags").text();
            manga.path = o.select("a").first().attr("href");
            manga.preview = o.select("img").first().attr("src");
            manga.provider = ReadComics.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public String getName() {
        return "Read Comics";
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
