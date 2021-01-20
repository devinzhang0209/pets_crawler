package com.zhuayinline.pets.crawler.vo;

public enum Website {

    BQ("波奇", "http://shop.boqii.com/allsort.html"),
    ECCAT("E宠", "https://cat.epet.com/"),
    ECDOG("E宠", "https://www.epet.com/"),

    ZQW("最宠网", ""),
    CMT("萌宠堂", ""),
    TWLCWYPSC("20楼宠物用品商城", ""),
    CWSC("宠物商城", ""),
    TAOBAO("淘宝", ""),
    TMALL("天猫", ""),
    ALIBABA("阿里巴巴", ""),
    JD("京东", ""),
    SUNING("苏宁", ""),
    DANGDANG("当当", "");

    Website(String websiteName, String baseCategoryUrl) {
        this.websiteName = websiteName;
        this.baseCategoryUrl = baseCategoryUrl;
    }


    private String websiteName;
    private String baseCategoryUrl;

    public String getWebsiteName() {
        return websiteName;
    }

    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    public String getBaseCategoryUrl() {
        return baseCategoryUrl;
    }

    public void setBaseCategoryUrl(String baseCategoryUrl) {
        this.baseCategoryUrl = baseCategoryUrl;
    }
}
