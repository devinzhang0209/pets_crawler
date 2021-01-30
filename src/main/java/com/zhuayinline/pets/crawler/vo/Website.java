package com.zhuayinline.pets.crawler.vo;

public enum Website {

    BQ("波奇", "https://www.boqii.com/", "http://shop.boqii.com/allsort.html"),
    ECCAT("E宠", "https://www.epet.com/", "https://cat.epet.com/"),
    ECDOG("E宠", "https://www.epet.com/", "https://www.epet.com/"),
    ZQW("最宠网", "", ""),
    MCT("萌宠堂", "https://www.mengchongtang.com/", "https://www.mengchongtang.com/catalog.php"),
    TWLCWYPSC("20楼宠物用品商城", "http://20floor.com/", "http://20floor.com/"),
    CWSC("宠物商城", "https://www.goodmaoning.com/", "https://www.goodmaoning.com/brand_index.html"),
    TAOBAO("淘宝", "", ""),
    TMALL("天猫", "https://www.tmall.com/", "https://www.tmall.com/"),
    ALIBABA("阿里巴巴", "https://1688.com", "https://p4psearch.1688.com/p4p114/p4psearch/offer.htm?keywords=replacehere&cosite=&location=&trackid=&spm=a2609.11209760.j3f8podl.e5rt432e&keywordid="),
    JD("京东", "https://www.jd.com/", "https://www.jd.com/allSort.aspx"),
    SUNING("苏宁", "https://suning.com/", "https://list.suning.com/"),
    DANGDANG("当当", "http://dangdang.com/", "http://category.dangdang.com/");

    Website(String websiteName, String websiteUrl, String baseCategoryUrl) {
        this.websiteName = websiteName;
        this.websiteUrl = websiteUrl;
        this.baseCategoryUrl = baseCategoryUrl;
    }


    private String websiteName;
    private String websiteUrl;
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

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
}
