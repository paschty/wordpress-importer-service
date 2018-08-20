package de.vzg.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WordpressMyCoReComparingResult {

    private List<PostInfo> notImportedPosts;

    private Map<String,PostInfo> mycoreIDPostMap;

    public WordpressMyCoReComparingResult() {
        this.notImportedPosts = new LinkedList<>();
        this.mycoreIDPostMap = new HashMap<>();
    }

    public List<PostInfo> getNotImportedPosts() {
        return notImportedPosts;
    }

    public Map<String,PostInfo> getMyCoReIDPostMap() {
        return mycoreIDPostMap;
    }
}
