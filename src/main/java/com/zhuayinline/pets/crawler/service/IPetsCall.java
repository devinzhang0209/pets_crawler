package com.zhuayinline.pets.crawler.service;

import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.util.StringUtil;
import com.zhuayinline.pets.crawler.vo.Category;

import java.util.List;

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
    public abstract int getCategoryProductCount(String categoryProductUrl) throws Exception;


    public abstract List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception;

    /**
     * get the product list
     *
     * @throws Exception
     */
    public abstract void search() throws Exception;

    public abstract String getSource() ;


    public abstract String getCategoryBaseUrl() ;

}

