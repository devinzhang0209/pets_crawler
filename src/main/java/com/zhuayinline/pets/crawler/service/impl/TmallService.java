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

import javax.print.Doc;
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
public class TmallService extends IPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    private static final String BASEPAGELINK = "https://list.tmall.com/search_product.htm";

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
        Elements rootCategoryElement = document.getElementsByClass("j_MenuNav nav-item a");

        for (int index = 0; index < rootCategoryElement.size(); index++) {

            Element categoryDoc = rootCategoryElement.get(index);
            String category1 = "宠物";
            String category2 = categoryDoc.text();
            if (StringUtil.isNotEmpty(category2) && category2.contains(category1)) {
                String category3 = "宠物食品及用品";
                String categoryLink = categoryDoc.attr("href");
                Category category = buildCategory(category1, category2, category3, categoryLink);
                categories.add(category);
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
            String totalPage = document.getElementsByClass("ui-page-s-len").text();
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
        if (CollectionUtils.isNotEmpty(document.getElementsByClass("view grid-nosku"))
                && CollectionUtils.isNotEmpty(document.getElementsByClass("view grid-nosku").get(0).children())) {
            Elements productList = document.getElementsByClass("view grid-nosku").get(0).children();
            for (Element element : productList) {
                String productId = element.attr("data-id");
                String productLink = HTTPS + element.select("div[class='productImg-wrap'] a").attr("href");
                String imageLink = HTTPS + element.select("div[class='productImg-wrap'] a img").attr("src");
                String productName = element.getElementsByClass("productTitle").text();
                String companyName = element.getElementsByClass("productShop").text();

                String productPrice = StringUtil.EMPTY;
                if (element.getElementsByClass("productPrice") != null) {
                    String price_n = element.getElementsByClass("productPrice").text();
                    productPrice = price_n.replaceAll("¥", "").replaceAll("￥", "");
                }
                if (StringUtil.isNotEmpty(productLink)) {
                    Document productDocument = searchUtil.getDocument(productLink);
                    if (productDocument == null) {
                        continue;
                    }

                    String brand = StringUtil.EMPTY;
                    if (null != productDocument.getElementById("parameter-brand")) {
                        brand = productDocument.getElementById("parameter-brand").text();
                    }

                    String productSpecs = StringUtil.EMPTY;
                    if (CollectionUtils.isNotEmpty(productDocument.getElementsByClass("tm-item-weight"))
                            && CollectionUtils.isNotEmpty(productDocument.getElementsByClass("tm-item-weight").get(0).select("dd em"))
                    ) {
                        productSpecs = productDocument.getElementsByClass("tm-item-weight").get(0).select("dd em").text();
                    }
                    if (StringUtil.isEmpty(productSpecs) && CollectionUtils.isNotEmpty(productDocument.getElementsByClass("tb-selected"))) {
                        productSpecs = productDocument.getElementsByClass("tb-selected").text();
                    }

                    String brandKeyword = "品牌";
                    String model = "毛重";
                    String model2 = "规格";
                    String model3 = "适用";

                    if (productDocument.getElementById("J_AttrUL") != null) {
                        Elements fontDoc = productDocument.getElementById("J_AttrUL").children();
                        for (int i = 0; i < fontDoc.size(); i++) {
                            String text = fontDoc.get(i).text();
                            String value = fontDoc.get(i).attr("title");

                            if (StringUtil.isEmpty(text)) {
                                continue;
                            }
                            if (StringUtil.isEmpty(productSpecs) &&
                                    (text.contains(model) || text.contains(model2) || text.contains(model3))) {
                                productSpecs = value;
                            }
                            if (text.contains(brandKeyword)) {
                                brand = value;
                            }
                        }

                    }
                    if (StringUtil.isEmpty(brand)) {
                        brand = companyName;
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
        final int len = 60;
        String firstPageUrl = category.getCategoryLink();
        int s = page * len;

        Document document = searchUtil.getDocument(firstPageUrl);
        Elements elementsByClass = document.getElementsByClass("ui-page-s-next");
        if (null != elementsByClass && CollectionUtils.isNotEmpty(elementsByClass)) {
            String href = elementsByClass.get(0).attr("href");
            String pageLink = BASEPAGELINK + href;
            System.out.println(String.format("pageLink:%s", pageLink));
            return pageLink.replaceAll("&s=60", "&s=" + s);
        }
        return null;
    }
}
