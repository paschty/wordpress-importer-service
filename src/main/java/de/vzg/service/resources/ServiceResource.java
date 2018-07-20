package de.vzg.service.resources;

import de.vzg.service.Post2ModsConverter;
import de.vzg.service.WordpressMyCoReCompare;
import de.vzg.service.configuration.ImporterConfiguration;
import de.vzg.service.configuration.ImporterConfigurationPart;
import de.vzg.service.wordpress.LocalPostStore;
import de.vzg.service.wordpress.model.Post;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;

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
    @Path("convert/{config}/{id}")
    public String convertBlogPost(@PathParam("config") String configName, @PathParam("id") int postID) {
        final ImporterConfigurationPart config = ImporterConfiguration.getConfiguration().getParts()
            .get(configName);
        final LocalPostStore postStore = LocalPostStore.getInstance(config.getBlog());
        final Post post = postStore.getPost(postID);
        final Document mods = new Post2ModsConverter(post, config.getParentObject(), config.getBlog(),
            config.getPostTempate()).getMods();

        return new XMLOutputter().outputString(mods);
    }


}
