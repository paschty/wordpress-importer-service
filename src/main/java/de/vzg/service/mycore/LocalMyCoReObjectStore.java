package de.vzg.service.mycore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.vzg.service.Utils;
import de.vzg.service.configuration.ImporterConfiguration;

public class LocalMyCoReObjectStore {

    public static final int FIVE_MINUTES = 1000 * 60 * 5;
    private static final Logger LOGGER = LogManager.getLogger();
    private static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    private String repoUrl;
    private Map<String, Document> idXMLMap;

    private Date lastCheckDate;

    private LocalMyCoReObjectStore() {
    }

    private LocalMyCoReObjectStore(String repoUrl) {
        this.repoUrl = repoUrl;

        final Path databasePath = getDatabasePath();

        if (Files.exists(databasePath)) {
            loadFromFile();
        } else {
            idXMLMap = new ConcurrentHashMap<>();
            lastCheckDate = new Date(0);
        }
    }

    private void loadFromFile() {
        Path databasePath = getDatabasePath();
        try (InputStream is = Files.newInputStream(databasePath)) {
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                final LocalMyCoReObjectStore savedStore = getGson().fromJson(isr, LocalMyCoReObjectStore.class);
                lastCheckDate = savedStore.lastCheckDate;
                repoUrl = savedStore.repoUrl;
                idXMLMap = savedStore.idXMLMap;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading " + databasePath.toString());
        }
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
            saveToFile();
        }
    }

    private void saveToFile() {
        final Gson gson = getGson();

        final Path dbPath = getDatabasePath();
        try (OutputStream os = Files.newOutputStream(dbPath, StandardOpenOption.CREATE, StandardOpenOption.SYNC)) {
            try (Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                gson.toJson(this, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while writing mycoredb", e);
        }
    }

    private Gson getGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Document.class, new DocumentTypeAdapter());
        return gsonBuilder.create();
    }

    private Path getDatabasePath() {
        return ImporterConfiguration.getConfigPath().resolve(getDatabaseName());
    }

    private String getDatabaseName() {
        return "mycoredb_" + getHost() + ".json";
    }

    private String getHost() {
        String host;
        try {
            host = new URL(this.repoUrl).getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return host;
    }

    private boolean isUpToDate() {
        return new Date().getTime() - lastCheckDate.getTime() < FIVE_MINUTES;
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
