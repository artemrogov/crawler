package ru.artemrogov.crawler;

import java.net.MalformedURLException;

public class RealeaseCrawler {

    public static void main(String[] args) throws MalformedURLException {

        Crawler crawler = new CrawlerModel();

        ///URL urlPage = new URL("http://artem-rogov.ru");

        //String[] results = crawler.pagelines("http://artem-rogov.ru"); success

       // String resultYandex = crawler.pagelines("https://yandex.ru"); //success https

       /// System.out.println(resultYandex); // строка со скаченной страницой не обработанная

        //boolean res = crawler.searchStringMatches(resultYandex,"погода", true); // искать строку на странице, учитывать регистр клавиатуры
        //System.out.print("Есть ли совпадения " + res); // выводит результат найденно ли совпадение

        crawler.setCrawling(true);

        crawler.crawl("http://www.inetkomp.ru",1500,false,"температура",false);

        //crawler.crawlSearchString("String","максимальное кол-во сайтов","установить лимит")

    }
}
