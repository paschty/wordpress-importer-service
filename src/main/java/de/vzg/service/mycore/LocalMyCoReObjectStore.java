package de.vzg.service.mycore;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.vzg.service.Utils;

public class LocalMyCoReObjectStore {

    public static final int A_HOUR_IN_MILLIS = 1000 * 60 * 60;

    private static final Logger LOGGER = LogManager.getLogger();

    private static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    private String repoUrl;

    private Map<String, Document> idXMLMap;

    private Date lastCheckDate = new Date(0);

    private LocalMyCoReObjectStore(String repoUrl) {
        this.repoUrl = repoUrl;
        idXMLMap = new ConcurrentHashMap<>();
    }

    public static LocalMyCoReObjectStore getInstance(String url) {
        return InstanceHolder.urlStoreInstanceHolder.computeIfAbsent(url, LocalMyCoReObjectStore::new);
    }

    public Document getObject(String id) {
        this.updateIfNeeded();
        return idXMLMap.computeIfAbsent(id, this::fetchObject);
    }

    private Document fetchObject(String id) {
        try {
            LOGGER.debug("Fetching {}", id);
            return ObjectFetcher.fetchObject(repoUrl, id);
        } catch (IOException | JDOMException e) {
            throw new RuntimeException("Could not fetch MODS!", e);
        }
    }

    public synchronized void updateIfNeeded() {
        if (!isUpToDate()) {
            LOGGER.debug("Update MyCoRe-Store!");
            final Document lastModifiedDocument = fetchObject("");
            final Date date = new Date();
            lastModifiedDocument.getRootElement().getChildren("mycoreobject").forEach(mycoreobjectElement -> {
                final String id = mycoreobjectElement.getAttributeValue("ID");
                if (idXMLMap.containsKey(id)) {
                    final Date lastModified;

                    try {
                        lastModified = SDF_UTC.parse(mycoreobjectElement.getAttributeValue("lastModified"));
                    } catch (ParseException e) {
                        throw new RuntimeException("Could not parse lastmodified of:" + id, e);
                    }

                    if (lastModified.getTime() > lastCheckDate.getTime()) {
                        fetchObject(id);
                    }
                }
            });
            lastCheckDate = date;
        }
    }

    private boolean isUpToDate() {
        return new Date().getTime() - lastCheckDate.getTime() < A_HOUR_IN_MILLIS;
    }

    private static class ObjectFetcher {

        private static final String V1_OBJECT_PATH = "api/v1/objects/";

        public static Document fetchObject(String repo, String mycoreID) throws IOException, JDOMException {
            final HttpClient httpClient = HttpClientBuilder.create().build();
            final String uri = Utils.getFixedURL(repo) + V1_OBJECT_PATH + mycoreID;
            final HttpGet get = new HttpGet(uri);
            final HttpResponse execute = httpClient.execute(get);

            try (final InputStream is = execute.getEntity().getContent()) {
                SAXBuilder saxBuilder = new SAXBuilder();
                return saxBuilder.build(is);
            }
        }
    }

    private static final class InstanceHolder {
        private static final ConcurrentHashMap<String, LocalMyCoReObjectStore> urlStoreInstanceHolder = new ConcurrentHashMap<>();
    }

}
