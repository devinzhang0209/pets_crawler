package com.zhuayinline.pets.crawler.service;

import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.util.DateUtil;
import com.zhuayinline.pets.crawler.util.StringUtil;
import com.zhuayinline.pets.crawler.vo.Category;
import com.zhuayinline.pets.crawler.vo.Website;
import org.apache.commons.collections.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author devin
 * @date 2020-12-8 20:17:13
 * @description interface for crawler
 */
public abstract class AbstractPetsCall {

    public static final String HTTPS = "https:";
    public static Map<String, String> runing = new HashMap();

    /**
     * get all category
     *
     * @return map
     */
    public abstract List<Category> getAllCategory() throws Exception;

    /**
     * get category product count
     *
     * @return int
     */
    public abstract Object[] getCategoryProductCount(String categoryProductUrl) throws Exception;


    public abstract List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception;


    public abstract String search() throws Exception;

    /**
     * get the product list
     *
     * @throws Exception
     */
    public String search(PetsProductMapper petsProductMapper) throws Exception {
        try {
            if (runing.get(getCategoryBaseUrl()) != null) {
                return "上一次爬取还未运行结束";
            }
            runing.put(getCategoryBaseUrl(), getCategoryBaseUrl());
            List<Category> allCategory = getAllCategory();
            if (CollectionUtils.isNotEmpty(allCategory)) {
                for (Category category : allCategory) {
                    List<PetsProduct> products = new LinkedList<>();

                    Object[] pageInfo = getCategoryProductCount(category.getCategoryLink());
                    int pageSize = (int) pageInfo[0];
                    String pageLink = StringUtil.EMPTY;
                    if (pageInfo.length == 2) {
                        pageLink = (String) pageInfo[1];
                    }
                    System.out.println("total page:" + pageSize);
                    System.out.println("begin to search the first page...");
                    //第一页
                    try {
                        products.addAll(getProducts(category, category.getCategoryLink()));
                        if (getSource().equals(Website.TMALLANDTAOBAO.getWebsiteName())) {
                            Thread.sleep(35 * 1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    int flag = 0;
                    if (pageSize > 1) {
                        //for 20floor
                        Map<String, String> otherParams = new HashMap();
                        if (StringUtil.isNotEmpty(pageLink)) {
                            otherParams.put("pageLink", pageLink);
                        }

                        for (int page = 2; page <= pageSize; page++) {
                            try {
                                System.out.println("begin to search the " + page + " page");
                                List<PetsProduct> list = getProducts(category, getPageLink(page, category, otherParams));
                                if (getSource().equals(Website.ZHIDEMAI.getWebsiteName())) {
                                    if (CollectionUtils.isEmpty(list)) {
                                        flag++;
                                    }
                                }
                                if (flag == 5) {
                                    break;
                                }
                                products.addAll(list);
                                if (getSource().equals(Website.TMALLANDTAOBAO.getWebsiteName())) {
                                    Thread.sleep(35 * 1000);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Thread.sleep(10 * 1000);
                            }
                            //submit
                            if (page % 3 == 0) {
                                saveProduct(petsProductMapper, products);
                                products = new ArrayList();
                            }
                        }
                    }
                    saveProduct(petsProductMapper, products);
                }
            }
            runing.remove(getCategoryBaseUrl());
        } catch (Exception e) {
            e.printStackTrace();
            runing.remove(getCategoryBaseUrl());
            return "爬取失败";
        }
        return "爬取成功";
    }

    public abstract String getPageLink(int page, Category category, Map<String, String> otherParams);

    public abstract String getSource();


    public abstract String getCategoryBaseUrl();

    public void saveProduct(PetsProductMapper petsProductMapper, List<PetsProduct> products) {
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

    public Category buildCategory(String category1, String category2, String category3, String categoryLink) {
        Category category = new Category();
        category.setCategory1(category1);
        category.setCategory2(category2);
        category.setCategory3(category3);
        category.setCategoryLink(categoryLink);
        return category;
    }

    public PetsProduct buildProduct(String productId, Category category, String productName, String brand, String productUnit, String imageLink, String productLink, String productPrice, String productSpecs) {
        return buildProduct(getSource(), productId, category, productName, brand, productUnit, imageLink, productLink, productPrice, productSpecs);
    }

    public PetsProduct buildProduct(String source, String productId, Category category, String productName, String brand, String productUnit, String imageLink, String productLink, String productPrice, String productSpecs) {
        PetsProduct product = new PetsProduct();
        product.setSource(source);
        product.setProductId(productId);
        product.setCategory1(category.getCategory1());
        product.setCategory2(category.getCategory2());
        product.setCategory3(category.getCategory3());
        product.setProductName(productName);
        product.setProductBrand(brand);
        product.setProductUnit(productUnit);
        product.setProductImageLink(imageLink);
        product.setProductLink(productLink);
        System.out.println("productPrice:" + productPrice);
        product.setProductPrice(new BigDecimal(StringUtil.isEmpty(productPrice) ? "0" : productPrice));
        product.setProductSpecs(productSpecs);
        product.setCreatedTime(DateUtil.getNow());
        product.setLastUpdatedTime(DateUtil.getNow());
        System.out.println(product);
        return product;
    }


}

