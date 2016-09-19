package org.yarnapps.comicshub.providers;

import android.content.Context;
import android.util.Log;

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
import java.util.Collections;

public class GogoComicProvider extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_popular};
    protected static final String sortUrls[] = {""};

    /* Genres if added later
    protected static final int genres[] = {R.string.genre_all, R.string.genre_marvel, R.string.genre_DC, R.string.genre_Vertigo, R.string.genre_darkHorse,
            R.string.genre_action, R.string.genre_adventure, R.string.genre_comedy, R.string.genre_crime, R.string.genre_cyborgs, R.string.genre_demons,
            R.string.genre_drama, R.string.genre_fantasy, R.string.genre_gore, R.string.genre_graphicsNovels, R.string.genre_historical, R.string.genre_horror,
            R.string.genre_magic, R.string.genre_martialarts, R.string.genre_mature, R.string.genre_mecha, R.string.genre_military, R.string.genre_movieCinematic,
            R.string.genre_mystery, R.string.genre_mythology, R.string.genre_psychological, R.string.genre_robots, R.string.genre_romance, R.string.genre_sci_fi,
            R.string.genre_sports, R.string.genre_spy, R.string.genre_supernatural, R.string.genre_suspense};
    protected static final String genreUrls[] = {"", "Marvel", "DC+Comics", "Vertigo", "Dark+Horse", "Action",
            "Adventure", "Comedy", "Crime", "Cyborgs", "Demons", "Drama", "Fantasy", "Gore", "Graphic+Novels", "Historical",
            "Horror", "Magic", "Martial+Arts", "Mature", "Mecha", "Military", "Movie+Cinematic+Link", "Mystery", "Mythology", "Psychological",
            "Robots", "Romance", "Science+Fiction", "Sports", "Spy", "Supernatural", "Suspense"};
     */

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document;
        int index = 1;

        if(page == 0) {//Regular sort
            document = getPage("http://www.gogocomic.net/popular.html");
        }
        else{
            document = getPage("http://www.gogocomic.net/popular/" + (page + 1) + ".html");
        }

        MangaInfo manga;
        Element elements = document.select("ul.list-comic-2").first();

        for (Element o : elements.children()) {
            try{
                manga = new MangaInfo();
                manga.name = o.select("h4").text();
                try {
                    manga.subtitle = o.select("span").text();
                } catch (Exception e) {
                    manga.subtitle = "";
                }
                manga.genres = o.select("ul.genres").text();
                manga.path = "http://gogocomic.net" + o.select("a").first().attr("href");
                manga.preview = o.select("img").first().attr("src");
                manga.provider = GogoComicProvider.class;
                manga.id = manga.path.hashCode();
                list.add(manga);
            } catch(Exception e){
                Log.e("GogoComicProvider", "Catched an exception on comic list");
            }
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            Document document = getPage(mangaInfo.path);
            Element e = document.body();

            Element desc = e.select("div.description").first();

            //children is 11
            String description = desc.childNode(11).toString();
            description = description.replace("</p>", "");
            summary.description = description.replace("<p>Summary:", "");
            MangaChapter chapter;
            Elements es = e.getElementsByClass("list-chapter");
            for (Element o : es.select("li")) {
                chapter = new MangaChapter();
                chapter.name = o.attr("title");
                chapter.readLink = "http://gogocomic.net" + o.select("a").attr("href");
                chapter.provider = summary.provider;
                summary.chapters.add(chapter);
            }
            Collections.reverse(summary.chapters);
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

            Elements e = document.body().select(".view-single");

            for (Element o : e) {
                page = new MangaPage(o.select("img").attr("src"));
                page.provider = GogoComicProvider.class;
                pages.add(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pages;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.path;
    }

    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.empty();
        }
        MangaList list = new MangaList();
        String searchUrl = "http://www.gogocomic.net/search/" + query + ".html";
        Document document = getPage(searchUrl);

        MangaInfo manga;
        Element elements = document.select("ul.list-comic-2").first();
        for (Element o : elements.children()) {
            try{
                manga = new MangaInfo();
                manga.name = o.select("h4").text();
                try {
                    manga.subtitle = o.select("span").text();
                } catch (Exception e) {
                    manga.subtitle = "";
                }
                manga.genres = o.select("ul.genres").text();
                manga.path = "http://gogocomic.net" + o.select("a").first().attr("href");
                manga.preview = o.select("img").first().attr("src");
                manga.provider = GogoComicProvider.class;
                manga.id = manga.path.hashCode();
                list.add(manga);
            } catch(Exception e){
                Log.e("GogoComicProvider", "Catched an exception on comic list");
            }
        }
        return list;
    }

    @Override
    public String getName() {
        return "Gogo Comics";
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

}