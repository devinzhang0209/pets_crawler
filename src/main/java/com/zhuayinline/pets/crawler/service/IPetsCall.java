package com.zhuayinline.pets.crawler.service;

import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.util.DateUtil;
import com.zhuayinline.pets.crawler.util.StringUtil;
import com.zhuayinline.pets.crawler.vo.Category;
import org.apache.commons.collections.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author devin
 * @date 2020-12-8 20:17:13
 * @description interface for crawler
 */
public abstract class IPetsCall {

    private String source;
    private String categoryBaseUrl;

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


    public abstract void search() throws Exception;

    /**
     * get the product list
     *
     * @throws Exception
     */
    public void search(PetsProductMapper petsProductMapper) throws Exception {
        try {
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
                    products.addAll(getProducts(category, category.getCategoryLink()));

                    if (pageSize > 1) {
                        //for 20floor
                        Map<String, String> otherParams = new HashMap();
                        if (StringUtil.isNotEmpty(pageLink)) {
                            otherParams.put("pageLink", pageLink);
                        }

                        for (int page = 2; page <= pageSize; page++) {
                            System.out.println("begin to search the " + page + " page");
                            products.addAll(getProducts(category, getPageLink(page, category, otherParams)));
                        }
                    }
                    saveProduct(petsProductMapper, products);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        PetsProduct product = new PetsProduct();
        product.setSource(getSource());
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
        return product;
    }

}

