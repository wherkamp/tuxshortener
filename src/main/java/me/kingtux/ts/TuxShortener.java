package me.kingtux.ts;

import io.javalin.Javalin;
import me.kingtux.tmvc.core.Website;
import me.kingtux.tmvc.core.view.templategrabbers.IETemplateGrabber;
import me.kingtux.tuxmvc.simple.SimpleWebsiteBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;

public class TuxShortener {
    private Website website;
    private Connection connection;
    private YamlFile databaseConfig;
    private File templatesFile = new File("templates");
    private boolean https;

    public boolean isHttps() {
        return https;
    }

    public TuxShortener(Javalin javalin, boolean https) {
        this.https = https;
        setupDBConfig();
        setupDB();
        try {
            connection.createStatement().execute(SQL.TABLE.type(databaseConfig.getString("type")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!templatesFile.exists()) templatesFile.mkdir();
        website = SimpleWebsiteBuilder.createSimpleBuilder(javalin).templateGrabber(new IETemplateGrabber(templatesFile, "templates")).build();
        website.registerController(new MainController(this));
    }

    private void setupDB() {
        if (databaseConfig.getString("type").equals("MYSQL")) {
            connection = mysqlConnection();
        } else {
            connection = SQLITEConnection();
        }

    }

    private Connection SQLITEConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + new File("db.sql").getPath());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private Connection mysqlConnection() {
        return null;
    }

    private void setupDBConfig() {
        File file = new File("db.yml");
        if (!file.exists()) saveResource("db.yml");
        try {
            databaseConfig = new YamlFile(file);
            databaseConfig.load();
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }

    }

    public static void saveResource(String s) {
        saveResource(s, new File(s));
    }

    public static void saveResource(String s, File file) {
        try {
            FileUtils.copyInputStreamToFile(TuxShortener.class.getResourceAsStream("/" + s), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRedirectLink(String key) {
        String query = SQL.GET.type(databaseConfig.getString("type"));
        System.out.println(query);
        String url = "";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, key);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println("Next");
                url = resultSet.getString("url");
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public String addURL(String toURL, String extension) {
        String query = SQL.INSERT.type(databaseConfig.getString("type"));
        String url = "";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            url = getExtension(extension);
            preparedStatement.setString(1, url);
            preparedStatement.setString(2, toURL);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private String getExtension(String extension) {
        if (extension.equals("")) {
            return generateRandomString(8);
        }
        return extension;
    }

    private String generateRandomString(int i) {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('a', 'z').build();
        return generator.generate(i);
    }

    public String encode(String host) {
        try {
            return URLEncoder.encode(host, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return host;
    }
}