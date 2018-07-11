package de.vzg.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.vzg.service.wordpress.model.Post;

public class WordpressMyCoReComparingResult {

    private Set<PostInfo> notImportedPosts;

    private Map<PostInfo, String> postMyCoReIDMap;

    public WordpressMyCoReComparingResult() {
        this.notImportedPosts = new HashSet<>();
        this.postMyCoReIDMap = new HashMap<>();
    }

    public Set<PostInfo> getNotImportedPosts() {
        return notImportedPosts;
    }

    public Map<PostInfo, String> getPostMyCoReIDMap() {
        return postMyCoReIDMap;
    }
}
