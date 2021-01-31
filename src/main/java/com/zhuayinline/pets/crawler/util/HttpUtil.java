package com.zhuayinline.pets.crawler.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;


import java.text.ParseException;

public class HttpUtil {

    public static String doGET(String url) throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        String result = "";
        try {
            // 连接超时
            httpclient.getParams().setParameter(
                    CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
            // 读取超时
            httpclient.getParams().setParameter(
                    CoreConnectionPNames.SO_TIMEOUT, 5000);

            HttpGet hg = new HttpGet(url);
            //模拟浏览器
            hg.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
            String charset = "UTF-8";
            hg.setURI(new java.net.URI(url));
            HttpResponse response = httpclient.execute(hg);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                charset = getContentCharSet(entity);
                // 使用EntityUtils的toString方法，传递编码，默认编码是ISO-8859-1
                result = EntityUtils.toString(entity, charset);
            }

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return result;
    }

    /**
     * 默认编码utf -8
     * Obtains character set of the entity, if known.
     *
     * @param entity must not be null
     * @return the character set, or null if not found
     * @throws ParseException           if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null
     */
    public static String getContentCharSet(final HttpEntity entity)
            throws ParseException {

        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }

        if (StringUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }
        return charset;
    }


    public static void main(String[] args) {
     /*   String url = "https://data.p4psearch.1688.com/data/ajax/get_premium_offer_list.json?beginpage=50&asyncreq=1&keywords=%E5%AE%A0%E7%89%A9%E7%94%A8%E5%93%81&sortType=&descendOrder=&province=&city=&priceStart=&priceEnd=&dis=&cosite=&location=&trackid=&spm=a2609.11209760.j3f8podl.e5rt432e&keywordid=&pageid=4c417a2dbhs01l&p4pid=772a98ba17a04b0abf82ce6c20bf6804&callback=jsonp_1611884266389_71964&_=1611884266389";
        String result = doGET(url);
        System.out.println("result:" + result);
        url = "https://suggest.1688.com/bin/suggest?type=offer&encode=utf8&q=%E5%AE%A0%E7%89%A9";
        String result2 = doGET(url);
        System.out.println("result2:" + result2);

        int start = result.indexOf("(") + 1;

        String substring = result.substring(start, result.length() - 1);
        System.out.println(substring);
        Object parse = JSONObject.parse(substring);
        System.out.println(parse);*/
    }
}
