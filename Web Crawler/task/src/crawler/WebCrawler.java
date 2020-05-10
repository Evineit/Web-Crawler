package crawler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    DefaultTableModel model = new DefaultTableModel(new String[0][0],new String[]{"Url","Titles"});
    JTable table = new JTable(model);
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
        model = new DefaultTableModel(new String[0][0],new String[]{"Url","Titles"});
        table.setModel(model);
        final String url = textFieldURL.getText();
//        Map<String, String> hashMap = new HashMap<>();
        final InputStream inputStream;
        try {
            URL url2 = new URL(url);
            inputStream = url2.openStream();
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
            String titlegroup = matcherTitle.group(1);
            labelTitle.setText(titlegroup);
            model.addRow(new String[]{url,titlegroup});
//            Pattern patternLink = Pattern.compile("<a id=\".*\".*href=\"(.*)\".{0,100}title=\"(.*?)\" .*>");
            Pattern patternLink = Pattern.compile("<a href=\"(.*?)\">");
            Matcher matcherLinks = patternLink.matcher(stringBuilder.toString());
//            int size = (int) matcherLinks.results().count();
////            links= new String[2];
////            int i = 0;
//            matcherLinks.reset();
            while (matcherLinks.find()){
                String link = matcherLinks.group(1);
//                System.out.println(link);

                URL url1 = new URL(url2.getProtocol()+"://"+url2.getHost()+":"+url2.getPort()+"/"+link);
                    try{
                        if (url1.openConnection().getContentType()!=null&&url1.openConnection().getContentType().equals("text/html")){
                        InputStream linkInputStream = url1.openStream();
                        final BufferedReader linkreader = new BufferedReader(new InputStreamReader(linkInputStream, StandardCharsets.UTF_8));
                        final StringBuilder linkstringBuilder = new StringBuilder();
                        String linknextLine;
                        while ((linknextLine = linkreader.readLine()) != null) {
                            linkstringBuilder.append(linknextLine);
                            linkstringBuilder.append(LINE_SEPARATOR);
                        }
                        Pattern linkpattern= Pattern.compile("<title>(.*)</title>");
                        Matcher linkmatcherTitle = linkpattern.matcher(linkstringBuilder.toString());
                        linkmatcherTitle.find();
                        String linktitlegroup = linkmatcherTitle.group(1);
                            model.addRow(new String[]{url1.toString(),linktitlegroup});
                        }
                    }catch (ConnectException e){
                        e.printStackTrace();
                    }

//                links[i][0] = matcherLinks.group(1);
//                links[i][1] = matcherLinks.group(2);
//                i++;
            }
//            model.
//            table.setModel(new DefaultTableModel(links, new String[]{"URL", "Title"}));
            table.setModel(model);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}