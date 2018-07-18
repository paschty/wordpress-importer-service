package de.vzg.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String getFixedURL(String urlToInstance) {
        /*if(urlToInstance.startsWith("http:")){
            urlToInstance = urlToInstance.replace("http:", "https:");
        }*/
        if (!urlToInstance.endsWith("/")) {
            urlToInstance += "/";
        }
        return urlToInstance;
    }

    private static SimpleDateFormat WP_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

    public static Date getWPDate(String date) throws ParseException {
        return WP_DATE.parse(date);
    }
}
