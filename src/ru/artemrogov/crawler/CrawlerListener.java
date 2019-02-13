package ru.artemrogov.crawler;

public interface CrawlerListener {
    void notifyFundsChanged();

    void notifyActionSearch();

}
