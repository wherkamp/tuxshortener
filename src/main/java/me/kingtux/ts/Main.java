package me.kingtux.ts;

import io.javalin.Javalin;
import io.javalin.staticfiles.Location;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
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
        new TuxShortener(createJavlin(conf), conf.getBoolean("ssl.enabled"));
    }

    private static Javalin createJavlin(YamlFile conf) {
        if (conf.getBoolean("ssl.enabled")) {
            Javalin javalin = javalin = Javalin.create().enableStaticFiles("public", Location.EXTERNAL);
            javalin.server(() -> {
                Server server = new Server();
                ServerConnector sslConnector = new ServerConnector(server, getSslContextFactory(conf));
                sslConnector.setPort(conf.getInt("port"));
                ServerConnector connector = new ServerConnector(server);
                connector.setPort(conf.getInt("ssl.port"));
                server.setConnectors(new Connector[]{sslConnector, connector});
                return server;
            });
            return javalin;
        } else {
            return Javalin.create().port(conf.getInt("port")).enableStaticFiles("public", Location.EXTERNAL);
        }
    }

    private static SslContextFactory getSslContextFactory(YamlFile file) {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(file.getString("ssl.path"));
        sslContextFactory.setKeyStorePassword(file.getString("ssl.password"));
        return sslContextFactory;
    }


}
