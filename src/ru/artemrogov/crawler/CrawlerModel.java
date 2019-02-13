package ru.artemrogov.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class CrawlerModel extends Crawler {


    private List<CrawlerListener> listeners;


    public CrawlerModel() {

        this.listeners = new ArrayList<>();

    }

    @Override
    public void updateStats(String crawling, int crawled, int toCrawl, int maxUrls) {

    }

    @Override
    public void addMatch(String url) {

    }

    @Override
    public void crawl(String startUrl, int maxUrls, boolean limitHost, String searchString, boolean caseSensitive) {
        // Установить список поиска.
        HashSet<String> crawledList = new HashSet<>();

        LinkedHashSet<String> toCrawlList = new LinkedHashSet<>();

        // Добавить начальный URL в список поиска.
        toCrawlList.add(startUrl);

       /*
           Выполнить поиск, последовательно просматривая список поиска
       */

        while (super.isCrawling() && toCrawlList.size() > 0) {
         /*
           Проверить, не достигнуто ли максимально
           число разрешенных URL, если это значение задано.
           */

            if (maxUrls != -1) {
                if (crawledList.size() == maxUrls) {
                    break;
                }
            }

            String url = toCrawlList.iterator().next(); // Получить URL.

            // Удалить URL из списка поиска.
            toCrawlList.remove(url);

            // Преобразовать строку url в объект URL.
           // URL verifiedUrl = verifyUrl(url);

            URL verifiedUrl = null;

            try {
                verifiedUrl = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // Пропустить URL, если по списку робота к нему нет доступа.
            /*if (!isRobotAllowed(verifiedUrl)) {
                continue;
            }*/

            // Обновить панель Stats.Поиск в Web с помощью Java

           // updateStats(url, crawledList.size(), toCrawlList.size(), maxUrls);

           // System.out.println(url);

            // Добавить страницу в список поиска.
            crawledList.add(url);

            // Загрузить страницу с заданным url.
            //String pageContents = downloadPage(verifiedUrl);


            String pageContents = null;
            try {

                pageContents = super.pagelines(verifiedUrl.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
                /*
                Если страница успешно загружена, извлечь из нее
                все ссылки и затем произвести поиск совпадающих строк.
                */


            if (pageContents != null && pageContents.length() > 0) {
                // Извлечь список допустимых ссылок из страницы.

                ArrayList<String> links = retrieveLinks(verifiedUrl, pageContents, crawledList, limitHost);
                // Добавить ссылки в список поиска.
                toCrawlList.addAll(links);

                /*
                Проверить на наличие совпадающей строки, и если
                совпадение есть, то записать совпадение.
                */
                if (searchStringMatches(pageContents, searchString,
                        caseSensitive)) {
                    addMatch(url);
                }
            }

            System.out.println(url);

            // Обновить панель Stats.
           /* updateStats(url, crawledList.size(), toCrawlList.size(),
                    maxUrls);*/


        }

    }



    public void addCrawlerListener(CrawlerListener listener){
        listeners.add(listener);
    }


    public void removeCrawlerListener(CrawlerListener listener){
        listeners.remove(listener);
    }


    public void removeAllCrawlerListener(){
        listeners.clear();
    }


}
