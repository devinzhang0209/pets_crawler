package com.zhuayinline.pets.crawler.service;

import com.zhuayinline.pets.crawler.dao.PetsProductMapper;
import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.vo.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author devin
 * @date 2020-12-8 20:17:13
 * @description interface for crawler
 */
public interface IPetsCall {


    /**
     * get all category
     *
     * @return map
     */
    List<Category> getAllCategory() throws Exception;

    /**
     * get category product count
     *
     * @return int
     */
    int getCategoryProductCount(String categoryProductUrl) throws Exception;


    List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception;

    /**
     * get the product list
     *
     * @throws Exception
     */
    void search() throws Exception;


}
