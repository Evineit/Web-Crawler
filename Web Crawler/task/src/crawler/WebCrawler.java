package crawler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebCrawler extends JFrame {
    JLabel startLabel;
    JLabel workLabel;
    JLabel maxDepthLabel;
    JLabel timeLimitLabel;
    JLabel timeElapsedLabel;
    JLabel parsedLabel;
    JLabel exportLabel;
    JTextField textFieldURL;
    JToggleButton buttonDownload;
    JTextField workField;
    JTextField depthField;
    JCheckBox depthCBox;
    JTextField timeLimitField;
    JCheckBox timeCBox;
    JLabel currentTimeLabel;
    JLabel parsedPagesLabel;
    JTextField textFieldExport;
    JButton exportButton = new JButton("Export");

    String LINE_SEPARATOR = System.getProperty("line.separator");
    JPanel mainPanel = new JPanel();
    GridBagLayout gridBagLayout = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    DefaultTableModel model = new DefaultTableModel(new String[0][0], new String[]{"Url", "Titles"});
    JTable table = new JTable(model);

    public WebCrawler() {
        super("Web crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 260);
//        setLayout(null);
        setContentPane(mainPanel);
        buttonDownload = new JToggleButton("Run");
        textFieldURL = new JTextField();
        textFieldExport = new JTextField();
        startLabel = new JLabel("Start Url");
        workLabel = new JLabel("Workers");
        maxDepthLabel = new JLabel("Maximum depth");
        timeLimitLabel = new JLabel("Time limit");
        timeElapsedLabel = new JLabel("Elapsed time:");
        parsedLabel = new JLabel("Parsed pages:");
        exportLabel = new JLabel("Export");
        workField = new JTextField("5");
        depthField = new JTextField("1");
        depthCBox = new JCheckBox("Enabled");
        timeLimitField = new JTextField("120");
        timeCBox = new JCheckBox("Enabled");
        currentTimeLabel = new JLabel("0:00");
        parsedPagesLabel = new JLabel("0");

        mainPanel.setLayout(gridBagLayout);
        addMainPanel(startLabel, 0, 1, 0);
        addMainPanel(textFieldURL, 0, 1, 1);
        addMainPanel(buttonDownload, 0, 1, 0);
        addMainPanel(workLabel, 1, 1, 0);
        addMainPanel(workField, 1, GridBagConstraints.REMAINDER, 1);
        addMainPanel(maxDepthLabel, 2, 1, 0);
        addMainPanel(depthField, 2, 1, 1);
        addMainPanel(depthCBox, 2, 1, 0);
        addMainPanel(timeLimitLabel, 3, 1, 0);
        addMainPanel(timeLimitField, 3, 1, 1);
        addMainPanel(timeCBox, 3, 1, 0);
        addMainPanel(timeElapsedLabel, 4, 1, 0);
        addMainPanel(currentTimeLabel, 4, 1, 0);
        addMainPanel(parsedLabel, 5, 1, 0);
        addMainPanel(parsedPagesLabel, 5, 1, 0);
        addMainPanel(exportLabel, 6, 1, 0);
        addMainPanel(textFieldExport, 6, 1, 1);
        addMainPanel(exportButton, 6, 1, 0);
//        addMainPanel();

        textFieldURL.setName("UrlTextField");
        buttonDownload.setName("RunButton");
        depthField.setName("DepthTextField");
        depthCBox.setName("DepthCheckBox");
        parsedPagesLabel.setName("ParsedLabel");
        textFieldExport.setName("ExportUrlTextField");
        exportButton.setName("ExportButton");
        table.setEnabled(false);
        buttonDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parsedPagesLabel.setText("0");
                downloadSource(Integer.parseInt(depthField.getText()));
                revalidate();
                repaint();
            }
        });
        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                export();
            }
        });
        setVisible(true);

    }

    void addMainPanel(JComponent component, int gridy, int width, int weightx) {
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = weightx;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = width;
        constraints.gridy = gridy;
        mainPanel.add(component, constraints);
    }

    private void export() {
        File file = new File(textFieldExport.getText());
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (int i = 0; i < table.getRowCount(); i++) {
                printWriter.println(table.getValueAt(i, 0));
                printWriter.println(table.getValueAt(i, 1));
            }
        } catch (IOException e) {
            System.out.printf("An exception occurs %s", e.getMessage());
        }

    }

    void downloadSource(int depth) {
        model = new DefaultTableModel(new String[0][0], new String[]{"Url", "Titles"});
        table.setModel(model);
        final String url = textFieldURL.getText();
//        Map<String, String> hashMap = new HashMap<>();
        try {
            URL url2 = new URL(url);
            url2.openConnection().setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
            InputStream inputStream = url2.openStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            final StringBuilder stringBuilder = new StringBuilder();
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                stringBuilder.append(nextLine);
                stringBuilder.append(LINE_SEPARATOR);
            }
            Pattern pattern = Pattern.compile("<title>(.*)</title>");
            Matcher matcherTitle = pattern.matcher(stringBuilder.toString());
            matcherTitle.find();
            String titlegroup = matcherTitle.group(1);
            model.addRow(new String[]{url, titlegroup});
            parsedPagesLabel.setText(String.valueOf(Integer.parseInt(parsedPagesLabel.getText()) + 1));
//            Pattern patternLink = Pattern.compile("<a id=\".*\".*href=\"(.*)\".{0,100}title=\"(.*?)\" .*>");
            Pattern patternLink = Pattern.compile("<a href=\"(.*?)\">");
            Matcher matcherLinks = patternLink.matcher(stringBuilder.toString());
            while (matcherLinks.find()) {
                String link = matcherLinks.group(1);
                URL url1 = new URL(url2.getProtocol() + "://" + url2.getHost() + ":" + url2.getPort() + "/" + link);
                try {
                    if (url1.openConnection().getContentType() != null && url1.openConnection().getContentType().equals("text/html")) {
                        InputStream linkInputStream = url1.openStream();
                        final BufferedReader linkreader = new BufferedReader(new InputStreamReader(linkInputStream, StandardCharsets.UTF_8));
                        final StringBuilder linkstringBuilder = new StringBuilder();
                        String linknextLine;
                        while ((linknextLine = linkreader.readLine()) != null) {
                            linkstringBuilder.append(linknextLine);
                            linkstringBuilder.append(LINE_SEPARATOR);
                        }
                        Pattern linkpattern = Pattern.compile("<title>(.*)</title>");
                        Matcher linkmatcherTitle = linkpattern.matcher(linkstringBuilder.toString());
                        linkmatcherTitle.find();
                        String linktitlegroup = linkmatcherTitle.group(1);
                        model.addRow(new String[]{url1.toString(), linktitlegroup});
                        parsedPagesLabel.setText(String.valueOf(Integer.parseInt(parsedPagesLabel.getText()) + 1));

                    }
                } catch (ConnectException e) {
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