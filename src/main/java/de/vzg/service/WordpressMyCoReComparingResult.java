package de.vzg.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WordpressMyCoReComparingResult {

    private List<PostInfo> notImportedPosts;

    private Map<PostInfo, String> postMyCoReIDMap;

    public WordpressMyCoReComparingResult() {
        this.notImportedPosts = new LinkedList<>();
        this.postMyCoReIDMap = new HashMap<>();
    }

    public List<PostInfo> getNotImportedPosts() {
        return notImportedPosts;
    }

    public Map<PostInfo, String> getPostMyCoReIDMap() {
        return postMyCoReIDMap;
    }
}
