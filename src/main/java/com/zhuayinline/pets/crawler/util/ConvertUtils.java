package com.zhuayinline.pets.crawler.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/**
 * @author sunflower
 */
@Slf4j
public class ConvertUtils {


	/**
	 * 功能描述：浅拷贝 - 转换Bean对象
	 *
	 * @param sourceObject sourceObject
	 * @param clazz Class
	 * @return sourceObject
	 * @author Elivense White
	 */
	public static <T> T convertBean(Object sourceObject, Class<T> clazz) {
		T result = null;
		if (sourceObject != null) {
			try {
				result = clazz.newInstance();
			} catch (Exception e) {
				log.error("colver error", e);
				e.printStackTrace();
			}
			assert result != null;
			BeanUtils.copyProperties(sourceObject, result);
		}
		return result;
	}
}
