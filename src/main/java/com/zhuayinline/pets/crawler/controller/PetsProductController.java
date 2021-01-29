package com.zhuayinline.pets.crawler.controller;

import com.zhuayinline.pets.crawler.service.IPetsCall;
import com.zhuayinline.pets.crawler.service.impl.*;
import com.zhuayinline.pets.crawler.util.StringUtil;
import com.zhuayinline.pets.crawler.vo.Result;
import com.zhuayinline.pets.crawler.vo.Website;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Devin Zhang
 * @className UserCertController
 * @description TODO
 * @date 2019/11/15 14:25
 */
@Api(description = "boqi")
@RestController
@RequestMapping("/api/pets")
public class PetsProductController {

    @Resource
    private BoQiService boQiService;
    @Resource
    private EPetsService ePetsService;
    @Resource
    private MCTService mctService;
    @Resource
    private TwentyFloorService twentyFloorService;
    @Resource
    private GoodmaoningService goodmaoningService;
    @Resource
    private SuningService suningService;
    @Resource
    private AlibabaService alibabaService;

    @GetMapping("/begin")
    public String callBoQi(String action) throws Exception {
        IPetsCall service = null;
        if (StringUtil.isEmpty(action)) {
            return Result.failResult("action can't be null");
        }
        if (action.equalsIgnoreCase(Website.BQ.name())) {
            service = boQiService;
        } else if (action.equalsIgnoreCase(Website.ECDOG.name())
                || action.equalsIgnoreCase(Website.ECCAT.name())) {
            service = ePetsService;
        } else if (action.equalsIgnoreCase(Website.MCT.name())) {
            service = mctService;
        } else if (action.equalsIgnoreCase(Website.TWLCWYPSC.name())) {
            service = twentyFloorService;
        } else if (action.equalsIgnoreCase(Website.CWSC.name())) {
            service = goodmaoningService;
        } else if (action.equalsIgnoreCase(Website.SUNING.name())) {
            service = suningService;
        } else if (action.equalsIgnoreCase(Website.ALIBABA.name())) {
            service = alibabaService;
        }
        if (null == service) {
            return Result.failResult("action not found");
        }
        service.search();
        return Result.succResult(null);
    }


}
