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

/**
 * @author Devin Zhang
 * @className JD servvice
 * @description TODO
 * @date 2021-1-30 09:09:23
 */
@Service
public class JDService extends IPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    private static final String PETSKEYWORKDS = "宠物";


    @Override
    public String getSource() {
        return Website.JD.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.JD.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(getCategoryBaseUrl());
        Elements rootCategoryElement = document.getElementsByClass("category-item m");

        for (int index = 0; index < rootCategoryElement.size(); index++) {

            Element categoryDoc = rootCategoryElement.get(index);
            String category1 = categoryDoc.select("h2[class='item-title']").text();
            String category2;
            String category3;

            Elements secondDocs = categoryDoc.getElementsByClass("clearfix");
            for (Element secondDoc : secondDocs) {
                category2 = secondDoc.select("dt").text();
                Elements thirdCategoryDoc = secondDoc.select("dd a");
                if (CollectionUtils.isNotEmpty(thirdCategoryDoc)) {
                    for (Element element : thirdCategoryDoc) {
                        category3 = element.text();
                        String categoryLink = HTTPS + element.attr("href");
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
            String totalPage = document.getElementsByClass("fp-text").text();
            totalPage = totalPage.replaceAll("1/", "").trim();
            page = Integer.parseInt(totalPage);
            //（页面有异步加载部分，页数是界面显示的2倍）
            page = page * 2;
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
        if (CollectionUtils.isNotEmpty(document.getElementsByClass("gl-warp clearfix"))
                && CollectionUtils.isNotEmpty(document.getElementsByClass("gl-warp clearfix").get(0).children())) {
            Elements productList = document.getElementsByClass("gl-warp clearfix").get(0).children();
            for (Element element : productList) {
                String productId = element.attr("data-sku");
                String productLink = HTTPS + element.select("div[class='p-img'] a").attr("href");
                String imageLink = element.select("div[class='p-img'] a img").attr("src");
                String productPrice = StringUtil.EMPTY;
                if (element.getElementsByClass("p-price") != null) {
                    String price_n = element.getElementsByClass("price_n").text();
                    productPrice = price_n.replaceAll("¥", "").replaceAll("￥", "");
                }
                if (StringUtil.isNotEmpty(productLink)) {
                    Document productDocument = searchUtil.getDocument(productLink);
                    if (productDocument == null) {
                        continue;
                    }
                    String productName = productDocument.getElementsByClass("sku-name").text();

                    String brand = StringUtil.EMPTY;
                    if (null != productDocument.getElementById("parameter-brand")) {
                        brand = productDocument.getElementById("parameter-brand").text();
                    }
                    String model = "毛重";
                    String model2 = "规格";

                    String productSpecs = StringUtil.EMPTY;
                    if (null != productDocument.getElementsByClass("item  selected")) {
                        productSpecs = productDocument.getElementsByClass("item  selected").text();
                    }
                    if (StringUtil.isEmpty(productSpecs)) {
                        if (productDocument.getElementsByClass("parameter2 p-parameter-list") != null) {
                            Elements fontDoc = productDocument.select("ul[class=parameter2 p-parameter-list] li");
                            for (int i = 0; i < fontDoc.size(); i++) {
                                String text = fontDoc.get(i).text();
                                if (StringUtil.isEmpty(text)) {
                                    continue;
                                }
                                if (text.contains(model) || text.contains(model2)) {
                                    productSpecs = text;
                                }
                            }
                        }
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
        final int len = 30;
        String firstPageUrl = category.getCategoryLink();
        int s = (page - 1) * len + 1;
        String pageLink = firstPageUrl + "&page=" + page + "&s=" + s + "&click=0";
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
