package com.zhuayinline.pets.crawler.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.IPetsCall;
import com.zhuayinline.pets.crawler.util.HttpUtil;
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

/**
 * @author Devin Zhang
 * @className SuningService
 * @description TODO
 * @date 2021-1-24 11:44:32
 */
@Service
public class SuningService extends IPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    private static final String PETSKEYWORKDS = "宠物";

    private static final String SUNNING_PRICE_URL = "https://icps.suning.com/icps-web/getVarnishAllPriceNoCache/0000000PID2_010_0100100_PID1_1_getClusterPrice.jsonp?callback=getClusterPrice";


    @Override
    public String getSource() {
        return Website.SUNING.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.SUNING.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(getCategoryBaseUrl());
        Elements rootCategoryElement = document.getElementsByClass("search-main introduce clearfix").get(0).children();

        for (int index = 0; index < rootCategoryElement.size(); index++) {

            Element categoryDoc = rootCategoryElement.get(index);
            String category1 = categoryDoc.select("h2").text();

            String category2;
            String category3;

            Elements subCategory = categoryDoc.getElementsByClass("title-box");
            for (int j = 0; j < subCategory.size(); j++) {
                Elements secondCategoryDoc = subCategory.get(j).getElementsByClass("t-left");
                if (secondCategoryDoc.size() == 0) {
                    continue;
                }
                category2 = secondCategoryDoc.get(0).text();
                Elements thirdCategoryDoc = subCategory.get(j).getElementsByClass("t-right");
                if (CollectionUtils.isNotEmpty(thirdCategoryDoc) && CollectionUtils.isNotEmpty(thirdCategoryDoc.get(0).children())) {
                    Elements children = thirdCategoryDoc.get(0).children();
                    for (Element element : children) {
                        category3 = element.text();
                        String categoryLink = Website.SUNING.getBaseCategoryUrl() + element.attr("href");

                        if (category1.contains(PETSKEYWORKDS)
                                || category2.contains(PETSKEYWORKDS)
                                || category2.contains(PETSKEYWORKDS)) {
                            Category category = buildCategory(category1, category2, category3, categoryLink);
                            categories.add(category);
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
            String totalPage = document.getElementsByClass("fl").get(0).text()
                    .replaceAll("1/", "")
                    .replaceAll("1 /", "").trim();
            page = Integer.parseInt(totalPage);
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
        if (CollectionUtils.isNotEmpty(document.getElementsByClass("product-list"))) {
            Elements productList = document.getElementsByClass("item-wrap");
            for (Element element : productList) {
                String productId = element.attr("id");
                Element element1 = element.getElementsByClass("img-block").get(0);
                String productLink = HTTPS + element1.select("a").attr("href");
                String imageLink = HTTPS + element1.select("a img").attr("src");
                String productName = element1.select("a img").attr("alt");

                if (StringUtil.isNotEmpty(productLink)) {
                    Document productDocument = searchUtil.getDocument(productLink);
                    Thread.sleep(5 * 1000);
                    if (productDocument == null) {
                        continue;
                    }
                    int begin = productLink.indexOf(".com") + 5;
                    int end = productLink.indexOf(".html");
                    String subString = productLink.substring(begin, end);

                    String pId1 = subString.split("/")[0];
                    String pId2 = subString.split("/")[1];

                    String productPriceLink = SUNNING_PRICE_URL;
                    productPriceLink = productPriceLink.replaceAll("PID1", pId1).replaceAll("PID2", pId2);

                    String priceText = HttpUtil.doGET(productPriceLink);
                    if (StringUtil.isEmpty(priceText)) {
                        continue;
                    }

                    int begin1 = priceText.indexOf("(") + 1;
                    String priceJsonStr = priceText.substring(begin1, priceText.length() - 2);
                    JSONArray jsonObj = (JSONArray) JSONObject.parse(priceJsonStr);
                    if (jsonObj == null || jsonObj.size() == 0) {
                        continue;
                    }

                    String productPrice = ((JSONObject) jsonObj.get(0)).getString("price");
                    Thread.sleep(5 * 1000);
                    if (StringUtil.isEmpty(productPrice)) {
                        productPrice = "0";
                    }
                    String brand = StringUtil.EMPTY;
                    String brands = "品牌";
                    String model = "适用";
                    String model2 = "尺码";

                    String productSpecs = StringUtil.EMPTY;
                    if (productDocument.getElementsByClass("cnt clearfix") != null) {
                        Elements fontDoc = productDocument.select("ul[class=cnt clearfix] li");
                        for (int i = 0; i < fontDoc.size(); i++) {
                            String text = fontDoc.get(i).text();
                            if (StringUtil.isEmpty(text)) {
                                continue;
                            }
                            String value = fontDoc.get(i).attr("title");
                            if (text.contains(brands)) {
                                brand = value;
                            }
                            if (text.contains(model) || text.contains(model2)) {
                                productSpecs = value;
                            }
                        }

                    }
                    if (StringUtil.isEmpty(productSpecs)) {
                        Elements selected = productDocument.getElementsByClass("clr-item selected");
                        if (CollectionUtils.isNotEmpty(selected)) {
                            productSpecs = selected.text();
                        }
                    }
                    if (StringUtil.isEmpty(productSpecs)) {
                        Elements selected = productDocument.getElementsByClass("selected");
                        if (CollectionUtils.isNotEmpty(selected)) {
                            productSpecs = selected.text();
                        }
                    }
                    String productUnit = StringUtil.EMPTY;
                    if (StringUtil.isNotEmpty(productSpecs)) {
                        productUnit = productSpecs;
                    }

                    PetsProduct product = buildProduct(productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
                    products.add(product);
                    Thread.sleep(5000);
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
        String firstPageUrl = category.getCategoryLink();
        String pageLink = firstPageUrl.replaceAll("0.html", (page - 1) + ".html");
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
