package com.zhuayinline.pets.crawler.service.impl;

import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.IPetsCall;
import com.zhuayinline.pets.crawler.util.DateUtil;
import com.zhuayinline.pets.crawler.util.SearchUtil;
import com.zhuayinline.pets.crawler.util.StringUtil;
import com.zhuayinline.pets.crawler.vo.Category;
import org.apache.commons.collections.CollectionUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Devin Zhang
 * @className BoQiService
 * @description TODO
 * @date 2020-12-9 09:13:03
 */
@Service
public class BoQiService implements IPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;


    private static final String SOURCE = "波奇网";
    private static final String CATEGORY_BASE_URL = "http://shop.boqii.com/allsort.html";


    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(CATEGORY_BASE_URL);
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
                        Category category = new Category();
                        category.setCategory1(category1);
                        category.setCategory2(category2);
                        category.setCategory3(category3);
                        category.setCategoryLink("https:" + categoryProductLink);
                        categories.add(category);
                        System.out.println(String.format("%s,%s,%s,%s", category1, category2, category3, categoryProductLink));
                    }
                }
            }
        }
        return categories;
    }

    @Override
    public int getCategoryProductCount(String categoryProductUrl) throws Exception {
        Integer page = 0;
        Document document = searchUtil.getDocument(categoryProductUrl);
        String totalPage = document.getElementsByClass("product_page_total").text();
        try {
            page = Integer.parseInt(totalPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return page;
    }

    @Override
    public List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception {
        List<PetsProduct> products = new LinkedList<>();

        Document document = searchUtil.getDocument(categoryProductUrl);
        Elements productList = document.getElementsByClass("product_list_container");
        for (Element element : productList) {
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

                PetsProduct product = new PetsProduct();
                product.setSource(SOURCE);
                product.setProductId(productId);
                product.setCategory1(category.getCategory1());
                product.setCategory2(category.getCategory2());
                product.setCategory3(category.getCategory3());
                product.setProductName(productName);
                product.setProductBrand(brand);
                product.setProductUnit(productUnit);
                product.setProductImageLink(imageLink);
                product.setProductLink(productLink);
                product.setProductPrice(new BigDecimal(productPrice));
                product.setProductSpecs(productSpecs);
                product.setCreatedTime(DateUtil.getNow());
                product.setLastUpdatedTime(DateUtil.getNow());

                System.out.println(product);
                products.add(product);
            }
        }
        return products;
    }

    @Override
    public void search() {
        try {
            List<Category> allCategory = getAllCategory();
            if (CollectionUtils.isNotEmpty(allCategory)) {
                for (Category category : allCategory) {
                    List<PetsProduct> products = new LinkedList<>();

                    int pageSize = getCategoryProductCount(category.getCategoryLink());
                    System.out.println("total page:"+pageSize);
                    System.out.println("begin to search the first page...");
                    //第一页
                    products.addAll(getProducts(category, category.getCategoryLink()));

                    if (pageSize > 1) {
                        for (int page = 2; page <= pageSize; page++) {
                            System.out.println("begin to search the "+page+" page");
                            String firstPageUrl = category.getCategoryLink();
                            String pageLink = firstPageUrl.replaceAll(".html", "") + "-0-0-p" + page + ".html";
                            products.addAll(getProducts(category, pageLink));
                        }
                    }

                    for (PetsProduct product : products) {
                        Example example = new Example(PetsProduct.class);
                        Example.Criteria criteria = example.createCriteria();
                        criteria.andEqualTo("source", product.getSource());
                        criteria.andEqualTo("productId", product.getProductId());
                        PetsProduct one = petsProductMapper.selectOneByExample(example);
                        if (one == null) {
                            petsProductMapper.insertSelective(product);
                        } else {
                            product.setId(one.getId());
                            product.setCreatedTime(one.getCreatedTime());
                            petsProductMapper.updateByPrimaryKeySelective(product);
                        }

                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
