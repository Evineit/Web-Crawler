package crawler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
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
    public WebCrawler() {
        super("Web crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
//        setLayout(null);
//        setContentPane(mainPanel);
        buttonDownload = new JButton("Get text!");
        textFieldURL = new JTextField();
        labelTitle = new JLabel("Title:");
        topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.Y_AXIS));
        mainPanel.setLayout(new BorderLayout());
        iTopPanel.setLayout(new BoxLayout(iTopPanel,BoxLayout.X_AXIS));
        urlPanel.setLayout(new BoxLayout(urlPanel,BoxLayout.X_AXIS));

        labelTitle.setName("TitleLabel");
        textArea.setName("HtmlTextArea");
        buttonDownload.setName("RunButton");
        textFieldURL.setName("UrlTextField");
        textArea.setText("HTML code?");
        textArea.setEnabled(false);
        topPanel.add(urlPanel);
        topPanel.add(iTopPanel);
        urlPanel.add(textFieldURL);
        urlPanel.add(buttonDownload);
        iTopPanel.add(labelTitle);
        add(topPanel,BorderLayout.NORTH);
        add(textArea,BorderLayout.CENTER);
        buttonDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textArea.setText(downloadSource());
            }
        });
        setVisible(true);

    }
    String downloadSource(){
        final String url = textFieldURL.getText();

        final InputStream inputStream;
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
            Matcher matcher = pattern.matcher(stringBuilder.toString());
            matcher.find();
            labelTitle.setText(matcher.group(1));
            return  stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }
}