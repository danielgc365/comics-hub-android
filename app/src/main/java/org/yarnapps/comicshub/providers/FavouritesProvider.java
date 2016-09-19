package org.yarnapps.comicshub.providers;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.helpers.StorageHelper;
import org.yarnapps.comicshub.items.MangaInfo;
import org.yarnapps.comicshub.items.MangaPage;
import org.yarnapps.comicshub.items.MangaSummary;
import org.yarnapps.comicshub.lists.MangaList;
import org.yarnapps.comicshub.utils.AppHelper;
import org.yarnapps.comicshub.utils.FileLogger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.yarnapps.comicshub.items.MangaInfo.STATUS_UNKNOWN;

public class FavouritesProvider extends MangaProvider {

    private static final String TABLE_NAME = "favourites";
    private static final int sorts[] = {R.string.sort_latest, R.string.sort_alphabetical};
    private static final String sortUrls[] = {"timestamp DESC", "name COLLATE NOCASE"};
    private static WeakReference<FavouritesProvider> instanceReference = new WeakReference<>(null);
    private final StorageHelper mStorageHelper;
    private final Context mContext;

    public FavouritesProvider(Context context) {
        mContext = context;
        mStorageHelper = new StorageHelper(context);
    }

    public static FavouritesProvider getInstacne(Context context) {
        FavouritesProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new FavouritesProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        mStorageHelper.close();
        super.finalize();
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws IOException {
        if (page > 0)
            return null;
        final SQLiteDatabase database = mStorageHelper.getReadableDatabase();
        MangaList list = null;
        MangaInfo manga;
        Map<Integer,Integer> updates = NewChaptersProvider.getInstance(mContext)
                .getLastUpdates();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            list = new MangaList();
            Cursor cursor = database.query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider"},
                    genre == 0 ? null : "category=" + genre, null, null, null, sortUrls[sort]);
            if (cursor.moveToFirst()) {
                do {
                    manga = new MangaInfo();
                    manga.id = cursor.getInt(0);
                    manga.name = cursor.getString(1);
                    manga.subtitle = cursor.getString(2);
                    manga.genres = cursor.getString(3);
                    manga.preview = cursor.getString(4);
                    manga.path = cursor.getString(5);
                    try {
                        manga.provider = Class.forName(cursor.getString(6));
                    } catch (ClassNotFoundException e) {
                        manga.provider = LocalMangaProvider.class;
                    }
                    manga.status = STATUS_UNKNOWN;
                    manga.extra = updates.containsKey(manga.id) ?
                            "+" + updates.get(manga.id) : null;
                    list.add(manga);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            database.close();
        }
        return list;
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
        return mContext.getString(R.string.action_favourites);
    }

    @Deprecated
    public boolean add(MangaInfo mangaInfo) {
        return add(mangaInfo, 0);
    }

    public boolean add(MangaInfo mangaInfo, int category) {
        final ContentValues cv = new ContentValues();
        cv.put("id", mangaInfo.id);
        cv.put("name", mangaInfo.name);
        cv.put("subtitle", mangaInfo.subtitle);
        cv.put("summary", mangaInfo.genres);
        cv.put("preview", mangaInfo.preview);
        cv.put("provider", mangaInfo.provider.getName());
        cv.put("path", mangaInfo.path);
        cv.put("timestamp", new Date().getTime());
        cv.put("category", category);
        SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        boolean res = (database.insert(TABLE_NAME, null, cv) != -1);
        database.close();
        return res;
    }

    public boolean remove(MangaInfo mangaInfo) {
        final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        int c = database.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(mangaInfo.id)});
        database.close();
        return c > 0;
    }

    @Override
    public boolean remove(long[] ids) {
        final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        database.beginTransaction();
        for (long o : ids) {
            database.delete(TABLE_NAME, "id=" + o, null);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        return true;
    }


    public boolean has(MangaInfo mangaInfo) {
        final SQLiteDatabase database = mStorageHelper.getReadableDatabase();
        boolean res = StorageHelper.getColumnCount(database, TABLE_NAME, "id=" + mangaInfo.id) != 0;
        database.close();
        return res;
    }

    public int getCategory(MangaInfo mangaInfo) {
        int res = -1;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query(TABLE_NAME, new String[]{"category"}, "id=" + mangaInfo.id, null, null, null, null);
            if (cursor.moveToFirst()) {
                res = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
        return res;
    }

    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return (context.getString(R.string.genre_all) + "," +
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                        .getString("fav.categories", context.getString(R.string.favourites_categories_default))).split(",\\s*");
    }

    public void move(long[] ids, int category) {
        SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category", category);
        database.beginTransaction();
        for (long id : ids) {
            database.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(id)});
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
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
    public boolean isItemsRemovable() {
        return true;
    }

    @Override
    public boolean isMultiPage() {
        return false;
    }

    public static void dialog(final Context context, @Nullable DialogInterface.OnClickListener doneListener, final MangaInfo mangaInfo) {
        final int[] selected = new int[1];
        final DialogInterface.OnClickListener listener = doneListener;
        CharSequence[] categories = (context.getString(R.string.category_no) + "," +
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                        .getString("fav.categories", context.getString(R.string.favourites_categories_default)))
                .replaceAll(", ", ",").split(",");
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_favourite)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .setSingleChoiceItems(categories, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected[0] = which;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getInstacne(context).add(mangaInfo, selected[0]);
                        if (listener != null) {
                            listener.onClick(dialog, selected[0]);
                        }
                    }
                }).create().show();
    }
}
