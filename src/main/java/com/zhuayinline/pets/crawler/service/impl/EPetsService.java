package com.zhuayinline.pets.crawler.service.impl;

import com.zhuayinline.pets.crawler.entity.PetsProduct;
import com.zhuayinline.pets.crawler.service.IPetsCall;
import com.zhuayinline.pets.crawler.vo.Category;

import java.util.List;

public class EPetsService extends IPetsCall {
    @Override
    public List<Category> getAllCategory() throws Exception {
        return null;
    }

    @Override
    public int getCategoryProductCount(String categoryProductUrl) throws Exception {
        return 0;
    }

    @Override
    public List<PetsProduct> getProducts(Category category, String categoryProductUrl) throws Exception {
        return null;
    }

    @Override
    public void search() throws Exception {

    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public String getCategoryBaseUrl() {
        return null;
    }
}
