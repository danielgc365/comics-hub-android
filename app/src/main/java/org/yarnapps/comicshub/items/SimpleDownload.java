package org.yarnapps.comicshub.items;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleDownload implements Runnable {

    private final String mSourceUrl;
    private final File mDestination;

    public SimpleDownload(String sourceUrl, File destination) {
        this.mSourceUrl = sourceUrl;
        this.mDestination = destination;
    }

    @Override
    public void run() {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(mSourceUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return;
            }
            input = connection.getInputStream();
            output = new FileOutputStream(mDestination);
            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
                //if (Thread.interrupted())
            }
        } catch (Exception e) {
            if (mDestination.exists()) {
                mDestination.delete();
            }
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
            if (connection != null)
                connection.disconnect();
        }
    }

    public boolean isSuccess() {
        return mDestination.exists();
    }

    public File getDestination() {
        return mDestination;
    }

    public String getSourceUrl() {
        return mSourceUrl;
    }
}
