package org.yarnapps.comicshub.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import org.yarnapps.comicshub.BuildConfig;
import org.yarnapps.comicshub.R;
import org.yarnapps.comicshub.adapters.SearchHistoryAdapter;
import org.yarnapps.comicshub.dialogs.DirSelectDialog;
import org.yarnapps.comicshub.dialogs.LocalMoveDialog;
import org.yarnapps.comicshub.helpers.DirRemoveHelper;
import org.yarnapps.comicshub.helpers.ScheduleHelper;
import org.yarnapps.comicshub.providers.AppUpdatesProvider;
import org.yarnapps.comicshub.providers.LocalMangaProvider;
import org.yarnapps.comicshub.services.UpdateService;
import org.yarnapps.comicshub.utils.AppHelper;
import org.yarnapps.comicshub.utils.BackupRestoreUtil;
import org.yarnapps.comicshub.utils.FileLogger;
import org.yarnapps.comicshub.utils.MangaStore;

import java.io.File;

public class SettingsActivity extends BaseAppActivity implements Preference.OnPreferenceClickListener {

    public static final int SECTION_READER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();

        int section = getIntent().getIntExtra("section", 0);
        Fragment fragment;
        switch (section) {
            case SECTION_READER:
                fragment = new ReadSettingsFragment();
                break;
            default:
                fragment = new CommonSettingsFragment();
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!getFragmentManager().popBackStackImmediate()) {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        switch (preference.getKey()) {
            case "readeropt":
                getFragmentManager().beginTransaction()
                        .replace(R.id.content, new ReadSettingsFragment())
                        .addToBackStack("main")
                        .commit();
                return true;
            case "srcselect":
                startActivity(new Intent(this, ProviderSelectActivity.class));
                return true;
            case "bugreport":
                FileLogger.sendLog(this);
                return true;
            case "csearchhist":
                SearchHistoryAdapter.clearHistory(this);
                Toast.makeText(this, R.string.completed, Toast.LENGTH_SHORT).show();
                return true;
            case "about":
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case "backup":
                BackupRestoreUtil.showBackupDialog(this);
                return true;
            case "restore":
                BackupRestoreUtil.showRestoreDialog(this);
                return true;
            case "ccache":
                new CacheClearTask(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            case "movemanga":
                new LocalMoveDialog(this,
                        LocalMangaProvider.getInstacne(this).getAllIds())
                        .showSelectSource(null);
                return true;
            case "mangadir":
                if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    return true;
                }
                new DirSelectDialog(this)
                        .setDirSelectListener(new DirSelectDialog.OnDirSelectListener() {
                            @Override
                            public void onDirSelected(final File dir) {
                                if (!dir.canWrite()) {
                                    Toast.makeText(SettingsActivity.this, R.string.dir_no_access,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                preference.setSummary(dir.getPath());
                                preference.getEditor()
                                        .putString("mangadir", dir.getPath()).apply();
                                checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .show();
                return true;
            case "update":
                new CheckUpdatesTask(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BackupRestoreUtil.BACKUP_IMPORT_CODE:
                if (resultCode == RESULT_OK) {
                    File file = AppHelper.getFileFromUri(this, data.getData());
                    if (file != null) {
                        new BackupRestoreUtil(this).restore(file);
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }
    }

    public static class CommonSettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_common);
        }

        @Override
        public void onResume() {
            super.onResume();
            findPreference("chupd").setSummary(
                    getPreferenceManager().getSharedPreferences().getBoolean("chupd", false) ?
                            R.string.enabled : R.string.disabled
            );
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final Activity activity = getActivity();
            activity.setTitle(R.string.action_settings);
            findPreference("srcselect").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("readeropt").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("ccache").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("csearchhist").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("backup").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("restore").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("movemanga").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("bugreport").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

            Preference p = findPreference("update");
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            long lastCheck = new ScheduleHelper(activity).getActionRawTime(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
            p.setSummary(getString(R.string.last_update_check,
                    lastCheck == -1 ? getString(R.string.unknown) : AppHelper.getReadableDateTimeRelative(lastCheck)));

            p = findPreference("about");
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            p.setSummary(String.format(activity.getString(R.string.version),
                    BuildConfig.VERSION_NAME));

            bindPreferenceSummary((ListPreference) findPreference("defsection"));
            bindPreferenceSummary((ListPreference) findPreference("theme"));
            bindPreferenceSummary((EditTextPreference) findPreference("fav.categories"));

            p = findPreference("mangadir");
            try {
                p.setSummary(MangaStore.getMangasDir(activity).getPath());
            } catch (Exception e) {
                p.setSummary(R.string.unknown);
            }
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

            new AsyncTask<Void, Void, Float>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    findPreference("ccache").setSummary(R.string.size_calculating);
                }

                @Override
                protected Float doInBackground(Void... params) {
                    try {
                        return LocalMangaProvider.dirSize(getActivity().getExternalCacheDir()) / 1048576f;
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Float aFloat) {
                    super.onPostExecute(aFloat);
                    Preference preference = findPreference("ccache");
                    if (preference != null) {
                        preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size),
                                aFloat == null ? 0 : aFloat));
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static class ReadSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_read);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Activity activity = getActivity();
            activity.setTitle(R.string.action_reading_options);
            bindPreferenceSummary((ListPreference) findPreference("direction"));
            bindPreferenceSummary((ListPreference) findPreference("animation"));
            bindPreferenceSummary((ListPreference) findPreference("scalemode"));
            bindPreferenceSummary((ListPreference) findPreference("clickareas"));
        }
    }

    private static class CacheClearTask extends AsyncTask<Void, Void, Void> {
        private Preference preference;

        public CacheClearTask(Preference preference) {
            this.preference = preference;
        }

        @Override
        protected void onPreExecute() {
            preference.setSummary(R.string.cache_clearing);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File dir = preference.getContext().getExternalCacheDir();
            new DirRemoveHelper(dir).run();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size), 0f));
            super.onPostExecute(aVoid);
        }
    }

    public static void bindPreferenceSummary(ListPreference listPreference) {
        int index = listPreference.findIndexOfValue(listPreference.getValue());
        String summ = listPreference.getEntries()[index].toString();
        listPreference.setSummary(summ);
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue((String) newValue);
                String summ = ((ListPreference) preference).getEntries()[index].toString();
                preference.setSummary(summ);
                return true;
            }
        });
    }

    public static void bindPreferenceSummary(EditTextPreference editTextPreference) {
        String summ = editTextPreference.getText();
        editTextPreference.setSummary(summ);
        editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String)newValue);
                return true;
            }
        });
    }

    private class CheckUpdatesTask extends AsyncTask<Void,Void,AppUpdatesProvider> {
        private final Preference mPreference;
        private int mSelected = 0;

        public CheckUpdatesTask(Preference preference) {
            super();
            mPreference = preference;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreference.setSummary(R.string.wait);
        }

        @Override
        protected AppUpdatesProvider doInBackground(Void... params) {
            return new AppUpdatesProvider();
        }

        @Override
        protected void onPostExecute(AppUpdatesProvider appUpdatesProvider) {
            super.onPostExecute(appUpdatesProvider);
            if (appUpdatesProvider.isSuccess()) {
                long lastCheck = System.currentTimeMillis();
                new ScheduleHelper(SettingsActivity.this).actionDone(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
                mPreference.setSummary(getString(R.string.last_update_check,
                        lastCheck == -1 ? getString(R.string.unknown) : AppHelper.getReadableDateTimeRelative(lastCheck)));
                final AppUpdatesProvider.AppUpdateInfo[] updates = appUpdatesProvider.getLatestUpdates();
                if (updates.length == 0) {
                    Toast.makeText(SettingsActivity.this, R.string.no_app_updates, Toast.LENGTH_SHORT).show();
                    return;
                }
                final String[] titles = new String[updates.length];
                for (int i = 0; i < titles.length; i++) {
                    titles[i] = updates[i].getVersionName();
                }
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.update)
                        .setSingleChoiceItems(titles, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSelected = which;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UpdateService.start(getApplicationContext(), updates[mSelected].getUrl());
                            }
                        })
                        .setCancelable(true)
                        .create().show();
            } else {
                mPreference.setSummary(R.string.error);
            }
        }
    }
}
