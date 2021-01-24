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
 * @className MCTService
 * @description TODO
 * @date 2021-1-20 22:36:16
 */
@Service
public class MCTService extends IPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;


    @Override
    public String getSource() {
        return Website.MCT.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.MCT.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(getCategoryBaseUrl());
        Elements rootCategoryElement = document.getElementsByClass("flbox_cat");

        for (int index = 0; index < rootCategoryElement.size(); index++) {

            Element categoryDoc = rootCategoryElement.get(index);
            String category1 = categoryDoc.getElementsByClass("fltitie" + (index + 1)).get(0).text();
            String category2 = StringUtil.EMPTY;
            String category3;

            Elements subCategory = categoryDoc.getElementsByTag("li");
            for (int j = 0; j < subCategory.size(); j++) {
                Elements secondDocs = subCategory.get(j).children();
                for (int k = 0; k < secondDocs.size(); k++) {
                    String text = secondDocs.get(k).text();
                    if (k == 0) {
                        category2 = text;
                        if (StringUtil.isNotEmpty(category2)) {
                            category2 = category2.replaceAll("：", "")
                                    .replaceAll(":", "").trim();
                        }
                    } else {
                        category3 = text;
                        if (StringUtil.isEmpty(category3)) {
                            continue;
                        }
                        String categoryLink = secondDocs.get(k).attr("href");
                        Category category = buildCategory(category1, category2, category3, categoryLink);


                        System.out.println(String.format("%s,%s,%s,%s", category1, category2, category3, categoryLink));

                        categories.add(category);
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
        String totalPage = document.getElementsByClass("txt").text().replaceAll("1/", "");
        try {
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
        if (CollectionUtils.isNotEmpty(document.getElementsByClass("list-all"))) {
            Elements productList = document.getElementsByClass("list-all").get(0).children();
            for (Element element : productList) {
                String productLink = element.getElementsByClass("pic fly_img").get(0).select("a").attr("href");
                String imageLink = element.getElementsByClass("pic fly_img").get(0).select("img").attr("src");
                if (StringUtil.isNotEmpty(productLink)) {
                    Document productDocument = searchUtil.getDocument(productLink);
                    if (productDocument == null || productDocument.getElementById("base_name-sf") == null) {
                        continue;
                    }
                    String productName = productDocument.getElementById("base_name-sf").text();
                    String productPrice = productDocument.getElementById("ECS_SHOPPRICE").text().replaceAll("￥", "").trim();
                    String brand = StringUtil.EMPTY;
                    String productSpecs = StringUtil.EMPTY;
                    if (productDocument.getElementById("J_des") != null) {
                        Elements liElements = productDocument.getElementById("J_des").select("li");
                        if (CollectionUtils.isNotEmpty(liElements)) {
                            brand = productDocument.getElementById("J_des").select("li").get(0).select("a").text();
                            if (liElements.size() >= 4) {
                                productSpecs = productDocument.getElementById("J_des").select("li").get(3).text().replaceAll("重量：", "");
                            }
                        }
                    }

                    String productUnit = StringUtil.EMPTY;
                    if (StringUtil.isNotEmpty(productSpecs)) {
                        productUnit = productSpecs;
                    }

                    String productId = element.attr("id").replaceAll("goods_id_", "");


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
        String firstPageUrl = category.getCategoryLink();
        String pageLink = firstPageUrl + "&page=" + page;
        return pageLink;
    }
}
