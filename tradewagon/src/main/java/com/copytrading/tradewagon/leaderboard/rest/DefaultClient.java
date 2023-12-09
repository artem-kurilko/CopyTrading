package com.copytrading.tradewagon.leaderboard.rest;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DefaultClient {
    private final HttpClient client;

    public DefaultClient() {
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(requestConfig).build();
    }

    @SuppressWarnings("unchecked")
    public Response doPost(String url, Object params, Map<String, String> headers) throws Exception {
        HttpPost request = new HttpPost(url);
        if (headers != null)
            for (String key : headers.keySet()) {
                request.setHeader(key, headers.get(key));
            }
        if (params != null) {
            if (params instanceof String)
                request.setEntity(new StringEntity(params.toString()));
            else
                request.setEntity(new UrlEncodedFormEntity((List<NameValuePair>) params, "UTF-8"));
        }

        HttpClientContext context = HttpClientContext.create();
        HttpResponse response = client.execute(request, context);
        String content = "";
        if (response.getEntity() != null)
            content = EntityUtils.toString(response.getEntity());

        Map<String, String> asList = new TreeMap<String, String>();
        for (Header header : response.getAllHeaders()) {
            asList.put(header.getName(), header.getValue());
        }
        request.releaseConnection();
        return new Response(content, asList);
    }

    public Response doGet(String url, Map<String, String> headers) throws Exception {
        HttpGet request = new HttpGet(url);
        if (headers != null)
            for (String key : headers.keySet()) {
                request.setHeader(key, headers.get(key));
            }
        HttpClientContext context = HttpClientContext.create();

        HttpResponse response = client.execute(request, context);
        String content = EntityUtils.toString(response.getEntity());
        request.releaseConnection();
        Map<String, String> asList = new TreeMap<String, String>();
        for (Header header : response.getAllHeaders()) {
            asList.put(header.getName(), header.getValue());
        }
        Response res = new Response(content, asList);
        request.releaseConnection();
        return res;
    }

}
