package com.zhuayinline.pets.crawler.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.AbstractPetsCall;
import com.zhuayinline.pets.crawler.util.AsciiConvertUtil;
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
 * @className ZhiDeMaiService
 * @description TODO
 * @date 2021-2-2 22:46:10
 */
@Service
public class ZhiDeMaiService extends AbstractPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    private static final String category1 = "宠物";
    private static final String PAGE_BASE_LINK = "https://search.smzdm.com/?c=home&s=keyword&order=score&mall_id=243&v=b&p=page";
    private static final String PAGE_BASE_LINK2 = "https://search.smzdm.com/?c=home&s=keyword&order=score&mall_id=2897&v=b&p=page";
    private static final String PAGE_BASE_LINK3 = "https://search.smzdm.com/?c=home&s=keyword&order=score&mall_id=247&v=b&p=page";

    @Override
    public String getSource() {
        return Website.ZHIDEMAI.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.ZHIDEMAI.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        String res = HttpUtil.doGET(getCategoryBaseUrl());

        int begin1 = res.indexOf("(") + 1;
        String priceJsonStr = res.substring(begin1, res.length() - 1);
        JSONObject jsonObj = JSONObject.parseObject(priceJsonStr);
        JSONArray arr = jsonObj.getJSONArray("data");
        for (Object o : arr) {
            String category2 = (String) o;
            if (StringUtil.isEmpty(category2)) {
                continue;
            }
            String categoryLink1 = PAGE_BASE_LINK.replaceAll("keyword", category2)
                    .replaceAll("page", "1");
            String categoryLink2 = PAGE_BASE_LINK2.replaceAll("keyword", category2)
                    .replaceAll("page", "1");
            String categoryLink3 = PAGE_BASE_LINK3.replaceAll("keyword", category2)
                    .replaceAll("page", "1");

            Category categoryModel1 = buildCategory(category1, category2, StringUtil.EMPTY, categoryLink1);
            Category categoryModel2 = buildCategory(category1, category2, StringUtil.EMPTY, categoryLink2);
            Category categoryModel3 = buildCategory(category1, category2, StringUtil.EMPTY, categoryLink3);

            categories.add(categoryModel1);
            categories.add(categoryModel2);
            categories.add(categoryModel3);
        }
        return categories;
    }

    @Override
    public Object[] getCategoryProductCount(String categoryProductUrl) throws Exception {
        Object[] objs = new Object[2];
        Integer page = 100;
        objs[0] = page;
        return objs;
    }

    @Override
    public List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception {
        List<PetsProduct> products = new LinkedList<>();

        Document document = searchUtil.getDocument(categoryProductUrl, 1);
        if (document.getElementById("feed-main-list") != null
                && CollectionUtils.isNotEmpty(document.getElementById("feed-main-list").children())) {
            Elements productList = document.getElementById("feed-main-list").children();
            for (Element element : productList) {
                PetsProduct product = null;
                try {
                    String currentLink = element.select("div[class=z-feed-img] a").attr("href");
                    if (StringUtil.isEmpty(currentLink)) {
                        continue;
                    }
                    String imageLink = HTTPS + element.select("div[class=z-feed-img] a img").attr("src");

                    System.out.println("currentLink:" + currentLink);
                    String[] arr = currentLink.split("/");
                    String productId = arr[arr.length - 1];

                    String source = element.getElementsByClass("feed-block-extras").text();
                    String sourceName;
                    if (source.contains("天猫")) {
                        sourceName = Website.TMALL.getWebsiteName();
                    } else {
                        sourceName = Website.TAOBAO.getWebsiteName();
                    }

                    String productLink = currentLink;
                    String productName = element.select("div[class=z-feed-img] a img").attr("alt");

                    String productPrice = element.getElementsByClass("z-highlight").text();
                    if (StringUtil.isNotEmpty(productPrice)) {
                        String[] priceArr = productPrice.split("元");
                        try {
                            productPrice = priceArr[0];
                        } catch (Exception e) {
                            productPrice = StringUtil.EMPTY;
                        }
                    }

                    String brand = StringUtil.EMPTY;
                    String productSpecs = StringUtil.EMPTY;
                    String productUnit = productSpecs;

                    product = buildProduct(sourceName, productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (null != product) {
                    products.add(product);
                }
            }
        }
        Thread.sleep(20 * 1000);
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
        String pageLink = firstPageUrl.replaceAll("&p=1", "&p=" + page);
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
