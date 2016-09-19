package org.yarnapps.comicshub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.yarnapps.comicshub.services.ScheduledService;
import org.yarnapps.comicshub.utils.FileLogger;

public class ScheduledServiceReceiver extends BroadcastReceiver {

    public static final long SCHEDULE_INTERVAL = AlarmManager.INTERVAL_HOUR / 30;

    public static void enable(Context context) {
        final Intent intent = new Intent(context, ScheduledService.class);
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SCHEDULE_INTERVAL, SCHEDULE_INTERVAL,
                PendingIntent.getService(context, 478, intent, 0));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                enable(context);
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                context.startService(new Intent(context, ScheduledService.class));
                break;
            default:
                FileLogger.getInstance().report("--ScheduledServiceReceiver unknown action");
        }
    }
}
