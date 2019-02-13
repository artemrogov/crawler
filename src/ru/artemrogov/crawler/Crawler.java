package ru.artemrogov.crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Crawler {

    private String startUrl;

    private boolean crawling; // работает червяк или нет

    private int maxUrls;

    private boolean limit;

    private String searchString;

    private String logFile; // которая используется при записи совпадений в журнал



    public Crawler(String startUrl, int maxUrls) {

        this.startUrl = startUrl;
        this.maxUrls = maxUrls;

    }

    /**
     * Проверка сосотояния поиска
     * @return true or false
     */
    public boolean isCrawling() {
        return crawling;
    }

    /**
     * Включить или выключить поиск
     * @param crawling true or false
     */
    public void setCrawling(boolean crawling) {
        this.crawling = crawling;
    }


    /**
     * Кэш-память для списка ограничений робота.
     * Используется для списка запрещенных путей робота
     * поэтому из нее надо будет специально извлекать из файла при обращении к отдельным URL
     */
    private HashMap<String, ArrayList<String>> disallowListCache = new HashMap<>();


    /**
     * Проверка формата url-адреса
     * @param url ссылка сайта
     * @return возращает верефициорованную ссылку сайта
     */
    private URL verifyUrl(String url) {

        // Разрешить только адреса HTTP.
        if (!url.toLowerCase().startsWith("http://"))
            return null;

        // Проверить формат URL.
        URL verifiedUrl = null;

        try {

            verifiedUrl = new URL(url);

        } catch (Exception e) {

            return null;
        }

        return verifiedUrl;
    }

    /**
     * Проверить, если робот разрешает доступ к данному URL.
     * @param urlToCheck проверенный url(ссылка) страницы
     * @return boolean (логическое значение true - если, запрашиваемая страница разрешена для индексации,
     * false - не разрешенна для индексации
     */
    private boolean isRobotAllowed(URL urlToCheck) {
        String host = urlToCheck.getHost().toLowerCase();
        // Извлечь список ограничений сайта из кэш-памяти.
        ArrayList<String> disallowList =
                disallowListCache.get(host);

        // Если в кэш-памяти нет списка, загрузить его.
        if (disallowList == null) {

            disallowList = new ArrayList<>();
            try {
                URL robotsFileUrl = new URL("http://" + host + "/robots.txt");

                // Открыть файл робота заданного URL для чтения.
                BufferedReader reader = new BufferedReader(new InputStreamReader(robotsFileUrl.openStream()));

                // Прочитать файл робота, создать список запрещенных путей.
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.indexOf("Disallow:") == 0) {
                        String disallowPath =
                                line.substring("Disallow:".length());

                        /* Просмотреть список запрещенных путей и удалить
                           комментарии, если они есть. */
                        int commentIndex = disallowPath.indexOf("#");
                        if (commentIndex != - 1) {
                            disallowPath = disallowPath.substring(0, commentIndex);
                        }

                        // Удалить начальные или конечные пробелы из
                        // запрещенных путей.
                        disallowPath = disallowPath.trim();

                        // Добавить запрещенные пути в список.
                        disallowList.add(disallowPath);
                    }
                }

                // Добавить новый список в кэш-память.
                disallowListCache.put(host, disallowList);
            }
            catch (Exception e) {
                /*
                    Использовать присвоенного робота после генерации
                    исключительной ситуации при отсутствии файла робота.
                 */
                return true;
            }
        }

        /*
            Просмотр списка запрещенных путей для проверки
            нахождения в нем заданного URL.
         */

        String file = urlToCheck.getFile();
        for (int i = 0; i < disallowList.size(); i++) {
            String disallow = disallowList.get(i);
            if (file.startsWith(disallow)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Загружает страницу с заданным URL
     * он загружает Web.страницы с данного URL и
     * возвращает содержимое страницы как одну большую строку
     *
     * @param pageUrl url страницы
     * @return возвращает контент страницы
     */

    private String downloadPage(URL pageUrl) {

        try {
            // Открыть соединение по заданному URL для чтения.
            BufferedReader reader = new BufferedReader(new InputStreamReader(pageUrl.openStream()));

            // Считать в буфер.
            String line;
            StringBuffer pageBuffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {

                pageBuffer.append(line);
            }

            return pageBuffer.toString();


        } catch (Exception e) {
        }

        return null;
    }



    /**
     * Удалить сиволы www из url адреса,
     * если присутствуют в url
     * @param url адрес
     * @return url без символов wwww
     */
    private String removeWwwFromUrl(String url) {
        int index = url.indexOf("://www.");
        if (index != -1) {
            return url.substring(0, index + 3) +
                    url.substring(index + 7);
        }
        return (url);
    }



    /**
     * Произвести синтаксический анализ и возвратить ссылки
     * @param pageUrl url страницы
     * @param pageContents содержание страницы
     * @param crawledList список пройденных url
     * @param limitHost лимит обхода
     * @return Список проанализированных ссылок
     */
    private ArrayList<String> retrieveLinks(URL pageUrl, String pageContents, HashSet<String> crawledList, boolean limitHost)
    {
        // Компилировать ссылки шаблонов совпадений.
        Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]", Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(pageContents);

        // Создать список совпадающих ссылок.
        ArrayList<String> linkList = new ArrayList<>();

        while (m.find()) {
            String link = m.group(1).trim();

            // Если ссылки пустые, то пропустить ссылки
            if (link.length() < 1) {
                continue;
            }

            //Если, ссылки указывают на задданную страницу
            //то пропустить ссылки

            if (link.charAt(0) == '#') {
                continue;
            }

            /*
                Пропустить ссылки, в которых содержится аттрибут mailto:
                (они используются для почтовых отправлений)
             */

            if (link.indexOf("mailto:") != -1) {
                continue;
            }


            // Пропустить ссылки на сценарии JavaScript.
            if (link.toLowerCase().indexOf("javascript") != -1) {
                continue;
            }

            // Восстановить префикс абсолютного или относительного URL.
            if (link.indexOf("://") == -1) {

                // Обработать абсолютный URL
                if (link.charAt(0) == '/') {
                    link = "http://" + pageUrl.getHost() + link;

                    // Обработать относительный URL.
                } else {

                    String file = pageUrl.getFile();

                    if (file.indexOf('/') == -1) {

                        link = "http://" + pageUrl.getHost() + "/" + link;

                    } else {

                        String path = file.substring(0, file.lastIndexOf('/') + 1);
                        link = "http://" + pageUrl.getHost() + path + link;
                    }
                }
            }

            // Удалить привязки из ссылок.
            int index = link.indexOf('#');
            if (index != -1) {
                link = link.substring(0, index);
            }

            // Удалить начальные символы "www" из URL, если они есть.
            link = removeWwwFromUrl(link);

            // Проверить ссылки и отбросить все неправильные.
            URL verifiedLink = verifyUrl(link);

            if (verifiedLink == null) {
                continue;
            }

            /*
                Если указано, то использовать только ссылки
                для сайта с начальным URL.
            */

            if (limitHost && !pageUrl.getHost().toLowerCase().equals(verifiedLink.getHost().toLowerCase()))
            {
                continue;
            }

            // Отбросить ссылки, если они уже просмотрены.
            if (crawledList.contains(link)) {
                continue;
            }

            // Добавить ссылку в список.
            linkList.add(link);
        }
        return (linkList);
    }



    /**
     * Определяет, присутствуют ли совпадения для строки поиска
     * на данной странице
     *
     * @param pageContents содержание страницы
     * @param searchString слово которое ищется
     * @param caseSensitive учитывать регистр клавиатуры
     * @return
     */
    private boolean searchStringMatches(String pageContents, String searchString, boolean caseSensitive)
    {
        String searchContents = pageContents;

        /*
           Если учитывается регистр клавиатуры, то преобразовать
            содержимое в нижний регистр для сравнения.
        */

        if (!caseSensitive) {
            searchContents = pageContents.toLowerCase();
        }

        // Разделить строку поиска на отдельные термы.
        Pattern p = Pattern.compile("[\\s]+");

        String[] terms = p.split(searchString);

        // Проверки на совпадение каждый терм

        for (int i = 0; i < terms.length; i++) {

            if (caseSensitive) {

                if (searchContents.indexOf(terms[i]) == -1) {

                    return false;
                }
            } else {
                if (searchContents.indexOf(terms[i].toLowerCase()) == -1) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     *
     * @param crawling текущий url
     * @param crawled кол-во просмотренных url
     * @param toCrawl сколько осталось просмотреть url
     * @param maxUrls максимальное количество URL
     */

    public abstract void updateStats(String crawling, int crawled, int toCrawl, int maxUrls);

    /**
     * Произвести запись в таблицу совпадений и файл журнала
     * @param url адрес страницы
     */
    public abstract void addMatch(String url);



}
