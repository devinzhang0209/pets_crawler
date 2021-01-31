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
 * @className Taobao servvice
 * @description TODO
 * @date 2021-1-30 13:08:01
 */
@Service
public class TaobaoService extends AbstractPetsCall {

    @Autowired
    private SearchUtil searchUtil;
    @Autowired
    private PetsProductMapper petsProductMapper;

    private static final String BASEPAGELINK = "https://list.tmall.com/search_product.htm";

    @Override
    public String getSource() {
        return Website.TAOBAO.getWebsiteName();
    }

    @Override
    public String getCategoryBaseUrl() {
        return Website.TAOBAO.getBaseCategoryUrl();
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        List<Category> categories = new LinkedList<>();
        Document document = searchUtil.getDocument(getCategoryBaseUrl());
        Elements rootCategoryElement = document.getElementsByClass("service-panel full");

        final String keywords = "宠物";

        for (int index = 0; index < rootCategoryElement.size(); index++) {

            Element categoryDoc = rootCategoryElement.get(index);
            String category1 = categoryDoc.select("h5").text();
            if (StringUtil.isNotEmpty(category1) && category1.contains(keywords)) {
                String category3 = StringUtil.EMPTY;

                Elements secondDocs = categoryDoc.getElementsByClass("p a");
                for (Element secondDoc : secondDocs) {
                    String link = secondDoc.attr("href");
                    String category2 = secondDoc.text();
                    Category category = buildCategory(category1, category2, category3, link);
                    categories.add(category);
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
            String totalPage = document.getElementsByClass("total").text();
            totalPage = totalPage.replaceAll("共", "")
                    .replaceAll("页", "")
                    .replaceAll("，", "")
                    .replaceAll(",", "")
                    .trim();
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
        if (CollectionUtils.isNotEmpty(document.getElementsByClass("item J_MouserOnverReq"))) {
            Elements productList = document.getElementsByClass("view grid-nosku");
            for (Element element : productList) {
                String productId = element.attr("data-id");
                String productLink = HTTPS + element.select("div[class='pic'] a").attr("href");
                String imageLink = HTTPS + element.select("div[class='pic'] a img").attr("src");
                String companyName = element.getElementsByClass("shopname").text();

                String productPrice = StringUtil.EMPTY;
                if (element.getElementsByClass("price g_price g_price-highlight") != null) {
                    String price_n = element.getElementsByClass("price g_price g_price-highlight").text();
                    productPrice = price_n.replaceAll("¥", "").replaceAll("￥", "");
                }
                if (StringUtil.isNotEmpty(productLink)) {
                    Document productDocument = searchUtil.getDocument(productLink);
                    if (productDocument == null) {
                        continue;
                    }
                    String productName = productDocument.getElementsByClass("tb-detail-hd").text();

                    String brand = StringUtil.EMPTY;
                    if (null != productDocument.getElementById("J_BrandAttr")
                            && null != productDocument.getElementById("J_BrandAttr").select("b")) {
                        brand = productDocument.getElementById("J_BrandAttr").select("b").text();
                    }

                    String productSpecs = StringUtil.EMPTY;
                    if (StringUtil.isEmpty(productSpecs) && CollectionUtils.isNotEmpty(productDocument.getElementsByClass("tb-selected"))) {
                        productSpecs = productDocument.getElementsByClass("tb-selected").text();
                    }
                    if (CollectionUtils.isNotEmpty(productDocument.getElementsByClass("tm-item-weight"))
                            && CollectionUtils.isNotEmpty(productDocument.getElementsByClass("tm-item-weight").get(0).select("dd em"))
                    ) {
                        productSpecs = productDocument.getElementsByClass("tm-item-weight").get(0).select("dd em").text();
                    }


                    String brandKeyword = "品牌";
                    String model = "毛重";
                    String model2 = "规格";
                    String model3 = "适用";
                    String model4 = "分类";
                    String model5 = "含量";
                    String model6 = "重量";

                    if (productDocument.getElementById("J_AttrUL") != null) {
                        Elements fontDoc = productDocument.getElementById("J_AttrUL").children();
                        for (int i = 0; i < fontDoc.size(); i++) {
                            String text = fontDoc.get(i).text();
                            if (null == text) {
                                continue;
                            }
                            String value = fontDoc.get(i).attr("title");

                            if (StringUtil.isEmpty(productSpecs) &&
                                    (text.contains(model)
                                            || text.contains(model2)
                                            || text.contains(model3)
                                            || text.contains(model4)
                                            || text.contains(model5)
                                            || text.contains(model6))
                            ) {
                                productSpecs = value;
                            }
                            if (StringUtil.isEmpty(brand) && text.contains(brandKeyword)) {
                                brand = value;
                            }
                        }
                    }
                    String productUnit = productSpecs;
                    if (StringUtil.isEmpty(brand)) {
                        brand = companyName;
                    }
                    PetsProduct product = buildProduct(productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
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
        final int len = 44;
        int s = (page - 1) * len;
        String pageLink = category.getCategoryLink();
        pageLink = pageLink + "&bcoffset=1&ntoffset=1&p4ppushleft=2%2C48&s=" + s;
        System.out.println(String.format("pageLink:%s", pageLink));
        return pageLink;
    }
}
