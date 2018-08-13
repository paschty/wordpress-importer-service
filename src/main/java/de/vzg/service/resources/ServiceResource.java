package de.vzg.service.resources;

import de.vzg.service.Post2ModsConverter;
import de.vzg.service.WordpressMyCoReCompare;
import de.vzg.service.configuration.ImporterConfiguration;
import de.vzg.service.configuration.ImporterConfigurationPart;
import de.vzg.service.mycore.DerivateCreater;
import de.vzg.service.wordpress.LocalPostStore;
import de.vzg.service.wordpress.Post2PDFConverter;
import de.vzg.service.wordpress.PostFetcher;
import de.vzg.service.wordpress.model.Post;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jsoup.Jsoup;

import com.google.gson.Gson;

@Path("/")
public class ServiceResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("config")
    public String getConfigurations(){
        final Map<String, ImporterConfigurationPart> configParts = ImporterConfiguration.getConfiguration().getParts();
        return new Gson().toJson(configParts);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("compare/{config}")
    public String compare(@PathParam("config") String config) throws IOException, JDOMException {
        final Map<String, ImporterConfigurationPart> configParts = ImporterConfiguration.getConfiguration().getParts();
        if(!configParts.containsKey(config)){
            throw new NotFoundException("There is not configuration " + config);
        }

        final WordpressMyCoReCompare wordpressMyCoReCompare = new WordpressMyCoReCompare(configParts.get(config));
        return new Gson().toJson(wordpressMyCoReCompare.compare());
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("convert/mods/{config}/{id}")
    public String convertBlogPostXML(@PathParam("config") String configName, @PathParam("id") int postID) {
        final ImporterConfigurationPart config = ImporterConfiguration.getConfiguration().getParts()
            .get(configName);
        final LocalPostStore postStore = LocalPostStore.getInstance(config.getBlog());
        final Post post = postStore.getPost(postID);
        final Document mods = new Post2ModsConverter(post, config.getParentObject(), config.getBlog(),
            config.getPostTempate()).getMods();

        return new XMLOutputter().outputString(mods);
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("convert/pdf/{config}/{id}")
    public Response convertBlogPostPDF(@PathParam("config") String configName, @PathParam("id") int postID)
        throws IOException {
        final ImporterConfigurationPart config = ImporterConfiguration.getConfiguration().getParts()
            .get(configName);
        Post post = PostFetcher.fetchPost(config.getBlog(), postID);
        return Response.ok((StreamingOutput) outputStream -> {
            try {
                new Post2PDFConverter().getPDF(post, outputStream);
            } catch (FOPException | TransformerException | URISyntaxException e) {
                throw new RuntimeException("Error while generating PDF!", e);
            }
        }).header("Content-Disposition", "attachment; filename=\"" + getTitleFileName(post) + ".pdf\"").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("convert/derivate/{config}/{id}")
    public Response convertBlogPostDerivate(@PathParam("config") String configName, @PathParam("id") int postID,
        @QueryParam("parent") String parent)
        throws IOException {
        final ImporterConfigurationPart config = ImporterConfiguration.getConfiguration().getParts()
            .get(configName);
        Post post = PostFetcher.fetchPost(config.getBlog(), postID);

        String fileName = getTitleFileName(post);
        Document derivate = new DerivateCreater().createDerivate(config.getDerivateIDTemplate(), parent, fileName);
        return Response.ok((StreamingOutput) os->{
            new XMLOutputter(Format.getPrettyFormat()).output(derivate, os);
        }).build();
    }

    private String getTitleFileName(Post post) {
        return Jsoup.parseBodyFragment(post.getTitle().getRendered()).text()
            .replaceAll("[ ]", "_")
            .replaceAll("[^a-zA-Z0-9_]", "");
    }

}
