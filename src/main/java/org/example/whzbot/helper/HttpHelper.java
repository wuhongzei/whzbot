package org.example.whzbot.helper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class HttpHelper {
    public static void testFunc(String url) {
        // uri [scheme:][//authority][path][?query][#fragment]
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println(response.statusCode());
            //System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int httpToFile(String url, String out_path) {
        // uri [scheme:][//authority][path][?query][#fragment]
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            HttpResponse<byte[]> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            int code = response.statusCode();

            if (code != 200)
                System.out.println(code);
            FileOutputStream writer = new FileOutputStream(out_path);
            writer.write(response.body());
            writer.close();

            return code;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static byte[] httpToFile(String url) {
        // uri [scheme:][//authority][path][?query][#fragment]
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            HttpResponse<byte[]> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            int code = response.statusCode();

            if (code != 200)
                System.out.println(code);

            return response.body();
        } catch (IOException | InterruptedException e) {
            return e.toString().getBytes();
        }
    }

    public static String ascii2d(String url) {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        String ascii2d = "https://ascii2d.net";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ascii2d))
                .build();
        String form = "";
        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() != 200) {
                System.out.println(response.statusCode());
                System.out.println(response.body());
            }
            String body = response.body();
            int beg = body.indexOf("<form");
            form = new String(
                    body.substring(beg, body.indexOf("</form", beg)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (form.isEmpty())
            return "web.error";
        form = form.replaceAll("'", "\"");

        HashMap<String, String> arg = new HashMap<>();
        int i = form.indexOf("<input");
        while (i > 0) {
            int j = form.indexOf("name", i);
            j = form.indexOf('=', j);
            j = form.indexOf('"', j);
            String arg_name = form.substring(
                    j + 1, form.indexOf('"', j + 1)
            );
            if (arg_name.equals("uri")) {
                arg.put(arg_name, url);
            } else {
                j = form.indexOf("value", i);
                j = form.indexOf('=', j);
                j = form.indexOf('"', j);
                arg.put(
                        arg_name,
                        form.substring(j + 1, form.indexOf('"', j + 1))
                );
            }
            i = form.indexOf("<input", i + 5);
        }

        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arg.entrySet())
            sj.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                    + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        //System.out.println(sj.toString());

        String search_result;

        request = HttpRequest.newBuilder()
                .uri(URI.create("https://ascii2d.net/search/uri"))
                .POST(HttpRequest.BodyPublishers.ofByteArray(
                        sj.toString().getBytes(StandardCharsets.UTF_8)))
                .build();
        search_result = getBody(client, request);

        if (search_result.isEmpty())
            return "web.error";
        i = search_result.indexOf("<div class='row item-box'>");
        int j = search_result.indexOf("\"/search/bovw/");
        String bvw_url = search_result.substring(
                j + 1, search_result.indexOf('"', j + 1)
        );
        i = search_result.indexOf("<div class='row item-box'>", i + 10);
        j = search_result.indexOf("<img", i);
        j = search_result.indexOf("src=\"", j);
        String pic1 = search_result.substring(
                j + 5, search_result.indexOf('"', j + 5)
        );
        j = search_result.indexOf("rel=\"noopener\"", i);
        j = search_result.indexOf("href=\"", j);
        String link1 = search_result.substring(
                j + 6, search_result.indexOf('"', j + 6)
        );

        search_result = getBody(client, ascii2d + bvw_url);
        if (search_result.isEmpty())
            return ascii2d + pic1 + "\n" + link1;

        i = search_result.indexOf("<div class='row item-box'>", i + 10);
        j = search_result.indexOf("<img", i);
        j = search_result.indexOf("src=\"", j);
        String pic2 = search_result.substring(
                j + 5, search_result.indexOf('"', j + 5)
        );
        j = search_result.indexOf("rel=\"noopener\"", i);
        j = search_result.indexOf("href=\"", j);
        String link2 = search_result.substring(
                j + 6, search_result.indexOf('"', j + 6)
        );
        i = search_result.indexOf("<div class='row item-box'>", i + 10);
        j = search_result.indexOf("<img", i);
        j = search_result.indexOf("src=\"", j);
        String pic3 = search_result.substring(
                j + 5, search_result.indexOf('"', j + 5)
        );
        j = search_result.indexOf("rel=\"noopener\"", i);
        j = search_result.indexOf("href=\"", j);
        String link3 = search_result.substring(
                j + 6, search_result.indexOf('"', j + 6)
        );

        return String.format("%s%s\n%s\n%s%s\n%s\n%s%s\n%s",
                ascii2d, pic1, link1,
                ascii2d, pic2, link2,
                ascii2d, pic3, link3);
    }

    private static String getBody(HttpClient client, HttpRequest request) {
        String search_result = "";
        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200)
                System.out.println(response.statusCode());
            else {
                search_result = response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return search_result;
    }

    private static String getBody(HttpClient client, String url) {
        String search_result;
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        search_result = getBody(client, request);
        return search_result;
    }

    public static boolean isUri(String url) {
        try {
            // To reduce risk, only http links are allowed.
            if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0)
                return false;
            URI uri = new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
