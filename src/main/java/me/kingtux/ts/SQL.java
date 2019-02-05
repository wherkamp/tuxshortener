package me.kingtux.ts;

public enum SQL {
    TABLE("", "CREATE TABLE IF NOT EXISTS `urls` (`id` INTEGER PRIMARY KEY AUTOINCREMENT,`key` TEXT UNIQUE, `url` TEXT );"),
    GET("", "SELECT * FROM urls WHERE key=?"),
    INSERT("", "INSERT INTO urls (key, url) VALUES (?,?);");

    private String mysql, sqlite;

    SQL(String mysql, String sqlite) {
        this.mysql = mysql;
        this.sqlite = sqlite;
    }

    public String type(String type) {
        if (type.equalsIgnoreCase("sqlite")) {
            return sqlite;
        }
        return mysql;
    }
}
