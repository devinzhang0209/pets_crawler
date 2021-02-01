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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Devin Zhang
 * @className ManManMaiService
 * @description TODO
 * @date 2021-1-31 22:24:46
 */
@Service
public class TMallAndTaobaoService extends AbstractPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    private static final String category1 = "宠物";
    private static final String PAGE_BASE_LINK = "http://www.b1bj.com/s.aspx?PageID=page&smallclass=&ppid=&siteid=15,10,190&price1=0&price2=0&orderby=&iszy=0&key=keyword&iswap=0&NotContains=&proid=0";

    private static final String TAOBAO_DETAIL_LINK_URL = "https://item.taobao.com/item.htm?id=";
    private static final String TMALL_DETAIL_LINK_URL = "https://detail.tmall.com/item.htm?id=";

    @Override
    public String getSource() {
        return Website.TMALLANDTAOBAO.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.TMALLANDTAOBAO.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        String res = HttpUtil.doGET(getCategoryBaseUrl());

        int begin1 = res.indexOf("(") + 1;
        String priceJsonStr = res.substring(begin1, res.length() - 1);
        JSONArray jsonObj = (JSONArray) JSONObject.parse(priceJsonStr);
        for (Object o : jsonObj) {
            String category2 = ((JSONObject) o).getString("key");
            if (StringUtil.isEmpty(category2)) {
                continue;
            }
            String categoryLink = PAGE_BASE_LINK;
            categoryLink = categoryLink.replaceAll("keyword", category2)
                    .replaceAll("page", "1");
            Category category = buildCategory(category1, category2, StringUtil.EMPTY, categoryLink);
            categories.add(category);
            if (CollectionUtils.isEmpty(((JSONObject) o).getJSONArray("tag"))) {
                continue;
            }
        }
        return categories;
    }

    @Override
    public Object[] getCategoryProductCount(String categoryProductUrl) throws Exception {
        Object[] objs = new Object[2];
        Integer page = 1;
        Document document = searchUtil.getDocument(categoryProductUrl, 1);
        try {
            Element dispage = document.getElementById("dispage");
            if (null != dispage && CollectionUtils.isNotEmpty(dispage.select("a"))) {
                Elements pages = dispage.select("a");
                for (Element element : pages) {
                    if (element.text().contains("末页")) {
                        String lastPage = element.attr("href");
                        int begin = lastPage.indexOf("PageID=") + 7;
                        int end = lastPage.indexOf("&smallclass=");
                        String totalPage = lastPage.substring(begin, end);
                        page = Integer.parseInt(totalPage);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        objs[0] = page;
        return objs;
    }

    @Override
    public List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception {
        List<PetsProduct> products = new LinkedList<>();

        Document document = searchUtil.getDocument(categoryProductUrl, 1);
        if (document.getElementById("listpro") != null
                && CollectionUtils.isNotEmpty(document.getElementById("listpro").children())) {
            Elements productList = document.getElementById("listpro").children();
            for (Element element : productList) {
                PetsProduct product = null;
                try {
                    String currentLink = element.select("div[class=divpic] a").attr("href");
                    if (StringUtil.isEmpty(currentLink)) {
                        continue;
                    }
                    String imageLink = element.select("div[class=divpic] a img").attr("src");

                    System.out.println("currentLink:" + currentLink);
                    int begin = currentLink.indexOf("itemid=") + ("itemid=").length();
                    String productId = currentLink.substring(begin);

                    String source = element.getElementsByClass("divlogo").text();
                    String pageLink;
                    String sourceName;
                    if (source.contains("天猫")) {
                        pageLink = TMALL_DETAIL_LINK_URL;
                        sourceName = Website.TMALL.getWebsiteName();
                    } else {
                        pageLink = TAOBAO_DETAIL_LINK_URL;
                        sourceName = Website.TAOBAO.getWebsiteName();
                    }

                    String productLink = pageLink + productId;
                    String productName = element.select("div[class=divtitle] a").text();

                    String productPrice = element.getElementsByClass("divprice").text();
                    productPrice = productPrice.replaceAll("¥", "")
                            .replaceAll("￥", "");

                    String brand = source.replaceAll("\\(", "")
                            .replaceAll("\\) (第三方 )", "")
                            .replaceAll("\\(", "")
                            .replaceAll("\\(", "")
                            .replaceAll("\\)", "").replaceAll("(第三方 )", "")
                            .replaceAll("淘宝", "")
                            .replaceAll("天猫商城", "")
                            .replaceAll("天猫超市", "");
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
        String pageLink = firstPageUrl.replaceAll("PageID=1", "PageID=" + page);
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
