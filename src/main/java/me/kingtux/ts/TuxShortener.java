package me.kingtux.ts;

import me.kingtux.tmvc.core.Website;
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

    public TuxShortener(Website build) {
        setupDBConfig();
        setupDB();
        try {
            connection.createStatement().execute(SQL.TABLE.type(databaseConfig.getString("type")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        website = build;
        website.registerController(new MainController(this));
    }

    public boolean isHttps() {
        return website.isHttps();
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
        String url = "";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, key);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
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
        url = getExtension(extension);
        if (getRedirectLink(url) != null) {
            url = "-" + generateRandomString(2);
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, url);
            preparedStatement.setString(2, toURL);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public void delete(String extension) {
        String query = SQL.DELETE.type(databaseConfig.getString("type"));
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, extension);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
