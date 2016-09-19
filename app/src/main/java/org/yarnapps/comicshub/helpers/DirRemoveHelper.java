package org.yarnapps.comicshub.helpers;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class DirRemoveHelper implements Runnable {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final File[] mFiles;

    public DirRemoveHelper(File file) {
        mFiles = new File[]{file};
    }

    public DirRemoveHelper(File files[]) {
        mFiles = files;
    }

    public DirRemoveHelper(File dir, String regexp) {
        final Pattern pattern = Pattern.compile(regexp);
        mFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return pattern.matcher(filename).matches();
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void removeDir(File dir) {
        if (dir == null || !dir.exists()) {
            Log.w("DIRRM", "not exists: " + (dir != null ? dir.getPath() : "null"));
            return;
        }
        if (dir.isDirectory()) {
            for (File o : dir.listFiles()) {
                if (o.isDirectory()) {
                    removeDir(o);
                } else {
                    o.delete();
                }
            }
        }
        Log.d("DIRRM", "removed: " + dir.getPath());
        dir.delete();
    }

    @Override
    public void run() {
        for (File file : mFiles) {
            removeDir(file);
        }
    }

    public void runAsync() {
        EXECUTOR.execute(this);
    }
}
