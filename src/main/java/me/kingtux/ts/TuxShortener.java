package me.kingtux.ts;

import me.kingtux.tmvc.core.Website;
import me.kingtux.tuxjsql.core.Builder;
import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.TuxJSQL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.RandomStringGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.Properties;

public class TuxShortener {
    private Website website;
    private Properties databaseConfig;

    public TuxShortener(Website build) {
        setupDBConfig();

        website = build;
        website.registerController(new MainController(this));
        TuxJSQL.setBuilder(TuxJSQL.Type.SQL);
        TuxJSQL.setConnection(databaseConfig);
        Builder builder = TuxJSQL.getBuilder();
        TuxJSQL.saveTable(TuxJSQL.getBuilder().createTable("urls", builder.createColumn("id", CommonDataTypes.INT, true), builder.createColumn("key", CommonDataTypes.TEXT, false, false, true), builder.createColumn("url", CommonDataTypes.TEXT)).createIfNotExists());
    }

    public boolean isHttps() {
        return website.isHttps();
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
        File file = new File("sql.properties");
        if (!file.exists()) saveResource("sql.properties");
        try {
            databaseConfig = new Properties();
            databaseConfig.load(new FileInputStream(file));
        } catch (IOException e) {
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
        String url = "";
        try (ResultSet resultSet = TuxJSQL.getTableByName("urls").select(key)) {
            while (resultSet.next()) {
                url = resultSet.getString("url");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public String addURL(String toURL, String extension) {
        String url = "";
        url = getExtension(extension);
        if (getRedirectLink(url) != null) {
            url = "-" + generateRandomString(2);
        }
        TuxJSQL.getTableByName("urls").insertAll(url, toURL);
        return url;
    }

    public void delete(String extension) {
        TuxJSQL.getTableByName("urls").delete(extension);
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
