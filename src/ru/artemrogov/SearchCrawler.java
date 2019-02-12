package ru.artemrogov;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Поисковый паук: Search Crawler
 */
public class SearchCrawler extends JFrame
{


    /**
     * Максимальное количество раскрывающихся значений URL
     */

    private static final String[] MAX_URLS = {"50", "100", "500", "1000"};


    /**
     * Кэш-память для списка ограничений робота.
     */

    private HashMap<String, ArrayList<String>> disallowListCache = new HashMap<>();

    /**
     * Элементы управления графического интерфейса панели Search
     */
    private JTextField startTextField;
    private JComboBox<String> maxComboBox;
    private JCheckBox limitCheckBox;
    private JTextField logTextField;
    private JTextField searchTextField;
    private JCheckBox caseCheckBox;
    private JButton searchButton;

    /**
     * Элементы управления графического интерфейса панели Stats.
     */
    private JLabel crawlingLabel2;
    private JLabel crawledLabel2;
    private JLabel toCrawlLabel2;
    private JProgressBar progressBar;
    private JLabel matchesLabel2;

    // Список соответствий.
    private JTable table;
    // Флаг отображения состояния поиска.
    private boolean crawling;
    // Файл журнала для текстового вывода.
    private PrintWriter logFileWriter;
    // Конструктор для поискового червя.


