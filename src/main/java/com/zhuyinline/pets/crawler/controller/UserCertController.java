package com.zhuyinline.pets.crawler.controller;

import com.zhuyinline.pets.crawler.entity.UserCert;
import com.zhuyinline.pets.crawler.service.impl.UserCertService;
import com.zhuyinline.pets.crawler.util.DateUtil;
import com.zhuyinline.pets.crawler.util.StringUtil;
import com.zhuyinline.pets.crawler.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author Devin Zhang
 * @className UserCertController
 * @description TODO
 * @date 2019/11/15 14:25
 */
@Api(description = "证书管理")
@Controller
@RequestMapping("/api/userCert")
public class UserCertController {

    @Resource
    private UserCertService userCertService;



}
