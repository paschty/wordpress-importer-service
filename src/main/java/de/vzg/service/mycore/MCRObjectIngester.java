package de.vzg.service.mycore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.google.gson.Gson;

import de.vzg.service.Utils;

public class MCRObjectIngester {

    private static final String AUTH_API_PATH = "api/v1/auth/login";

    private static final String OBJECT_API_PATH = "api/v1/objects";

    public static AuthApiResponse login(String repo, String userName, String password)
        throws IOException, URISyntaxException {
        final String uriString = Utils.getFixedURL(repo) + AUTH_API_PATH;
        URI uri = new URI(uriString);

        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
        credentialsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), credentials);

        final HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider)
            .build();

        final HttpGet get = new HttpGet(uri);
        final HttpResponse execute = httpClient.execute(get);

        try (final InputStream is = execute.getEntity().getContent()) {
            try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                final Gson gson = new Gson();
                return gson.fromJson(isr, AuthApiResponse.class);
            }
        }
    }

    public static void ingestObject(String repo, AuthApiResponse auth, Document object) throws IOException {
        final String uriString = Utils.getFixedURL(repo) + OBJECT_API_PATH;
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPost post = new HttpPost(uriString);
        post.setHeader("Authorization", auth.asAuthString());
        final String entity = new XMLOutputter(Format.getPrettyFormat()).outputString(object);

        ;
        post.setEntity(MultipartEntityBuilder.create()
            .addBinaryBody("file", new ByteArrayInputStream(entity.getBytes(StandardCharsets.UTF_8))).build());

        final HttpResponse execute = httpClient.execute(post);

        if (execute.getStatusLine().getStatusCode() == 201) {
            return;
        }

        throw new IOException(
            "Error while ingesting MCRObject. " + execute.getStatusLine().getStatusCode() + " - " + execute
                .getStatusLine().getReasonPhrase());
    }

}
