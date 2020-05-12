package crawler;

import java.net.URL;
import java.util.Objects;

public class Page {
    public final URL url;
    public final String title;
    public final int depth;

    public Page(URL url, String title, int depth) {
        this.url = url;
        this.title = title;
        this.depth = depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return url.equals(page.url) &&
                title.equals(page.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, title);
    }
}
