package com.zhuayinline.pets.crawler.service.impl;

import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.IPetsCall;
import com.zhuayinline.pets.crawler.util.SearchUtil;
import com.zhuayinline.pets.crawler.util.StringUtil;
import com.zhuayinline.pets.crawler.vo.Category;
import com.zhuayinline.pets.crawler.vo.Website;
import org.apache.commons.collections.CollectionUtils;
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

/**
 * @author Devin Zhang
 * @className GoodmaoningService
 * @description TODO
 * @date 2021-1-23 15:32:20
 */
@Service
public class GoodmaoningService extends IPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;


    @Override
    public String getSource() {
        return Website.CWSC.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.CWSC.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(getCategoryBaseUrl());
        Elements rootCategoryElement = document.getElementsByClass("categorys-item");

        for (int index = 0; index < rootCategoryElement.size(); index++) {

            Element categoryDoc = rootCategoryElement.get(index);
            String category1 = categoryDoc.getElementsByClass("categorys-title").get(0).select("strong").text();
            String category2 = StringUtil.EMPTY;
            String category3;
            String categoryLink;
            Elements secondCategoryDoc = categoryDoc.getElementsByClass("dl_fore1");
            if (CollectionUtils.isNotEmpty(secondCategoryDoc)) {
                for (Element element : secondCategoryDoc) {
                    Elements children = element.children();
                    for (Element childDoc : children) {
                        if (childDoc.tagName().equalsIgnoreCase("dt")) {
                            category2 = childDoc.text();
                        } else if (childDoc.tagName().equalsIgnoreCase("dd")) {
                            Elements thirdCategoryDoc = childDoc.children();
                            for (Element a : thirdCategoryDoc) {
                                category3 = a.text();
                                categoryLink = a.attr("href");
                                if (StringUtil.isNotEmpty(categoryLink)) {
                                    categoryLink = Website.CWSC.getWebsiteUrl() + categoryLink;
                                    Category category = buildCategory(category1, category2, category3, categoryLink);
                                    categories.add(category);
                                }
                            }
                        }
                    }
                }
            }
        }
        return categories;
    }

    @Override
    public Object[] getCategoryProductCount(String categoryProductUrl) throws Exception {
        Object[] objs = new Object[2];
        Integer page = 1;
        Document document = searchUtil.getDocument(categoryProductUrl);
        try {
            Elements pageDoc = document.getElementsByClass("pg");
            if (CollectionUtils.isNotEmpty(pageDoc)) {
                Elements pageNoDoc = pageDoc.get(0).select("span[title^='共']");
                if (CollectionUtils.isNotEmpty(pageNoDoc)) {
                    String totalPage = pageNoDoc.text().
                            replaceAll("/", "")
                            .replaceAll("页", "")
                            .trim();
                    page = Integer.parseInt(totalPage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        objs[0] = page;
        return objs;
    }

    @Override
    public List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception {
        List<PetsProduct> products = new LinkedList<>();

        Document document = searchUtil.getDocument(categoryProductUrl);
        Elements productList = document.getElementsByClass("thumb");
        if (CollectionUtils.isNotEmpty(productList)) {
            for (Element element : productList) {
                String productLink = Website.CWSC.getWebsiteUrl() + element.select("a").attr("href");
                String imageLink = element.select("a img").attr("src");
                String productName = element.select("a img").attr("alt");
                String productId = productLink.replaceAll(Website.CWSC.getWebsiteUrl(), "")
                        .replaceAll(".html", "");
                String brand = StringUtil.EMPTY;
                if (element.parent() != null && CollectionUtils.isNotEmpty(element.parent().getElementsByClass("lj_qq"))) {
                    brand = element.parent().getElementsByClass("lj_qq").text();
                    brand = brand.replaceAll("\\.", "")
                            .replaceAll(":", "")
                            .replaceAll("旗舰店", "")
                            .replaceAll(" ", "")
                            .trim();
                }
                if (StringUtil.isNotEmpty(productLink)) {
                    Document productDocument = searchUtil.getDocument(productLink);
                    if (productDocument == null) {
                        continue;
                    }
                    String productPrice;
                    String priceSource = productDocument.getElementById("sku_price").text().replaceAll("￥", "").trim();
                    if (priceSource.contains("~")) {
                        productPrice = priceSource.split("~")[0];
                    } else {
                        productPrice = priceSource;
                    }
                    String productSpecs = StringUtil.EMPTY;
                    Elements goodsSku = productDocument.getElementsByClass("goods_sku f_v tb-selected");
                    if (CollectionUtils.isNotEmpty(goodsSku)) {
                        try {
                            productSpecs = goodsSku.text();
                        } catch (Exception e) {

                        }
                    }

                    //从产品名称中取产品规格
                    String finder = StringUtil.EMPTY;
                    String units = "kg|KG|g|千克|克|磅|G|cm|CM|w|W|ml|包";
                    String patternStr = "(\\d*\\.)?[0-9]+(" + units + ")";
                    Pattern pattern = Pattern.compile(patternStr);
                    Matcher matcher = pattern.matcher(productName);
                    if (matcher.find()) {
                        finder = matcher.group();
                    }

                    if (StringUtil.isEmpty(productSpecs)
                            || StringUtil.isNotEmpty(finder)
                            || productSpecs.trim().equalsIgnoreCase("w")) {
                        productSpecs = finder;
                    }
                    String productUnit = productSpecs;
                    PetsProduct product = buildProduct(productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
                    products.add(product);
                }
            }
        }
        return products;
    }

    @Override
    public void search() {
        try {
            super.search(petsProductMapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPageLink(int page, Category category, Map<String, String> otherParams) {
        String pageLink = category.getCategoryLink();
        if (pageLink.endsWith(".html")) {
            pageLink = pageLink + "?page=" + page;
        } else {
            pageLink = pageLink + "&page=" + page;
        }
        return pageLink;
    }
}
