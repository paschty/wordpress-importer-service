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

import de.vzg.service.wordpress.model.Post;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;

public class PostFetcher {

    private static final String V2_POSTS_PATH = "wp-json/wp/v2/posts/";

    public static List<Post> fetch(String instanceURL) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final String uri = getFixedURL(instanceURL) + V2_POSTS_PATH;
        final HttpGet get = new HttpGet(uri);
        final HttpResponse execute = httpClient.execute(get);

        try (final InputStream is = execute.getEntity().getContent()) {
            try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return Arrays.asList(new Gson().fromJson(isr, Post[].class));
            }
        }
    }

    public static Post fetch(String instanceURL, int id) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final String uri = getFixedURL(instanceURL) + V2_POSTS_PATH + id;
        final HttpGet get = new HttpGet(uri);
        final HttpResponse execute = httpClient.execute(get);

        try (final InputStream is = execute.getEntity().getContent()) {
            try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(isr, Post.class);
            }
        }
    }

    private static String getFixedURL(String urlToInstance) {
        if (!urlToInstance.endsWith("/")) {
            urlToInstance += "/";
        }
        return urlToInstance;
    }


}
