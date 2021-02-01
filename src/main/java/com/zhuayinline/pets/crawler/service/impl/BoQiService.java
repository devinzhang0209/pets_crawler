package com.zhuayinline.pets.crawler.service.impl;

import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.AbstractPetsCall;
import com.zhuayinline.pets.crawler.util.SearchUtil;
import com.zhuayinline.pets.crawler.util.StringUtil;
import com.zhuayinline.pets.crawler.vo.Category;
import com.zhuayinline.pets.crawler.vo.Website;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Devin Zhang
 * @className BoQiService
 * @description TODO
 * @date 2020-12-9 09:13:03
 */
@Service
public class BoQiService extends AbstractPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;


    @Override
    public String getSource() {
        return Website.BQ.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.BQ.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(getCategoryBaseUrl());
        Elements leftRightDocument = document.getElementsByClass("classify_h");

        for (int index = 0; index < 2; index++) {

            Element partDocument = leftRightDocument.get(0).child(index);

            String category1 = StringUtil.EMPTY;
            String category2;
            String category3;
            String categoryProductLink;

            Elements categoryChild = partDocument.children();
            for (int i = 0; i < categoryChild.size(); i++) {
                Element leftChild = categoryChild.get(i);


                if (leftChild.hasClass("classify_tit")) {
                    category1 = leftChild.child(0).text();
                } else if (leftChild.hasClass("classify_list")) {
                    category2 = leftChild.children().tagName("dt").get(0).text();
                    Elements category3Nodes = leftChild.children().tagName("dd").get(1).getElementsByTag("a");
                    for (int j = 0; j < category3Nodes.size(); j++) {
                        category3 = category3Nodes.get(j).text();
                        categoryProductLink = category3Nodes.get(j).attr("href");
                        String link = HTTPS + categoryProductLink;
                        Category category = buildCategory(category1, category2, category3, link);
                        categories.add(category);
                        System.out.println(String.format("%s,%s,%s,%s", category1, category2, category3, categoryProductLink));
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
        String totalPage = document.getElementsByClass("product_page_total").text();
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
        Elements productList = document.getElementsByClass("product_list_container");
        for (Element element : productList) {
            PetsProduct product = null;
            try {
                String productLink = element.child(0).attr("href");
                String imageLink = element.child(0).tagName("img").child(0).attr("data-original");
                if (StringUtil.isNotEmpty(productLink)) {
                    Document productDocument = searchUtil.getDocument(productLink);
                    if (productDocument == null || productDocument.getElementById("goodname") == null) {
                        continue;
                    }
                    String productName = productDocument.getElementById("goodname").val();
                    String productPrice = productDocument.getElementById("yhhcast").val();
                    String brand = productDocument.getElementsByClass("brand").first().getElementsByTag("a").first().text();
                    String productSpecs = productDocument.getElementsByClass("change current").text();
                    if (StringUtil.isEmpty(productSpecs)) {
                        try {
                            productSpecs = productDocument.getElementsByClass("property").first().getElementsByTag("td").get(1).getElementsByTag("span").text();
                        } catch (Exception e) {

                        }
                    }
                    String productUnit = StringUtil.EMPTY;
                    if (StringUtil.isNotEmpty(productSpecs)) {
                        productUnit = productSpecs;
                    }

                    String productId = "";
                    String patternStr = "-[0-9]*.html";
                    Pattern pattern = Pattern.compile(patternStr);
                    Matcher matcher = pattern.matcher(productLink);
                    if (matcher.find()) {
                        productId = matcher.group();
                        productId = productId.replaceAll(".html", "").replaceAll("-", "");
                    }
                    product = buildProduct(productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
                }
                if (null != product) {
                    products.add(product);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        String pageLink = firstPageUrl.replaceAll(".html", "") + "-0-0-p" + page + ".html";
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
