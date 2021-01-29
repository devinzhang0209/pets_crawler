package com.zhuayinline.pets.crawler.service.impl;

import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.IPetsCall;
import com.zhuayinline.pets.crawler.util.SearchUtil;
import com.zhuayinline.pets.crawler.util.StringUtil;
import com.zhuayinline.pets.crawler.vo.Category;
import com.zhuayinline.pets.crawler.vo.Website;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EPetsService extends IPetsCall {
    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();

        String categoryClassName = "dogType";
        String dogCategory1 = "狗狗";
        String catCategory1 = "猫猫";

        Document dogDocument = searchUtil.getDocument(Website.ECDOG.getBaseCategoryUrl());
        Elements dogElement = dogDocument.getElementsByClass(categoryClassName);
        Elements dogCategorySelect = dogElement.select("ul").select("li").select("a");

        buildCategory(dogCategory1, dogCategorySelect, categories);

        Document catDocument = searchUtil.getDocument(Website.ECCAT.getBaseCategoryUrl());
        Elements catElement = catDocument.getElementsByClass(categoryClassName);
        Elements catCategorySelect = catElement.select("ul").select("li").select("a");
        buildCategory(catCategory1, catCategorySelect, categories);


        return categories;
    }

    private void buildCategory(String category1, Elements elements, List<Category> categories) {
        System.out.println(String.format("begin to get category for %s", category1));
        for (int i = 0; i < elements.size(); i++) {
            String categoryLink = elements.get(i).attr("href");
            String categoryName = elements.get(i).text();
            System.out.println(String.format("EC: category:%s , categoryLink:%s", categoryName, categoryLink));

            Category category = buildCategory(category1, categoryName, StringUtil.EMPTY, categoryLink);

            categories.add(category);
        }
    }

    @Override
    public Object[] getCategoryProductCount(String categoryProductUrl) throws Exception {
        Object[] objs = new Object[2];
        Integer page = 0;
        Document document = searchUtil.getDocument(categoryProductUrl);
        String text = document.getElementsByClass("last").text();
        try {
            if (StringUtil.isNotEmpty(text)) {
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String totalPage = matcher.group();
                    page = Integer.parseInt(totalPage);
                }
            } else {
                Elements totoalCount = document.getElementsByClass("mr fr");
                if (totoalCount != null && totoalCount.prev() != null) {
                    Elements prev = totoalCount.prev();
                    if (prev.size() > 0) {
                        Element pageFont = document.getElementsByClass("mr fr").prev().get(0);
                        if (null != pageFont && pageFont.hasClass("fr")) {
                            String pageText = pageFont.text();
                            if (null != pageText) {
                                page = Integer.parseInt(pageText.replaceAll("1/", ""));
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.format("category:%s,pageCount :%s", categoryProductUrl, page));
        objs[0] = page;
        return objs;
    }

    @Override
    public List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception {
        List<PetsProduct> products = new LinkedList<>();

        Document document = searchUtil.getDocument(categoryProductUrl);
        Elements productList = document.getElementsByClass("list_box-li");
        for (int i = 0; i < productList.size(); i++) {
            String productLink = productList.get(i).getElementsByClass("gd-photo db rela").attr("href");
            if (StringUtil.isNotEmpty(productLink)) {
                Document productDocument = searchUtil.getDocument(productLink);
                if (productDocument == null) {
                    continue;
                }
                String productName = StringUtil.EMPTY;
                if (productDocument.select("meta[property='og:title']").size() > 0) {
                    productName = productDocument.select("meta[property='og:title']").get(0).attr("content");
                }
                if (StringUtil.isEmpty(productName)) {
                    if (productDocument.getElementsByClass("gdtitle").size() > 0) {
                        productName = productDocument.getElementsByClass("gdtitle").get(0).text();
                    }
                }

                String productPrice = StringUtil.EMPTY;
                if (productDocument.select("meta[property='og:product:price']").size() > 0) {
                    productPrice = productDocument.select("meta[property='og:product:price']").get(0).attr("content");
                }
                if (StringUtil.isEmpty(productPrice)) {
                    productPrice = productDocument.getElementById("goods-sale-price").text();
                }
                String productSpecs = StringUtil.EMPTY;
                try {
                    productSpecs = productDocument.getElementsByClass("goods-select").parents().get(0).text();
                } catch (Exception e) {
                }
                String productUnit = StringUtil.EMPTY;
                if (StringUtil.isNotEmpty(productSpecs)) {
                    productUnit = productSpecs;
                }
                String productImage = StringUtil.EMPTY;
                String brand = StringUtil.EMPTY;
                try {
                    if (productDocument.getElementById("zoom1") != null) {
                        productImage = productDocument.getElementById("zoom1").attr("href");
                    }
                    if (StringUtil.isEmpty(productImage)) {
                        if (productList.get(i).getElementsByClass("gd-photoimg").size() > 0) {
                            productImage = productList.get(i).getElementsByClass("gd-photoimg").get(0).attr("src0");
                        }
                    }
                    if (StringUtil.isEmpty(productImage)) {
                        System.out.println();
                    }
                    brand = productDocument.getElementsByClass("fontline fl").get(0).text();
                    brand = brand.replaceAll("品牌馆", "");
                } catch (Exception e) {
                }
                String productId = "";
                String patternStr = "/[0-9]*.html";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(productLink);
                if (matcher.find()) {
                    productId = matcher.group();
                    productId = productId.replaceAll(".html", "").replaceAll("/", "");
                }
                PetsProduct product = buildProduct(productId, category, productName, brand, productUnit, productImage, productLink, productPrice, productSpecs);
                products.add(product);

            }
        }
        return products;
    }

    @Override
    public void search() throws Exception {
        try {
            super.search(petsProductMapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPageLink(int page, Category category, Map<String, String> otherParams) {
        String firstPageUrl = category.getCategoryLink();
        String nextPage = firstPageUrl.replaceAll(".html", "") + "b1f" + page + ".html";
        return nextPage;
    }

    @Override
    public String getSource() {
        return Website.ECDOG.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.ECDOG.getBaseCategoryUrl();
    }
}
