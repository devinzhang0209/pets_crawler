package com.zhuayinline.pets.crawler.controller;

import com.zhuayinline.pets.crawler.service.impl.BoQiService;
import com.zhuayinline.pets.crawler.service.impl.EPetsService;
import com.zhuayinline.pets.crawler.vo.Result;
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

    @GetMapping("/boqi")
    public String callBoQi() {
        boQiService.search();
        return Result.succResult(null);
    }

    @GetMapping("/ec")
    public String callEC() throws Exception {
        ePetsService.search();
        return Result.succResult(null);
    }

}
