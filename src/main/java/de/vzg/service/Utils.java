package de.vzg.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;

import de.vzg.service.wordpress.model.Post;

public class Utils {
    public static String getFixedURL(String urlToInstance) {
        if(urlToInstance.startsWith("http:")){
            urlToInstance = urlToInstance.replace("http:", "http:");
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

    public static String getTitleFileName(Post post) {
        return Jsoup.parseBodyFragment(post.getTitle().getRendered()).text()
            .replaceAll("[ ]", "_")
            .replaceAll("[^a-zA-Z0-9_]", "")+".pdf";
    }
}
