package com.zhuayinline.pets.crawler.service.impl;

import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.AbstractPetsCall;
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
 * @className DangdangService
 * @description TODO
 * @date 2021-1-29 17:56:36
 */
@Service
public class DangdangService extends AbstractPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    private static final String PETSKEYWORKDS = "宠物";


    @Override
    public String getSource() {
        return Website.DANGDANG.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.DANGDANG.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(getCategoryBaseUrl());
        Elements rootCategoryElement = document.getElementsByClass("classify_books");

        for (int index = 0; index < rootCategoryElement.size(); index++) {

            Element categoryDoc = rootCategoryElement.get(index);
            String category1 = categoryDoc.select("h3[class='classify_title']").text();

            String category2;
            String category3;

            Elements secondDocs = categoryDoc.getElementsByClass("classify_kind");
            for (Element secondDoc : secondDocs) {
                category2 = secondDoc.getElementsByClass("classify_kind_name").text();
                Elements thirdCategoryDoc = secondDoc.select("ul[class='classify_kind_detail'] li");
                if (CollectionUtils.isNotEmpty(thirdCategoryDoc)) {
                    for (Element element : thirdCategoryDoc) {
                        category3 = element.text();
                        String categoryLink = element.select("a").attr("href");
                        if (category1.contains(PETSKEYWORKDS)
                                || category2.contains(PETSKEYWORKDS)
                                || category2.contains(PETSKEYWORKDS)) {
                            if (categoryLink.contains(".html")) {
                                Category category = buildCategory(category1, category2, category3, categoryLink);
                                categories.add(category);
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
            String totalPage = document.getElementsByClass("data").text();
            totalPage = totalPage.replaceAll("1/", "").trim();
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
        if (CollectionUtils.isNotEmpty(document.getElementsByClass("bigimg cloth_shoplist"))
                && CollectionUtils.isNotEmpty(document.getElementsByClass("bigimg cloth_shoplist").get(0).children())) {
            Elements productList = document.getElementsByClass("bigimg cloth_shoplist").get(0).children();
            PetsProduct product = null;
            for (Element element : productList) {
                try {
                    String productId = element.attr("id");
                    String productLink = element.select("a").attr("href");
                    String imageLink = element.select("a img").attr("data-original");
                    String productName = element.select("a").attr("title");

                    if (StringUtil.isNotEmpty(productLink)) {
                        Document productDocument = searchUtil.getDocument(productLink);
                        if (productDocument == null) {
                            continue;
                        }
                        String productPrice = StringUtil.EMPTY;
                        if (element.getElementsByClass("price_n") != null) {
                            String price_n = element.getElementsByClass("price_n").text();
                            productPrice = price_n.replaceAll("¥", "");
                        }
                        String brand = StringUtil.EMPTY;
                        String brands = "品牌";
                        String model = "适用";
                        String model2 = "种类";

                        String productSpecs = StringUtil.EMPTY;
                        if (productDocument.getElementsByClass("pro_content") != null) {
                            Elements fontDoc = productDocument.select("ul[class=key clearfix] li");
                            for (int i = 0; i < fontDoc.size(); i++) {
                                String text = fontDoc.get(i).text();
                                if (StringUtil.isEmpty(text)) {
                                    continue;
                                }
                                String value = StringUtil.EMPTY;
                                Elements valueElement = fontDoc.get(i).select("a");
                                if (CollectionUtils.isNotEmpty(valueElement)) {
                                    value = valueElement.text();
                                }
                                if (text.contains(brands)) {
                                    brand = value;
                                }
                                if (text.contains(model) || text.contains(model2)) {
                                    productSpecs = value;
                                }
                            }

                        }

                        String productUnit = productSpecs;

                        product = buildProduct(productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (null != product) {
                    products.add(product);
                }
            }
        }
        return products;
    }

    @Override
    public String search() {
        try {
            return super.search(petsProductMapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtil.EMPTY;
    }

    @Override
    public String getPageLink(int page, Category category, Map<String, String> otherParams) {
        String firstPageUrl = category.getCategoryLink();
        String pageLink = firstPageUrl.replaceAll("dangdang.com/", "dangdang.com/pg" + page + "-");
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
