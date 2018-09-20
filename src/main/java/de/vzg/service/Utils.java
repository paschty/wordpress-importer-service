package de.vzg.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String getFixedURL(String urlToInstance) {
        if(urlToInstance.startsWith("http:")){
            urlToInstance = urlToInstance.replace("http:", "https:");
        }
        if (!urlToInstance.endsWith("/")) {
            urlToInstance += "/";
        }
        return urlToInstance;
    }

    public static String encodeURI(String uriString) throws URISyntaxException, MalformedURLException {
        try {
            new URI(uriString);
            return uriString;
        } catch (URISyntaxException ex) {
            URL url = new URL(uriString);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                url.getQuery(), url.getRef());
            url = uri.toURL();
            return url.toString();
        }
    }

    private static SimpleDateFormat WP_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

    public static Date getWPDate(String date) throws ParseException {
        return WP_DATE.parse(date);
    }
}
