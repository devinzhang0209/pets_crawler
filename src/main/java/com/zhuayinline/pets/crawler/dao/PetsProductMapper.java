package com.zhuayinline.pets.crawler.dao;

import com.zhuayinline.pets.crawler.entity.PetsProduct;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface PetsProductMapper extends Mapper<PetsProduct>, MySqlMapper<PetsProduct> {

}