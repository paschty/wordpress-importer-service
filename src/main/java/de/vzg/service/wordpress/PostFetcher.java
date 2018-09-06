/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vzg.service.wordpress;

import de.vzg.service.Utils;
import de.vzg.service.wordpress.model.Post;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

public class PostFetcher {

    public static final String V2_POSTS_PAGE_PARAM = "page";

    private static final String V2_POSTS_PATH = "wp-json/wp/v2/posts/";

    public static final String V2_POSTS_PER_PAGE = "per_page";

    public static final String V2_POST_COUNT = "X-WP-TotalPages";

    private static final Logger LOGGER = LogManager.getLogger();

    public static int fetchCount(String instanceURL) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final String uri = Utils.getFixedURL(instanceURL) + V2_POSTS_PATH + "?" + V2_POSTS_PER_PAGE + "=100";
        LOGGER.debug("Fetching post count from {}", uri);
        final HttpGet get = new HttpGet(uri);
        final HttpResponse execute = httpClient.execute(get);
        return Integer.parseInt(execute.getFirstHeader(V2_POST_COUNT).getValue());
    }

    public static List<Post> fetch(String instanceURL) throws IOException {
        LOGGER.debug("Fetching all posts from {}", instanceURL);
        final int count = fetchCount(instanceURL);
        ArrayList<Post> allPosts = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            allPosts.addAll(fetch(instanceURL, i));
        }
        return allPosts;
    }

    public static Set<Post> fetchUntil(String instanceURL, Date until) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        int pageCount = 999;
        Date lastModified = null;
        Set<Post> postsUntil = new HashSet<Post>();
        for (int i = 1; i <= pageCount && (lastModified == null || lastModified.getTime() >= until.getTime()); i++) {
            final String uri = buildURLForPage(instanceURL, i);
            LOGGER.debug("Fetching : {}", uri);

            final HttpGet get = new HttpGet(uri);
            final HttpResponse execute = httpClient.execute(get);
            pageCount = Integer.parseInt(execute.getFirstHeader(V2_POST_COUNT).getValue());

            try (final InputStream is = execute.getEntity().getContent()) {
                try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    final List<Post> posts = Arrays.asList(new Gson().fromJson(isr, Post[].class));
                    for(Post modifiedPost:posts){
                        lastModified = Utils.getWPDate(modifiedPost.getModified());
                        if(lastModified.getTime()>=until.getTime()){
                         postsUntil.add(modifiedPost);
                        }
                    }
                } catch (ParseException e) {
                    throw new RuntimeException("Error while parsing WP Date!",e);
                }
            }
        }
        return postsUntil;
    }

    public static List<Post> fetch(String instanceURL, int page) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final String uri = buildURLForPage(instanceURL, page);
        LOGGER.debug("Fetching : {}", uri);
        final HttpGet get = new HttpGet(uri);
        final HttpResponse execute = httpClient.execute(get);

        try (final InputStream is = execute.getEntity().getContent()) {
            try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return Arrays.asList(new Gson().fromJson(isr, Post[].class));
            }
        }
    }

    private static String buildURLForPage(String instanceURL, int page) {
        return Utils.getFixedURL(instanceURL) + V2_POSTS_PATH + "?" + V2_POSTS_PAGE_PARAM + "=" + page + "&"
            + V2_POSTS_PER_PAGE + "=100" + "&orderby" + "=modified";
    }

    public static Post fetchPost(String instanceURL, int id) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final String uri = Utils.getFixedURL(instanceURL) + V2_POSTS_PATH + id;
        LOGGER.debug("Fetching : {}", uri);
        final HttpGet get = new HttpGet(uri);
        final HttpResponse execute = httpClient.execute(get);

        try (final InputStream is = execute.getEntity().getContent()) {
            try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(isr, Post.class);
            }
        }
    }

}
