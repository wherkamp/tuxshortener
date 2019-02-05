package me.kingtux.ts;

import me.kingtux.tmvc.core.annotations.Path;
import me.kingtux.tmvc.core.annotations.RequestParam;
import me.kingtux.tmvc.core.request.HTTPCode;
import me.kingtux.tmvc.core.request.Request;
import me.kingtux.tmvc.core.request.RequestType;
import me.kingtux.tmvc.core.view.View;

public class MainController {
    private TuxShortener tuxShortener;

    public MainController(TuxShortener tuxShortener) {
        this.tuxShortener = tuxShortener;
    }

    @Path(path = "/")
    public void index(View view) {
        System.out.println("Connected to base!");
        view.setTemplate("index");
    }

    @Path(path = "/submit", requestType = RequestType.POST)
    public void submit(@RequestParam(key = "url") String toURL, @RequestParam(key = "extension") String extension, Request request) {
        String s = tuxShortener.addURL(toURL, extension);
        String url = tuxShortener.isHttps() ? "https" : "http" + "://" + request.header("Host") + "/submit?url=" + tuxShortener.encode(tuxShortener.isHttps() ? "https" : "http" + "://" + request.header("Host") + "/") + s;
        request.redirect(url, HTTPCode.TEMP_REDIRECT);
    }


    @Path(path = "/submit")
    public void submit(@RequestParam(key = "url", type = RequestParam.Type.GET) String url, View view) {
        System.out.println("URL: " + url);
        view.setTemplate("submit").set("url", url);

    }

    @Path(path = "/:key")
    public void redirect(@RequestParam(key = "key", type = RequestParam.Type.URL) String key, Request request) {
        String keyt = key.replaceAll("/", "");
        if (keyt.equals("")) {
            System.out.println(proto() + request.header("Host"));
            //request.redirect(proto() + request.header("Host"), HTTPCode.TEMP_REDIRECT);
            return;
        }
        String url = tuxShortener.getRedirectLink(keyt);
        if (keyt.equals("")) {
            //request.redirect(proto() + request.header("Host"));
            return;
        }
        request.redirect(url, HTTPCode.REDIRECT);
    }

    private String proto() {
        return tuxShortener.isHttps() ? "https" : "http" + "://";
    }
}
