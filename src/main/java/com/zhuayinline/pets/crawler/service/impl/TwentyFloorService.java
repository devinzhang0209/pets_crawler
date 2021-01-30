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
 * @className TwentyFloorService
 * @description TODO
 * @date 2021-1-21 22:33:28
 */
@Service
public class TwentyFloorService extends IPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;


    @Override
    public String getSource() {
        return Website.TWLCWYPSC.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.TWLCWYPSC.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(getCategoryBaseUrl());
        Elements rootCategoryElement = document.getElementsByClass("ulMall").select("li");

        for (int index = 0; index < rootCategoryElement.size(); index++) {

            Element categoryDoc = rootCategoryElement.get(index);
            String category1 = categoryDoc.getElementsByClass("mallLiNameTM").get(0).text();
            Elements secondCatetories = categoryDoc.getElementsByClass("secGroupBox");
            for (int j = 0; j < secondCatetories.size(); j++) {
                String category2 = categoryDoc.getElementsByClass("secGroupName").get(j).text();
                Elements thirdCategory = categoryDoc.getElementsByClass("thdGroupBox");
                for (int k = 0; k < thirdCategory.size(); k++) {
                    final Element thirdDoc = thirdCategory.get(k);
                    String category3 = thirdDoc.text();
                    String link = thirdDoc.attr("onclick");
                    link = link.replaceAll("javascript:window.location.href=", "").replaceAll("\"", "").replaceAll(";", "");
                    String categoryLink = Website.TWLCWYPSC.getBaseCategoryUrl() + link;

                    Category category = buildCategory(category1, category2, category3, categoryLink);
                    categories.add(category);
                    System.out.println(String.format("%s,%s,%s,%s", category1, category2, category3, categoryLink));

                }
            }
        }
        return categories;
    }

    @Override
    public Object[] getCategoryProductCount(String categoryProductUrl) throws Exception {
        Object[] objs = new Object[2];
        Integer page = 1;
        String pageLink = StringUtil.EMPTY;
        //pageTotal
        Document document = searchUtil.getDocument(categoryProductUrl);
        String totalPage = document.getElementsByClass("pageTotal").text().replaceAll("/", "");
        try {
            page = Integer.parseInt(totalPage);
            Elements pageDiv = document.getElementsByClass("g_border js_pagination");
            if (CollectionUtils.isNotEmpty(pageDiv)) {
                String page2Link = pageDiv.get(0).attr("href");
                if (StringUtil.isNotEmpty(page2Link)) {
                    pageLink = Website.TWLCWYPSC.getBaseCategoryUrl() + page2Link;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        objs[0] = page;
        objs[1] = pageLink;
        return objs;
    }

    @Override
    public List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception {
        List<PetsProduct> products = new LinkedList<>();

        System.out.println(String.format("pageLink:%s", categoryProductUrl));
        Document document = null;
        try {
            document = searchUtil.getDocument(categoryProductUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != document) {
            if (document.getElementById("productListForms22") != null
                    && CollectionUtils.isNotEmpty(document.getElementById("productListForms22").children())) {
                Elements productList = document.getElementById("productListForms22").children();
                for (Element element : productList) {
                    String productName = element.attr("productname");
                    String productId = element.attr("productid");
                    if (StringUtil.isEmpty(productId)) {
                        continue;
                    }
                    String productLink = Website.TWLCWYPSC.getBaseCategoryUrl() + element.select("tr a").get(0).attr("href");
                    String imageLink = "http:" + element.select("tr a img").get(0).attr("src");
                    String brand = StringUtil.EMPTY;
                    if (element.select("span[class='propValue']").size() > 0) {
                        brand = element.select("span[class='propValue']").get(0).text();
                    }
                    String productPrice = element.getElementsByClass("fk-prop-price").get(0).text();
                    if (StringUtil.isNotEmpty(productLink)) {
                        Thread.sleep(1000);
                        Document productDocument = searchUtil.getDocument(productLink);
                        if (productDocument == null) {
                            continue;
                        }

                        String spescs = "重量,型号,规格,使用阶段";
                        String productSpecs = StringUtil.EMPTY;
                        int j = 0;
                        if (productDocument.getElementsByClass("fk-productParamDetail") != null) {
                            Elements tdDoc = productDocument.select("table[class=fk-productParamDetail] td");
                            for (int i = 0; i < tdDoc.size(); i++) {
                                String text = productDocument.select("table[class=fk-productParamDetail] td").get(i).text();
                                if (StringUtil.isEmpty(text)) {
                                    continue;
                                }
                                text = text.replaceAll("：", "").trim();
                                if (spescs.contains(text)) {
                                    j = i;
                                    break;
                                }
                            }
                            if (j > 0 && tdDoc.size() >= (j + 1)) {
                                productSpecs = tdDoc.get((j + 1)).text();
                            }
                        }

                        String productUnit = StringUtil.EMPTY;
                        if (StringUtil.isNotEmpty(productSpecs)) {
                            productUnit = productSpecs;
                        }
                        PetsProduct product = buildProduct(productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
                        products.add(product);
                        Thread.sleep(1000);
                    }
                }
            }
        }
        Thread.sleep(1000);
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
        String pageLink = StringUtil.EMPTY;
        try {
            if (null != otherParams) {
                pageLink = otherParams.get("pageLink");
                pageLink = pageLink.replaceAll("pageno=2", "pageno=" + page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
