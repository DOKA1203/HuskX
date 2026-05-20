package kr.doka.lab.huskx;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HuskXLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        Logger logger = LoggerFactory.getLogger("HuskX");
        logger.info("Checking for updates...");


        Properties props = new Properties();
        try (InputStream in = HuskXLoader.class
                .getClassLoader()
                .getResourceAsStream("huskx.properties")) {
            props.load(in);
        } catch (IOException _) {

        }

        String repo = props.getProperty("github.repo");

    }
}
