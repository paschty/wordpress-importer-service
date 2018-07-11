package de.vzg.service.resources;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jdom2.JDOMException;

import com.google.gson.Gson;

import de.vzg.service.WordpressMyCoReCompare;
import de.vzg.service.configuration.ImporterConfiguration;
import de.vzg.service.configuration.ImporterConfigurationPart;

@Path("/")
public class CompareResource {

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
}
