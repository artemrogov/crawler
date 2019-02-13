package ru.artemrogov.crawler;

public class CrawlerController {

    private CrawlerModel crawlerModel; // модель Crawler
    private CrawlerView crawlerView;

    public CrawlerController(CrawlerModel crawlerRes, CrawlerView view) {
        this.crawlerModel = crawlerRes;
        this.crawlerView = view;

    }

    public static void main(String[] args) {

        Crawler crawlerModelRes = new CrawlerModel();

        CrawlerView crawlerViewRes = new CrawlerView();

        CrawlerController crawlerController = new CrawlerController((CrawlerModel) crawlerModelRes,crawlerViewRes);

    }

    public void notifyCrawl(String startUrl, int maxUrls, boolean limitHost, String searchString, boolean caseSensitive){
        crawlerModel.crawl(startUrl,maxUrls,limitHost,searchString,caseSensitive);
    }
}
