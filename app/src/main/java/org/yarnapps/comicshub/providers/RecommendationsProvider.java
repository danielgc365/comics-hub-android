package org.yarnapps.comicshub.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.helpers.StorageHelper;
import org.yarnapps.comicshub.items.MangaInfo;
import org.yarnapps.comicshub.items.MangaPage;
import org.yarnapps.comicshub.items.MangaSummary;
import org.yarnapps.comicshub.lists.MangaList;
import org.yarnapps.comicshub.providers.staff.MangaProviderManager;
import org.yarnapps.comicshub.providers.staff.ProviderSummary;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RecommendationsProvider extends MangaProvider {

    @NonNull
    private static WeakReference<RecommendationsProvider> instanceReference = new WeakReference<>(null);
    private final MangaProviderManager mProviderManager;
    private final Context mContext;
    private final StorageHelper mStorageHelper;
    private final boolean[] mConfig = new boolean[3];

    public RecommendationsProvider(Context context) {
        mContext = context;
        mProviderManager = new MangaProviderManager(mContext);
        mStorageHelper = new StorageHelper(mContext);
    }

    public static RecommendationsProvider getInstacne(Context context) {
        RecommendationsProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new RecommendationsProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        instance.updateConfig();
        return instance;
    }

    private ArrayList<String> getStatGenres(boolean fav, boolean hist) {
        final ArrayList<String> genres = new ArrayList<>();
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            if (fav) {
                cursor = database.query("favourites",  new String[]{"summary"}, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        String s = cursor.getString(0);
                        if (!TextUtils.isEmpty(s)) {
                            Collections.addAll(genres, s.split("[,]?\\s"));
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
                cursor = null;
            }
            if (hist) {
                cursor = database.query("history",  new String[]{"summary"}, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        String s = cursor.getString(0);
                        if (!TextUtils.isEmpty(s)) {
                            Collections.addAll(genres, s.toLowerCase().split("[,]?\\s"));
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
                cursor = null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
        return genres;
    }

    private int checkGenres(String summary, ArrayList<String> genres) {
        if (summary == null || summary.length() == 0) {
            return 0;
        }
        if (genres.isEmpty()) {
            return 100;
        }
        String[] parsed = summary.toLowerCase().split("[,]?\\s");
        int coincidences = 0;
        for (String o: parsed) {
            if (genres.contains(o)) {
                coincidences++;
            }
        }
        return coincidences * 100 / parsed.length;
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        final ArrayList<String> genres = getStatGenres(mConfig[0], mConfig[1]);
        final ArrayList<ProviderSummary> providers = mProviderManager.getEnabledProviders();
        final MangaList mangas = new MangaList();
        final Random random = new Random();
        final int groupCount = Math.min(providers.size(), 4);
        final int groupSize = 20 / groupCount;
        MangaList tempList;
        MangaInfo manga;
        boolean atLeastOne = false;
        for (int i=0; i<groupCount && mangas.size()<=20; i++) {
            try {
                //noinspection ConstantConditions
                tempList = MangaProviderManager.instanceProvider(mContext, providers.get(i).aClass).getList(random.nextInt(10), 0, 0);
                if (tempList == null) {
                    continue;
                }
                atLeastOne = true;
                Collections.shuffle(tempList);
                int k=0;
                for (int j=0; j<tempList.size() && k<=groupSize;j++) {
                    manga = tempList.get(j);
                    if (checkGenres(manga.genres, genres) >= (mConfig[2] ? 99 : 49)) {
                        mangas.add(manga);
                        k++;
                    }
                }
            } catch (Exception e) {
                //ы
            }
        }
        Collections.shuffle(mangas);
        return atLeastOne ? mangas : null;
    }

    @Override
    protected void finalize() throws Throwable {
        mStorageHelper.close();
        super.finalize();
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        return null;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return null;
    }

    @Override
    public String getName() {
        return mContext.getString(R.string.action_recommendations);
    }

    public void updateConfig() {
        SharedPreferences prefs = mContext.getSharedPreferences("recommendations", Context.MODE_PRIVATE);
        mConfig[0] = prefs.getBoolean("fav", true);
        mConfig[1] = prefs.getBoolean("hist", true);
        mConfig[2] = prefs.getBoolean("match", false);
    }
}
