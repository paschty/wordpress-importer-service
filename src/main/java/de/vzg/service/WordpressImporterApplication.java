package de.vzg.service;

import javax.ws.rs.ApplicationPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class WordpressImporterApplication extends ResourceConfig {

    private static final Logger LOGGER = LogManager.getLogger();

    public WordpressImporterApplication() {
        LOGGER.info("Initializing " + this.getClass().getName());
        packages("de.vzg.service.resources");
    }
}
