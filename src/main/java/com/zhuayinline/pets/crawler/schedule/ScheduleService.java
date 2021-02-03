package com.zhuayinline.pets.crawler.schedule;

import com.zhuayinline.pets.crawler.service.AbstractPetsCall;
import com.zhuayinline.pets.crawler.service.impl.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ScheduleService {

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
    private AbstractPetsCall alibabaService;
    @Resource
    private DangdangService dangdangService;
    @Resource
    private JDService jdService;
    @Resource
    private ZhiDeMaiService zhiDeMaiService;

    private static List<AbstractPetsCall> serviceList = new ArrayList();

    @PostConstruct
    public void init() {
        serviceList.add(boQiService);
        serviceList.add(ePetsService);
        serviceList.add(mctService);
        serviceList.add(twentyFloorService);
        serviceList.add(goodmaoningService);
        serviceList.add(suningService);
        serviceList.add(alibabaService);
        serviceList.add(dangdangService);
        serviceList.add(jdService);
        serviceList.add(zhiDeMaiService);

    }

    @Scheduled(cron = "0 52 20 ? * *")
    public void regularTimeExport() {
        System.out.println("定时器爬取执行");
        ExecutorService executorService = Executors.newFixedThreadPool(serviceList.size());
        for (int i = 0; i < serviceList.size(); i++) {
            final AbstractPetsCall service = serviceList.get(i);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        service.search();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }


    }
}
