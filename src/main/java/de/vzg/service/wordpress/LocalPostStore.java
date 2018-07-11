package de.vzg.service.wordpress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.vzg.service.wordpress.model.Post;

public class LocalPostStore {

    private Map<Integer, Post> idPostMap;

    private String instanceURL;

    private Date lastUpdate;

    private LocalPostStore(String instanceURL) {
        this.instanceURL = instanceURL;
        this.lastUpdate = new Date(0);
        this.idPostMap = new ConcurrentHashMap<>();
    }

    public static LocalPostStore getInstance(String instanceURL) {
        return InstanceHolder.blogStoreMap.computeIfAbsent(instanceURL, LocalPostStore::new);
    }

    public List<Post> getAllPosts() {
        this.update();
        return new ArrayList<>(this.idPostMap.values());
    }

    private void update() {
        try {
            final Date currentDate = new Date();
            final Set<Post> posts = PostFetcher.fetchUntil(instanceURL, lastUpdate);
            posts.forEach(p -> this.idPostMap.put(p.getId(), p));

            this.lastUpdate = currentDate;
        } catch (IOException e) {
            throw new RuntimeException("Error while gettings Posts from " + this.instanceURL, e);
        }
    }

    private static class InstanceHolder {
        private static Map<String, LocalPostStore> blogStoreMap = new ConcurrentHashMap<>();
    }

}
