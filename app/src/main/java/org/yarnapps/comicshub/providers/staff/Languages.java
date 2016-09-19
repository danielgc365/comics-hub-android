package org.yarnapps.comicshub.providers.staff;

import java.util.Locale;

public class Languages {

    public static final int EN = 0;
    public static final int RU = 1;
    public static final int JP = 2;
    public static final int TR = 3;
    public static final int MULTI = 4;

    public static int fromLocale(Locale locale) {
        switch (locale.getLanguage()) {
            case "ru":
            case "uk":
            case "be":
            case "sk":
            case "sl":
            case "sr":
                return RU;
            case "tr":
                return TR;
            default:
                return EN;
        }
    }
}
