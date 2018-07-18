package de.vzg.service.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.jdom2.Document;
import org.jdom2.JDOMException;

import com.google.gson.Gson;

import de.vzg.service.Post2ModsConverter;
import de.vzg.service.WordpressMyCoReCompare;
import de.vzg.service.configuration.ImporterConfiguration;
import de.vzg.service.configuration.ImporterConfigurationPart;
import de.vzg.service.mycore.AuthApiResponse;
import de.vzg.service.mycore.MCRObjectIngester;
import de.vzg.service.wordpress.LocalPostStore;
import de.vzg.service.wordpress.model.Post;

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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("import/{config}/{id}")
    public String importBlogPost(@PathParam("config") String configName, @PathParam("id") int postID,
        @QueryParam("user") String user, @QueryParam("password") String password) {

        final ImporterConfigurationPart config = ImporterConfiguration.getConfiguration().getParts()
            .get(configName);
        final LocalPostStore postStore = LocalPostStore.getInstance(config.getBlog());
        final Post post = postStore.getPost(postID);
        final Document mods = new Post2ModsConverter(post, config.getParentObject(), config.getBlog()).getMods();

        try {
            final AuthApiResponse authToken = MCRObjectIngester.login(config.getRepository(), user, password);
            MCRObjectIngester.ingestObject(config.getRepository(), authToken, mods);
        } catch (IOException | URISyntaxException e) {
            throw new WebApplicationException("Error while ingesting!", e);
        }

        return "[\"" + config.getRepository()+"receive/"+"" +"\"]";
    }


}
