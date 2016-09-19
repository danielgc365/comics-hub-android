package org.yarnapps.comicshub.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.activities.NewChaptersActivity;
import org.yarnapps.comicshub.helpers.NotificationHelper;
import org.yarnapps.comicshub.helpers.ScheduleHelper;
import org.yarnapps.comicshub.items.MangaUpdateInfo;
import org.yarnapps.comicshub.providers.AppUpdatesProvider;
import org.yarnapps.comicshub.providers.NewChaptersProvider;
import org.yarnapps.comicshub.utils.OneShotNotifier;

public class ScheduledService extends Service {

    private static final int INTERVAL_CHECK_APP_UPDATE = 12;

    private ScheduleHelper mScheduleHelper;
    private boolean mChaptersCheckEnabled;
    private int mChaptersCheckInterval;
    private boolean mChapterCheckWifiOnly;
    private boolean mAutoUpdate;

    public static boolean internetConnectionIsValid(Context context, boolean wifi_only) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() && (!wifi_only || ni.getType() == ConnectivityManager.TYPE_WIFI);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mScheduleHelper = new ScheduleHelper(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mChaptersCheckEnabled = prefs.getBoolean("chupd", false);
        mChaptersCheckInterval = Integer.parseInt(prefs.getString("chupd.interval", "12"));
        mChapterCheckWifiOnly = prefs.getBoolean("chupd.wifionly", false);
        mAutoUpdate = prefs.getBoolean("autoupdate", true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (internetConnectionIsValid(this, mChapterCheckWifiOnly)) {
            new BackgroundTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private class BackgroundTask extends AsyncTask<Void,AppUpdatesProvider.AppUpdateInfo,MangaUpdateInfo[]> {

        @Override
        protected MangaUpdateInfo[] doInBackground(Void... params) {
            try {
                int delay = mScheduleHelper.getActionIntervalHours(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
                if (mAutoUpdate && (delay < 0 || delay >= INTERVAL_CHECK_APP_UPDATE)) {
                    publishProgress(new AppUpdatesProvider().getLatestAny());
                    mScheduleHelper.actionDone(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                int delay = mScheduleHelper.getActionIntervalHours(ScheduleHelper.ACTION_CHECK_NEW_CHAPTERS);
                if (mChaptersCheckEnabled && (delay < 0 || delay >= mChaptersCheckInterval)) {
                    MangaUpdateInfo[] res = NewChaptersProvider.getInstance(ScheduledService.this).checkForNewChapters();
                    mScheduleHelper.actionDone(ScheduleHelper.ACTION_CHECK_NEW_CHAPTERS);
                    return res;
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MangaUpdateInfo[] mangaUpdates) {
            super.onPostExecute(mangaUpdates);
            if (mangaUpdates != null && mangaUpdates.length != 0) {
                int sum = 0;
                for (MangaUpdateInfo o : mangaUpdates) {
                    sum += (o.chapters - o.lastChapters);
                }

                Notification.Builder builder = new Notification.Builder(ScheduledService.this)
                        .setSmallIcon(R.drawable.ic_stat_star)
                        .setContentTitle(getString(R.string.new_chapters))
                        .setContentIntent(PendingIntent.getActivity(ScheduledService.this, 1,
                                new Intent(ScheduledService.this, NewChaptersActivity.class), 0))
                        .setTicker(getString(R.string.new_chapters))
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentText(String.format(getString(R.string.new_chapters_count), sum));
                Notification notification;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
                    for (MangaUpdateInfo o : mangaUpdates) {
                        inboxStyle.addLine((o.chapters - o.lastChapters) + " - " + o.mangaName);
                    }
                    inboxStyle.setSummaryText(String.format(getString(R.string.new_chapters_count), sum));
                    notification = inboxStyle.build();
                } else {
                    //noinspection deprecation
                    notification = builder.getNotification();
                }
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                new OneShotNotifier(ScheduledService.this).notify(678, notification);
            }
            stopSelf();
        }

        @Override
        protected void onProgressUpdate(AppUpdatesProvider.AppUpdateInfo... values) {
            super.onProgressUpdate(values);
            if (values[0] == null || !values[0].isActual()) {
                return;
            }
            new NotificationHelper(ScheduledService.this)
                    .title(R.string.app_update_avaliable)
                    .text(getString(R.string.app_name) + " " + values[0].getVersionName())
                    .icon(R.drawable.ic_stat_update)
                    .autoCancel()
                    .image(R.mipmap.ic_launcher)
                    .intentService(new Intent(ScheduledService.this, UpdateService.class).putExtra("url", values[0].getUrl()))
                    .notifyOnce(555, R.string.app_update_avaliable, values[0].getVersionCode());
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
