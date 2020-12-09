package com.zhuayinline.pets.crawler.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class SearchUtil {

    public Document getDocument(String url) throws Exception {
        Document document = Jsoup
                .connect(url)
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36")
                .get();
        return document;
    }
}