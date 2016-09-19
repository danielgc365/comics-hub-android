package org.yarnapps.comicshub.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.helpers.DirRemoveHelper;
import org.yarnapps.comicshub.helpers.NotificationHelper;
import org.yarnapps.comicshub.providers.LocalMangaProvider;
import org.yarnapps.comicshub.utils.ChangesObserver;
import org.yarnapps.comicshub.utils.FileLogger;
import org.yarnapps.comicshub.utils.MangaStore;
import org.yarnapps.comicshub.utils.ZipBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.yarnapps.comicshub.utils.MangaStore.TABLE_CHAPTERS;
import static org.yarnapps.comicshub.utils.MangaStore.TABLE_MANGAS;
import static org.yarnapps.comicshub.utils.MangaStore.TABLE_PAGES;

public class ImportService extends Service {

    public static final int ACTION_START = 60;
    public static final int ACTION_CANCEL = 61;
    private static final int NOTIFY_ID = 632;

    private NotificationHelper mNotificationHelper;
    private PowerManager.WakeLock mWakeLock;
    private WeakReference<ImportTask> mTaskReference = new WeakReference<>(null);

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationHelper = new NotificationHelper(this)
                .text(R.string.preparing)
                .indeterminate()
                .actionCancel(PendingIntent.getService(
                        this, 0,
                        new Intent(this, ImportService.class).putExtra("action", ACTION_CANCEL),
                        0));
        startForeground(NOTIFY_ID, mNotificationHelper.notification());
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Saving manga");
        mWakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        mWakeLock.release();
        stopForeground(false);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent != null ? intent.getIntExtra("action", 0) : 0;
        ImportTask task = mTaskReference.get();
        switch (action) {
            case ACTION_START:
                if (task != null) {
                    Toast.makeText(this, R.string.wait_for_complete, Toast.LENGTH_SHORT).show();
                    break;
                }
                String path = intent.getStringExtra(Intent.EXTRA_TEXT);
                task = new ImportTask(new File(path).getName());
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
                mTaskReference = new WeakReference<>(task);
                break;
            case ACTION_CANCEL:
                if (task != null) {
                    mNotificationHelper
                            .noActions()
                            .indeterminate()
                            .text(R.string.cancelling)
                            .update(NOTIFY_ID);
                    task.cancel(false);
                } else {
                    stopSelf();
                }
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ImportTask extends AsyncTask<String,Integer,Integer> {
        private final String mName;

        public ImportTask(String name) {
            this.mName = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mNotificationHelper
                    .indeterminate()
                    .icon(android.R.drawable.stat_sys_download)
                    .title(getString(R.string.import_file) + " " + mName)
                    .text(R.string.preparing)
                    .update(NOTIFY_ID, R.string.import_file);
        }

        @Override
        //todo breaks into two methods one for zip(.cbz) one for rar(.cbr)
        protected Integer doInBackground(String... params) {
            SQLiteDatabase database = null;
            ZipInputStream zipInputStream = null;
            int pages = 0;
            try {
                //reading info
                final ZipEntry[] entries = ZipBuilder.enumerateEntries(params[0]);
                String name = entries[0].getName();
                name = name.substring(0, name.length() - 1);
                int total = 0;
                for (ZipEntry o:entries) {
                    if (!o.isDirectory()) {
                        total++;
                    }
                }
                int mangaId;
                int chapterId;
                int pageId;
                File preview = null;
                zipInputStream = new ZipInputStream(new FileInputStream(params[0]));
                final File dest = new File(MangaStore.getMangasDir(ImportService.this),
                        String.valueOf(mangaId = params[0].hashCode()));
                if (dest.exists()) {
                    new DirRemoveHelper(dest).run();
                }
                dest.mkdirs();
                final byte[] buffer = new byte[1024];
                publishProgress(0, entries.length);
                //importing
                database = new MangaStore(ImportService.this).getDatabase(true);
                ContentValues cv;
                //all pages
                chapterId = 0;
                File outFile;
                ZipEntry entry;
                FileOutputStream outputStream;
                while ((entry = zipInputStream.getNextEntry()) != null && !isCancelled()) {
                    if (!entry.isDirectory()) {
                        pageId = entry.getName().hashCode();
                        outFile = new File(dest, chapterId + "_" + pageId);
                        if (outFile.exists() || outFile.createNewFile()) {
                            outputStream = new FileOutputStream(outFile);
                            int len;
                            while ((len = zipInputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, len);
                            }
                            outputStream.close();
                            cv = new ContentValues();
                            cv.put("id", pageId);
                            cv.put("chapterid", chapterId);
                            cv.put("mangaid", mangaId);
                            cv.put("file", outFile.getName());
                            cv.put("number", pages);
                            database.insert(TABLE_PAGES, null, cv);
                            pages++;
                            publishProgress(pages, total);
                            if (preview == null) {
                                preview = new File(dest, "cover");
                                LocalMangaProvider.copyFile(outFile, preview);
                            }
                        }
                    }
                }
                if (isCancelled()) {
                    //remove all
                    database.delete(TABLE_PAGES, "mangaid=?", new String[]{String.valueOf(mangaId)});
                    new DirRemoveHelper(dest).run();
                    database.close();
                    try {
                        zipInputStream.close();
                    } catch (IOException ignored) {
                    }
                    return null;
                }
                //save chapter
                cv = new ContentValues();
                cv.put("id", chapterId);
                cv.put("mangaid", mangaId);
                cv.put("name", "default");
                cv.put("number", 0);
                database.insert(TABLE_CHAPTERS, null, cv);
                //save manga
                cv = new ContentValues();
                cv.put("id", mangaId);
                cv.put("name", name);
                cv.put("subtitle", "");
                cv.put("summary", "");
                cv.put("description", getString(R.string.imported_from, new File(params[0]).getName()));
                cv.put("dir", MangaStore.getMangaDir(ImportService.this, database, mangaId).getPath());
                cv.put("timestamp", new Date().getTime());
                database.insert(TABLE_MANGAS, null, cv);
            } catch (Exception e) {
                FileLogger.getInstance().report(e);
                pages = -1;
            } finally {
                if (database != null) {
                    database.close();
                }
                if (zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return pages;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mNotificationHelper
                    .progress(values[0], values[1])
                    .text(getString(R.string.import_progress, values[0], values[1]))
                    .update(NOTIFY_ID);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            stopSelf();
            mNotificationHelper
                    .noActions()
                    .noProgress()
                    .autoCancel()
                    .icon(integer == -1 ? android.R.drawable.stat_notify_error : android.R.drawable.stat_sys_download_done)
                    .text(integer == -1 ? R.string.error : R.string.import_complete)
                    .update(NOTIFY_ID);
            mTaskReference = new WeakReference<>(null);
            ChangesObserver.getInstance().emitOnLocalChanged(-1, null);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            stopSelf();
            mTaskReference = new WeakReference<>(null);
            mNotificationHelper.dismiss(NOTIFY_ID);
        }
    }

}
