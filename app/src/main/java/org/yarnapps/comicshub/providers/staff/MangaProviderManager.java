package org.yarnapps.comicshub.providers.staff;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import org.yarnapps.comicshub.providers.ComiCastleProvider;
import org.yarnapps.comicshub.providers.FavouritesProvider;
import org.yarnapps.comicshub.providers.GogoComicProvider;
import org.yarnapps.comicshub.providers.HistoryProvider;
import org.yarnapps.comicshub.providers.LocalMangaProvider;
import org.yarnapps.comicshub.providers.MangaProvider;
import org.yarnapps.comicshub.providers.ReadComics;
import org.yarnapps.comicshub.providers.RecommendationsProvider;
import org.yarnapps.comicshub.utils.FileLogger;

import java.util.ArrayList;

public class MangaProviderManager {

    public static final int PROVIDER_LOCAL = -4;
    public static final int PROVIDER_RECOMMENDATIONS = -3;
    public static final int PROVIDER_FAVOURITES = -2;
    public static final int PROVIDER_HISTORY = -1;

    //here is where you initiate more providers
    public static final ProviderSummary[] providers = {
            new ProviderSummary("ReadComics", ReadComics.class, Languages.EN),
            new ProviderSummary("Comicastle", ComiCastleProvider.class, Languages.EN),
            new ProviderSummary("Gogo Comics", GogoComicProvider.class, Languages.EN)
    };

    private final Context mContext;
    private final ArrayList<ProviderSummary> mEnabledProviders;

    public MangaProviderManager(Context context) {
        mContext = context;
        mEnabledProviders = new ArrayList<>();
        update();
    }

    public void update() {
        mEnabledProviders.clear();
        SharedPreferences prefs = mContext.getSharedPreferences("providers", Context.MODE_PRIVATE);
        for (ProviderSummary o : providers) {
            if (prefs.getBoolean(o.name, true)) {
                mEnabledProviders.add(o);
            }
        }
    }

    public MangaProvider getProvider(int index) {
        try {
            switch (index) {
                case PROVIDER_LOCAL:
                    return LocalMangaProvider.getInstacne(mContext);
                case PROVIDER_FAVOURITES:
                    return FavouritesProvider.getInstacne(mContext);
                case PROVIDER_HISTORY:
                    return HistoryProvider.getInstacne(mContext);
                case PROVIDER_RECOMMENDATIONS:
                    return RecommendationsProvider.getInstacne(mContext);
                default:
                    return mEnabledProviders.get(index).aClass.newInstance();
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    public String[] getNames() {
        String[] res = new String[mEnabledProviders.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = mEnabledProviders.get(i).name;
        }
        return res;
    }

    @Nullable
    public static MangaProvider createProvider(String className) {
        try {
            Class aClass = Class.forName(className);
            return (MangaProvider) aClass.newInstance();
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    public static void configure(Context context, int language) {
        SharedPreferences.Editor editor = context.getSharedPreferences("providers", Context.MODE_PRIVATE)
                .edit();
        for (ProviderSummary o : providers) {
            editor.putBoolean(o.name, o.lang == language || o.lang == Languages.MULTI);
        }
        editor.apply();
    }

    public void setProviderEnabled(String name, boolean enabled) {
        mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).edit().putBoolean(name, enabled).apply();
    }

    public boolean isProviderEnabled(String name) {
        return mContext.getSharedPreferences("providers", Context.MODE_PRIVATE).getBoolean(name, true);
    }

    public int getProviderIndex(ProviderSummary providerSumm) {
        for (int i = 0; i < mEnabledProviders.size(); i++) {
            if (mEnabledProviders.get(i).equals(providerSumm)) {
                return i;
            }
        }
        if (LocalMangaProvider.class.equals(providerSumm.aClass)) {
            return PROVIDER_LOCAL;
        } else if (RecommendationsProvider.class.equals(providerSumm.aClass)) {
            return PROVIDER_RECOMMENDATIONS;
        } else if (FavouritesProvider.class.equals(providerSumm.aClass)) {
            return PROVIDER_FAVOURITES;
        } else if (HistoryProvider.class.equals(providerSumm.aClass)) {
            return PROVIDER_HISTORY;
        }
        return -1;
    }

    public ArrayList<ProviderSummary> getEnabledProviders() {
        return mEnabledProviders;
    }

    public static boolean needConnection(MangaProvider provider) {
        return !(provider instanceof LocalMangaProvider || provider instanceof FavouritesProvider || provider instanceof HistoryProvider);
    }

    public static boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isAvailable() && ni.isConnected();
    }

    public MangaProvider instanceProvider(Class<? extends MangaProvider> aClass) {
        return instanceProvider(mContext, aClass);
    }

    @Nullable
    public static MangaProvider instanceProvider(Context context, Class<? extends MangaProvider> aClass) {
        if (aClass.equals(LocalMangaProvider.class)) {
            return LocalMangaProvider.getInstacne(context);
        } else if (aClass.equals(RecommendationsProvider.class)) {
            return RecommendationsProvider.getInstacne(context);
        } else if (aClass.equals(FavouritesProvider.class)) {
            return FavouritesProvider.getInstacne(context);
        } else if (aClass.equals(HistoryProvider.class)) {
            return HistoryProvider.getInstacne(context);
        } else {
            try {
                return aClass.newInstance();
            } catch (Exception e) {
                FileLogger.getInstance().report(e);
            }
        }
        return null;
    }

    public static void setSort(Context context, MangaProvider provider, int sort) {
        context.getSharedPreferences("sort", Context.MODE_PRIVATE)
                .edit()
                .putInt(provider.getName(), sort)
                .apply();
    }

    public static int getSort(Context context, MangaProvider provider) {
        return context.getSharedPreferences("sort", Context.MODE_PRIVATE).getInt(provider.getName(), 0);
    }
}
