package com.zhuayinline.pets.crawler.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.AbstractPetsCall;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Devin Zhang
 * @className AbstractPetsCall
 * @description TODO
 * @date 2021-1-26 22:21:47
 */
@Service
public class AlibabaService extends AbstractPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    private static final String CATEGORYGETLINK = "https://suggest.1688.com/bin/suggest?type=offer&encode=utf8&q=%E5%AE%A0%E7%89%A9";
    private static final String PAGE_JSON_REQUEST = "https://data.p4psearch.1688.com/data/ajax/get_premium_offer_list.json?beginpage=replacePage&asyncreq=replaceSeq&keywords=replaceKeywords&sortType=&descendOrder=&province=&city=&priceStart=&priceEnd=&dis=&cosite=&location=&trackid=&spm=a2609.11209760.j3f8podl.e5rt432e&keywordid=&provinceValue=%E6%89%80%E5%9C%A8%E5%9C%B0%E5%8C%BA&p_rs=true&pageid=681d7a2dzZGvcv&p4pid=3a8b61dde1fb42a1be4f43aba3cfe8af&callback=jsonp_1611889668607_36635&_=1611889668607";
    private static final String PRODUCT_DETAIL = "https://detail.1688.com/offer/pid.html";

    @Override
    public String getSource() {
        return Website.ALIBABA.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.ALIBABA.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        String baseCategoryLink = getCategoryBaseUrl();
        String category1 = "宠物";
        String categoryText = HttpUtil.doGET(CATEGORYGETLINK);
        int start = categoryText.indexOf("{");
        JSONObject json = JSONObject.parseObject(categoryText.substring(start));
        if (null != json && json.get("result") != null) {
            for (Object obj : json.getJSONArray("result")) {
                JSONArray arr = (JSONArray) obj;
                String category2 = arr.get(0).toString();
                category2 = category2.replaceAll("_", "")
                        .replaceAll("-", "")
                        .replaceAll("%", "");
                String categoryLink = baseCategoryLink.replaceAll("replacehere", category2);
                Category category = buildCategory(category1, category2, StringUtil.EMPTY, categoryLink);
                categories.add(category);
            }
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

        try {
            System.out.println(categoryProductUrl);
            String res = HttpUtil.doGET(categoryProductUrl);

            int start = res.indexOf("(") + 1;
            String substring = res.substring(start, res.length() - 1);
            System.out.println(substring);
            JSONObject jsonObj = (JSONObject) JSONObject.parse(substring);

            if (jsonObj.getJSONObject("data") == null
                    || jsonObj.getJSONObject("data").getJSONObject("content") == null
                    || StringUtil.isEmpty(jsonObj.getJSONObject("data").getJSONObject("content").getString("p4pAttrs"))) {
                return products;
            }
            String[] ids = jsonObj.getJSONObject("data").getJSONObject("content").getString("p4pAttrs").split(";");

            for (String pidOne : ids) {
                String productId = pidOne.split(",")[0];
                String productLink = PRODUCT_DETAIL.replaceAll("pid", productId + "");
                System.out.println(productLink);

                Document document = searchUtil.getDocument(productLink);
                Thread.sleep(5 * 1000);
                String productName = document.getElementsByClass("mod-detail-title").text();

                String imageLink = document.select("meta[property='og:image']").attr("content");

                String productPrice = StringUtil.EMPTY;
                if (StringUtil.isEmpty(productPrice)) {
                    if (null != document.select("meta[property='og:product:price']")) {
                        productPrice = document.select("meta[property='og:product:price']").attr("content");
                    }
                }
                String productSpecs = StringUtil.EMPTY;
                if (null != document.getElementsByClass("table-sku") &&
                        null != document.getElementsByClass("table-sku").select("td[class='name']")
                ) {
                    productSpecs = document.getElementsByClass("table-sku").select("td[class='name']").text();
                }
                Elements select = document.select("div[class='obj-content'] tr td");
                String brand = StringUtil.EMPTY;
                String brandText = "品牌";
                int brandIndex = -1;

                int i = 0;
                for (Element element : select) {
                    if (element.text().contains(brandText)) {
                        brandIndex = i;
                        break;
                    }
                    i++;
                }
                if (brandIndex != -1 && (brandIndex + 1) < select.size()) {
                    brand = select.get((brandIndex + 1)).text();
                }

                String productUnit = productSpecs;
                PetsProduct product = buildProduct(productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
                products.add(product);
            }


        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public String search() {
        try {
            if (runing.get(getSource()) != null) {
                return "上一次爬取还未运行结束";
            }
            runing.put(getSource(), getSource());
            List<Category> allCategory = getAllCategory();
            if (CollectionUtils.isNotEmpty(allCategory)) {
                for (Category category : allCategory) {
                    List<PetsProduct> products = new LinkedList<>();

                    Object[] pageInfo = getCategoryProductCount(category.getCategoryLink());
                    int pageSize = (int) pageInfo[0];

                    if (pageSize > 1) {
                        for (int page = 1; page <= pageSize; page++) {
                            for (int seq = 1; seq <= 6; seq++) {
                                System.out.println("begin to search the " + page + " page");
                                String pageUrl = getPageLink(page, category, null);
                                pageUrl = pageUrl.replaceAll("replaceSeq", seq + "");
                                products.addAll(getProducts(category, pageUrl));
                                Thread.sleep(10 * 1000);
                            }
                            if (page % 3 == 0) {
                                saveProduct(petsProductMapper, products);
                                products = new ArrayList();
                            }
                        }
                    }
                    saveProduct(petsProductMapper, products);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            runing.remove(getSource());
        }
        return "爬取成功";
    }

    @Override
    public String getPageLink(int page, Category category, Map<String, String> otherParams) {
        String pageLink = PAGE_JSON_REQUEST.replaceAll("replacePage", page + "")
                .replaceAll("replaceKeywords", category.getCategory2());
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
