package com.zhuayinline.pets.crawler.util;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtil {

    public static String doGET(String urlAddress) {
        try {
            URL url = new URL(urlAddress);
            URLConnection connection = url.openConnection();

            InputStream inputStream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String doPOST(String urlAddress, String params) {
        try {
            URL url = new URL(urlAddress);
            URLConnection connection = url.openConnection();
            connection.addRequestProperty("encoding", "UTF-8");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(params);
            bufferedWriter.flush();

            InputStream inputStream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            bufferedWriter.close();
            outputStreamWriter.close();
            outputStream.close();
            return stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String url = "https://data.p4psearch.1688.com/data/ajax/get_premium_offer_list.json?beginpage=50&asyncreq=1&keywords=%E5%AE%A0%E7%89%A9%E7%94%A8%E5%93%81&sortType=&descendOrder=&province=&city=&priceStart=&priceEnd=&dis=&cosite=&location=&trackid=&spm=a2609.11209760.j3f8podl.e5rt432e&keywordid=&pageid=4c417a2dbhs01l&p4pid=772a98ba17a04b0abf82ce6c20bf6804&callback=jsonp_1611884266389_71964&_=1611884266389";
        String result = doGET(url);
        System.out.println("result:" + result);
        url = "https://suggest.1688.com/bin/suggest?type=offer&encode=utf8&q=%E5%AE%A0%E7%89%A9";
        String result2 = doGET(url);
        System.out.println("result2:" + result2);

        int start = result.indexOf("(") + 1;

        String substring = result.substring(start, result.length() - 1);
        System.out.println(substring);
        Object parse = JSONObject.parse(substring);
        System.out.println(parse);
    }
}
