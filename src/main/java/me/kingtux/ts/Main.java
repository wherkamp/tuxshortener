package me.kingtux.ts;

import me.kingtux.tmvc.core.view.templategrabbers.IETemplateGrabber;
import me.kingtux.tuxmvc.simple.SimpleWebsiteBuilder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        File publc = new File("public");
        File tmpls = new File("templates");
        if (!tmpls.exists()) tmpls.mkdir();
        if (!publc.exists()) publc.mkdir();
        File configFile = new File("config.yml");
        if (!configFile.exists()) TuxShortener.saveResource("config.yml");
        YamlFile conf = new YamlFile(configFile);
        try {
            conf.load();
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        new TuxShortener(SimpleWebsiteBuilder.create().port(conf.getInt("port", 9089)).templateGrabber(new IETemplateGrabber(tmpls, "templates")).ssl(conf.getInt("ssl.port", 9090), getSslContextFactory(conf)).build());
    }

    private static SslContextFactory getSslContextFactory(YamlFile file) {
        if (!file.getBoolean("ssl.enabled")) return null;
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(file.getString("ssl.path"));
        sslContextFactory.setKeyStorePassword(file.getString("ssl.password"));
        return sslContextFactory;
    }
}