    public SearchCrawler()
    {
        // Установка заголовка приложения.
        setTitle("Search Crawler v1.0");
        // Установка размеров окна.
        setSize(1200, 800);



        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });


        // Установить меню “файл”.
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        // Помощь - О программе

        JMenu helpMenu = new JMenu("Помошь");


        fileMenu.setMnemonic(KeyEvent.VK_F);
        helpMenu.setMnemonic(KeyEvent.VK_F1);

        JMenuItem fileExitMenuItem = new JMenuItem("Выход", KeyEvent.VK_X);
        JMenuItem aboutDialogMenuItem = new JMenuItem("О программе");


        fileExitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionExit();
            }
        });

        aboutDialogMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aboutProgramDialog();
            }
        });

        fileMenu.add(fileExitMenuItem);
        helpMenu.add(aboutDialogMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // Установить панель поиска.
        JPanel searchPanel = new JPanel();
        GridBagConstraints constraints;
        GridBagLayout layout = new GridBagLayout();
        searchPanel.setLayout(layout);
        JLabel startLabel = new JLabel("Начальный URL:");

        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(startLabel, constraints);
        searchPanel.add(startLabel);
        startTextField = new JTextField();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);


        layout.setConstraints(startTextField, constraints);
        searchPanel.add(startTextField);

        JLabel maxLabel = new JLabel("Количество URL для сканирования:");

        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(maxLabel, constraints);

        searchPanel.add(maxLabel);

        maxComboBox = new JComboBox<>(MAX_URLS);
        maxComboBox.setEditable(true);

        constraints = new GridBagConstraints();

        constraints.insets = new Insets(5, 5, 0, 0);

        layout.setConstraints(maxComboBox, constraints);
        searchPanel.add(maxComboBox);


        limitCheckBox = new JCheckBox("Ограничить сканирование до начального URL сайта");

        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;

        constraints.insets = new Insets(0, 10, 0, 0);

        layout.setConstraints(limitCheckBox, constraints);
        searchPanel.add(limitCheckBox);
        JLabel blankLabel = new JLabel();
        constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(blankLabel, constraints);
        searchPanel.add(blankLabel);

        JLabel logLabel = new JLabel("Файл журнала соответствий:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(logLabel, constraints);
        searchPanel.add(logLabel);


        String file = System.getProperty("user.dir") +
                        System.getProperty("file.separator") +
                        "crawler.log";

        logTextField = new JTextField(file);
        constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(logTextField, constraints);
        searchPanel.add(logTextField);

        JLabel searchLabel = new JLabel("Строка поиска:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(searchLabel, constraints);
        searchPanel.add(searchLabel);
        searchTextField = new JTextField();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.gridwidth= 2;

        constraints.weightx = 1.0d;

        layout.setConstraints(searchTextField, constraints);
        searchPanel.add(searchTextField);

        caseCheckBox = new JCheckBox("учитывать регистр символов");

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 0, 5);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(caseCheckBox, constraints);
        searchPanel.add(caseCheckBox);

        searchButton = new JButton("Искать");

        /**
         * Если нажали на кнопку искать,
         */
        searchButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionSearch();
            }

        });

        constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 5, 5);
        layout.setConstraints(searchButton, constraints);
        searchPanel.add(searchButton);
        JSeparator separator = new JSeparator();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 5, 5);
        layout.setConstraints(separator, constraints);
        searchPanel.add(separator);

        JLabel crawlingLabel1 = new JLabel("Обход:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(crawlingLabel1, constraints);
        searchPanel.add(crawlingLabel1);
        crawlingLabel2 = new JLabel();

        crawlingLabel2.setFont(crawlingLabel2.getFont().deriveFont(Font.PLAIN));
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(crawlingLabel2, constraints);
        searchPanel.add(crawlingLabel2);

        JLabel crawledLabel1 = new JLabel("Просканированные URL:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(crawledLabel1, constraints);

        searchPanel.add(crawledLabel1);
        crawledLabel2 = new JLabel();

        crawledLabel2.setFont(crawledLabel2.getFont().deriveFont(Font.PLAIN));

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);

        layout.setConstraints(crawledLabel2, constraints);
        searchPanel.add(crawledLabel2);

        JLabel toCrawlLabel1 = new JLabel("URL для сканирования:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(toCrawlLabel1, constraints);

        searchPanel.add(toCrawlLabel1);
        toCrawlLabel2 = new JLabel();

        toCrawlLabel2.setFont(toCrawlLabel2.getFont().deriveFont(Font.PLAIN));

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(toCrawlLabel2, constraints);
        searchPanel.add(toCrawlLabel2);

        JLabel progressLabel = new JLabel("Прогресс сканирования");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(progressLabel, constraints);
        searchPanel.add(progressLabel);
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setStringPainted(true);
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(progressBar, constraints);
        searchPanel.add(progressBar);


        JLabel matchesLabel1 = new JLabel("Поиск совпадений:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 10, 0);
        layout.setConstraints(matchesLabel1, constraints);
        searchPanel.add(matchesLabel1);
        matchesLabel2 = new JLabel();

        matchesLabel2.setFont(matchesLabel2.getFont().deriveFont(Font.PLAIN));
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 10, 5);
        layout.setConstraints(matchesLabel2, constraints);
        searchPanel.add(matchesLabel2);


        // Установить таблицу совпадений.
        table = new JTable(new DefaultTableModel(new Object[][]{},
                        new String[]{"URL"}) {
                    public boolean isCellEditable(int row, int column)
                    {
                        return false;
                    }
                });

        // Установить панель совпадений.
        JPanel matchesPanel = new JPanel();

        matchesPanel.setBorder(BorderFactory.createTitledBorder("Соответствуют:"));

        matchesPanel.setLayout(new BorderLayout());

        matchesPanel.add(new JScrollPane(table), BorderLayout.CENTER);


        // Отобразить панели на дисплее.
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(searchPanel, BorderLayout.NORTH);
        getContentPane().add(matchesPanel, BorderLayout.CENTER);


    }

    /**
     * Вывод диалогового окна "О программе"
     */
    private void aboutProgramDialog(){

        JOptionPane.showMessageDialog(this, "Search Crawler v1.0 \n Автор: Артём Рогов \n " +
                "email: artemrogov281@gmail.com" +
                "\n сайт: http://artem-rogov.ru","О программе",JOptionPane.INFORMATION_MESSAGE);

    }

    /**
     * Выход из программы
     */
    private void actionExit() {
        System.exit(0);
    }

    /**
     * Обрабатывает щелчок на кнопке Искать
     */
    private void actionSearch() {

        // Если произведен щелчок на кнопке stop, сбросить флаг.
        if (crawling) {
            crawling = false;
            return;
        }
        ArrayList<String> errorList = new ArrayList<String>();


        // Проверить ввод начального адреса (URL).
        String startUrl = startTextField.getText().trim();

        if (startUrl.length() < 1) {
            errorList.add("Отсутствует начальный URL.");
        }

         // Проверить начальный URL.
        else if (verifyUrl(startUrl) == null) {
            errorList.add("Неверный начальный URL.");
        }

        /* Проверить, что введено значение для максимально допустимого
           количества адресов и что это число. */

        int maxUrls = 0;
        String max = ((String) maxComboBox.getSelectedItem()).trim();

        if (max.length() > 0) {
            try {
                maxUrls = Integer.parseInt(max);

            } catch (NumberFormatException e) {
            }

            if (maxUrls < 1) {
                errorList.add("Неверное значение Max URLs.");
            }

        }
        // Проверить, что файл с журналом совпадений существует.
        String logFile = logTextField.getText().trim();
        if (logFile.length() < 1) {
            errorList.add("Missing Matches Log File.");
        }

        // Проверить, что введена стока для поиска.
        String searchString = searchTextField.getText().trim();

        if (searchString.length() < 1) {
            errorList.add("Отсутствует строка поиска");
        }

        // Показать ошибки, если они есть, и возврат.
        if (errorList.size() > 0) {

            StringBuffer message = new StringBuffer();

            // Объединить ошибки в одно сообщение.
            for (int i = 0; i < errorList.size(); i++) {
                message.append(errorList.get(i));
                if (i + 1 < errorList.size()) {
                    message.append("\n");
                }
            }

            showError(message.toString());
            return;
        }

        // Удалить символы "www" из начального URL, если они есть.
        startUrl = removeWwwFromUrl(startUrl);


        // Запустить поискового червя.
        search(logFile, startUrl, maxUrls, searchString);

    }

    /**
     *
     * @param logFile файл с результатами поиска
     * @param startUrl сканируемый сайт
     * @param maxUrls кол-во сканируемых url
     * @param searchString строка поиска
     */
    private void search(final String logFile, final String startUrl, final int maxUrls, final String searchString)
    {
        // Начать поиск в новом потоке.
        Thread thread = new Thread(new Runnable() {
            public void run() {
                // Отобразить песочные часы на время работы поискового червя.
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Заблокировать элементы управления поиска.
                startTextField.setEnabled(false);

                maxComboBox.setEnabled(false);

                limitCheckBox.setEnabled(false);

                logTextField.setEnabled(false);

                searchTextField.setEnabled(false);

                caseCheckBox.setEnabled(false);

                // Переключить кнопку поиска в состояние "Stop."
                searchButton.setText("Остановить");


                // Переустановить панель Stats.
                table.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"URL"}) {
                    public boolean isCellEditable(int row, int column)
                    {
                        return false;
                    }
                });

                updateStats(startUrl, 0, 0, maxUrls);

                // Открыть журнал совпадений.
                try {
                    logFileWriter = new PrintWriter(new FileWriter(logFile));
                } catch (Exception e) {
                    showError("Невозможно открыть файл журнала совпадений");
                    return;
                }

                // Установить флаг поиска.
                crawling = true;

                // Выполнять реальный поиск.
                crawl(startUrl, maxUrls, limitCheckBox.isSelected(), searchString, caseCheckBox.isSelected());

                // Сбросить флаг поиска.
                crawling = false;

                // Закрыть журнал совпадений.
                try {
                    logFileWriter.close();
                } catch (Exception e) {
                    showError("Невозможно закрыть файл журнала совпадений.");
                }

                // Отметить окончание поиска.
                crawlingLabel2.setText("Готово");


                // Разблокировать элементы контроля поиска.
                startTextField.setEnabled(true);
                maxComboBox.setEnabled(true);
                limitCheckBox.setEnabled(true);
                logTextField.setEnabled(true);
                searchTextField.setEnabled(true);
                caseCheckBox.setEnabled(true);


                // Переключить кнопку поиска в состояние "Search."
                searchButton.setText("Искать");

                // Возвратить курсор по умолчанию.
                setCursor(Cursor.getDefaultCursor());

                // Отобразить сообщение, если строка не найдена.
                if (table.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(SearchCrawler.this,
                            "Строка поиска не найдена. Пожалуйста, попробуйте сформировать другой запрос.",
                            "Строка поиска не найдена",
                            JOptionPane.WARNING_MESSAGE);
                }

            }
        });


        thread.start();
    }




    /**
     * Отображает диалоговое окно с сообщением об ошибке
     * @param message Собщение об ошибке
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка",
                JOptionPane.ERROR_MESSAGE);
    }



    /**
     * Обновляет панель stats
     * @param crawling
     * @param crawled
     * @param toCrawl
     * @param maxUrls
     */
    private void updateStats(String crawling, int crawled, int toCrawl, int maxUrls)
    {
        crawlingLabel2.setText(crawling);

        crawledLabel2.setText("" + crawled);
        toCrawlLabel2.setText("" + toCrawl);

        // Обновить индикатор выполнения.
        if (maxUrls == -1) {
            progressBar.setMaximum(crawled + toCrawl);
        } else {
            progressBar.setMaximum(maxUrls);
        }

        progressBar.setValue(crawled);
        matchesLabel2.setText("" + table.getRowCount());
    }




    /**
     * Добавить совпадение в таблицу совпадений и в журнал совпадений
     * @param url адрес сайта
     */
    private void addMatch(String url) {

        // Добавить URL в таблицу совпадений.
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.addRow(new Object[]{url});

        // Добавить URL в журнал совпадений.
        try {
            logFileWriter.println(url);
        } catch (Exception e) {
            showError("Невозможно войти добавить в журнал совпадений!");
        }
    }


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

    //

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
            disallowList = new ArrayList<String>();
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

    // Загрузить страницу с заданным URL.

    /**
     * Загружает страницу с заданным URL
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
     * Выполнить просмотр, производя поиск для заданной строки.
     * @param startUrl заданный url
     * @param maxUrls
     * @param limitHost установить лимит на прохождение
     * @param searchString строка, которую нужно найти
     * @param caseSensitive учитывать регистр клавиатуры
     */

    public void crawl(String startUrl, int maxUrls, boolean limitHost, String searchString, boolean caseSensitive)
    {
       // Установить список поиска.
        HashSet<String> crawledList = new HashSet<>();
        LinkedHashSet<String> toCrawlList = new LinkedHashSet<>();

        // Добавить начальный URL в список поиска.
        toCrawlList.add(startUrl);

       /*
           Выполнить поиск, последовательно просматривая список поиска
       */

        while (crawling && toCrawlList.size() > 0)
        {
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
            URL verifiedUrl = verifyUrl(url);

            // Пропустить URL, если по списку робота к нему нет доступа.
            if (!isRobotAllowed(verifiedUrl)) {
                continue;
            }

            // Обновить панель Stats.Поиск в Web с помощью Java

            updateStats(url, crawledList.size(), toCrawlList.size(),
                    maxUrls);

            // Добавить страницу в список поиска.
            crawledList.add(url);

            // Загрузить страницу с заданным url.
            String pageContents = downloadPage(verifiedUrl);

                /*
                Если страница успешно загружена, извлечь из нее
                все ссылки и затем произвести поиск совпадающих строк.
                */


            if (pageContents != null && pageContents.length() > 0)
            {
              // Извлечь список допустимых ссылок из страницы.
                ArrayList<String> links =
                        retrieveLinks(verifiedUrl, pageContents, crawledList,
                                limitHost);
                // Добавить ссылки в список поиска.
                toCrawlList.addAll(links);

                /*
                Проверить на наличие совпадающей строки, и если
                совпадение есть, то записать совпадение.
                */
                if (searchStringMatches(pageContents, searchString,
                        caseSensitive))
                {
                    addMatch(url);
                }
            }

            // Обновить панель Stats.
            updateStats(url, crawledList.size(), toCrawlList.size(),
                    maxUrls);
        }
    }


    /**
     * Запуск окнного приложения
     * @param args
     */
    public static void main(String[] args) {

       SearchCrawler crawler = new SearchCrawler();

       crawler.show();



    }
}
