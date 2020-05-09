package crawler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebCrawler extends JFrame {
    String LINE_SEPARATOR = System.getProperty("line.separator");
    JPanel mainPanel = new JPanel();
    JPanel topPanel = new JPanel();
    JPanel urlPanel = new JPanel();
    JPanel iTopPanel = new JPanel();
    JTextArea textArea = new JTextArea();
    JTextField textFieldURL;
    JButton buttonDownload;
    JLabel labelTitle;
    JTable table = new JTable(new DefaultTableModel(new String[0][0], new String[]{"Titles", "Links"}));
    public WebCrawler() {
        super("Web crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
//        setLayout(null);
//        setContentPane(mainPanel);
        buttonDownload = new JButton("Get text!");
        textFieldURL = new JTextField();
        labelTitle = new JLabel("Title:");
//        table.addColumn(new TableColumn());
//        table.
        JScrollPane scrollPane =  new JScrollPane(table);
        topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.Y_AXIS));
        mainPanel.setLayout(new BorderLayout());
        iTopPanel.setLayout(new BoxLayout(iTopPanel,BoxLayout.X_AXIS));
        urlPanel.setLayout(new BoxLayout(urlPanel,BoxLayout.X_AXIS));

        labelTitle.setName("TitleLabel");
//        textArea.setName("HtmlTextArea");
        table.setName("TitlesTable");
        buttonDownload.setName("RunButton");
        textFieldURL.setName("UrlTextField");
        textArea.setText("HTML code?");
        table.setEnabled(false);
        topPanel.add(urlPanel);
        topPanel.add(iTopPanel);
        urlPanel.add(textFieldURL);
        urlPanel.add(buttonDownload);
        iTopPanel.add(labelTitle);
        add(topPanel,BorderLayout.NORTH);
        add(scrollPane,BorderLayout.CENTER);
        buttonDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                textArea.setText(downloadSource());
                downloadSource();
                revalidate();
                repaint();
            }
        });
        setVisible(true);

    }
    void downloadSource(){
        final String url = textFieldURL.getText();
//        Map<String, String> hashMap = new HashMap<>();
        final InputStream inputStream;
        String links[][];
        try {
            inputStream = new URL(url).openStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            final StringBuilder stringBuilder = new StringBuilder();

            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                stringBuilder.append(nextLine);
                stringBuilder.append(LINE_SEPARATOR);
            }
            Pattern pattern= Pattern.compile("<title>(.*)</title>");
            Matcher matcherTitle = pattern.matcher(stringBuilder.toString());
            matcherTitle.find();
            labelTitle.setText(matcherTitle.group(1));
//            Pattern patternLink = Pattern.compile("<a id=\".*\".*href=\"(.*)\".{0,100}title=\"(.*?)\" .*>");
            Pattern patternLink = Pattern.compile("<a target=\"(.*)\" href=\"(.*?)\">");
            Matcher matcherLinks = patternLink.matcher(stringBuilder.toString());
            int size = (int) matcherLinks.results().count();
            links= new String[size][2];
            int i = 0;
            matcherLinks.reset();
            while (matcherLinks.find()){
                links[i][0] = matcherLinks.group(1);
                links[i][1] = matcherLinks.group(2);
                i++;
            }
            table.setModel(new DefaultTableModel(links, new String[]{"URL", "Title"}));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}