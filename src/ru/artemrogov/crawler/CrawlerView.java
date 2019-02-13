package ru.artemrogov.crawler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CrawlerView extends JFrame implements  CrawlerListener, ActionListener {

    private CrawlerController controller;

    // Тут располагаются элементы управления представлением

    /**
     *
     * @throws HeadlessException
     */

    public CrawlerView() throws HeadlessException {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //controller.notifyCrawl();
    }

    @Override
    public void notifyActionSearch() {
        System.out.println("Оповещение элемента управления об обновлении данных");
    }

    public void registerController(CrawlerController controller){

        this.controller = controller;

    }


}
