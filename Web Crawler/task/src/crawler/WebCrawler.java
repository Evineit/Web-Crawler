package crawler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    JToggleButton buttonRun;
    JTextField workField;
    JTextField depthField;
    JCheckBox depthCBox;
    JTextField timeLimitField;
    JCheckBox timeCBox;
    JLabel currentTimeLabel;
    JLabel parsedPagesLabel;
    JTextField textFieldExport;
    JButton exportButton = new JButton("Export");

    final String LINE_SEPARATOR = System.getProperty("line.separator");
    JPanel mainPanel = new JPanel();
    GridBagLayout gridBagLayout = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();

    ThreadPoolExecutor executor;
    private ConcurrentLinkedQueue<Page> websites;
    public static final Pattern PATTERN_LINK = Pattern.compile("<a href=\"(.*?)\">");

    public WebCrawler() {
        super("Web crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 260);
        setContentPane(mainPanel);
        initComponents();
        setVisible(true);
        setLocationRelativeTo(null);

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
        List<Page> websites = new ArrayList<>(this.websites);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (Page website : websites) {
                printWriter.println(website.url);
                printWriter.println(website.title);
            }
        } catch (IOException e) {
            System.out.printf("An exception occurs %s", e.getMessage());
        }

    }

    private URL getUrl(String link) {
        try {
            URL url = new URL(link);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
            String contentType = urlConnection.getContentType();
            if ("text/html".equals(contentType)) {
                return url;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void processPage(Page parentPage) {
        try {
            URL sourceURL = parentPage.url;
            final StringBuilder html = getHtml(sourceURL);
            if (websites.contains(parentPage)) {
                return;
            }
            addPage(parentPage);
            Matcher matcherLinks = PATTERN_LINK.matcher(html.toString());
            while (matcherLinks.find()) {
                String link = matcherLinks.group(1);
                URL childURL = getUrl(sourceURL.getProtocol() + "://" + sourceURL.getHost() + ":" + sourceURL.getPort() + "/" + link);
                try {
                    if (childURL != null && (parentPage.depth != 0 || !depthCBox.isSelected())) {
                        executeNewTask(new Page(childURL, getTitle(getHtml(childURL)), parentPage.depth - 1));
                    }
                } catch (ConnectException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void addPage(Page parentPage) {
        websites.add(parentPage);
        parsedPagesLabel.setText(String.valueOf(Integer.parseInt(parsedPagesLabel.getText()) + 1));
    }

    private StringBuilder getHtml(URL sourceURL) throws IOException {
        InputStream inputStream = sourceURL.openStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        final StringBuilder stringBuilder = new StringBuilder();
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
            stringBuilder.append(nextLine);
            stringBuilder.append(LINE_SEPARATOR);
        }
        return stringBuilder;
    }

    private String getTitle(StringBuilder stringBuilder) {
        Pattern pattern = Pattern.compile("<title>(.*)</title>");
        Matcher matcherTitle = pattern.matcher(stringBuilder.toString());
        return (matcherTitle.find()) ? matcherTitle.group(1) : "NO TITLE FOUND";
    }

    private void initComponents() {
        buttonRun = new JToggleButton("Run");
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
        depthCBox = new JCheckBox("Enabled", true);
        timeLimitField = new JTextField("120");
        timeCBox = new JCheckBox("Enabled");
        currentTimeLabel = new JLabel("0");
        parsedPagesLabel = new JLabel("0");

        mainPanel.setLayout(gridBagLayout);
        addMainPanel(startLabel, 0, 1, 0);
        addMainPanel(textFieldURL, 0, 1, 1);
        addMainPanel(buttonRun, 0, 1, 0);
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

        textFieldURL.setName("UrlTextField");
        buttonRun.setName("RunButton");
        depthField.setName("DepthTextField");
        depthCBox.setName("DepthCheckBox");
        parsedPagesLabel.setName("ParsedLabel");
        textFieldExport.setName("ExportUrlTextField");
        exportButton.setName("ExportButton");

        buttonRun.addItemListener(e -> {
            int workers = Integer.parseInt(workField.getText());
            if (buttonRun.isSelected()) {
                websites = new ConcurrentLinkedQueue<>();
                executor = new ThreadPoolExecutor(workers, workers,
                        0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
                crawl();
            }
        });
        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                stop();
                export();
            }
        });
    }

    private void crawl() {
        buttonRun.setText("Stop");
        parsedPagesLabel.setText("0");
        Thread crawl = new Thread(() -> {
            URL url = getUrl(textFieldURL.getText());
            try {
                if (url != null) {
                    processPage(new Page(url, getTitle(getHtml(url)), Integer.parseInt(depthField.getText())));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        crawl.start();
        try {
            crawl.join();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
//        stop();
        buttonRun.setSelected(false);
        buttonRun.setText("Start");
        revalidate();
        repaint();
    }

    private void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeNewTask(Page taskPage) {
        executor.execute(() -> processPage(taskPage));
    }
}