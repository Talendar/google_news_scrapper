import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Web scrapper used to retrieve news from Google.
 * @author Gabriel G. Nogueira (Talendar)
 */
public class GNewsScrapper implements Runnable
{
    private static final String GNEWS_BASE_URL = "https://www.google.com/search?q=";
    private static final String NEWS_URL_PARAM = "&tbm=nws";
    private static final String LANGUAGE_URL_PARAM = "&hl=";

    private String language;
    private List<String> searchTerms = new ArrayList<>();

    private Runnable callback;
    private List<SearchItem> searchResults = new ArrayList<>();


    /**
     * Full constructor.
     *
     * @param language language param to be put in the URL.
     * @param runnable callback function; called when the search is finished.
     * @param searchTerms search terms to be used in the search.
     */
    public GNewsScrapper(String language, Runnable runnable, String... searchTerms) {
        if(searchTerms.length == 0)
            throw new IllegalArgumentException();

        this.language = language;
        this.callback = runnable;
        this.searchTerms.addAll(Arrays.asList(searchTerms));

        Thread thread = new Thread(this);
        thread.start();
    }


    /**
     * Constructor without the language parameter.
     */
    public GNewsScrapper(Runnable runnable, String... searchTerms) {
        this("", runnable, searchTerms);
    }


    /**
     * Represents an item obtained during a search.
     */
    public class SearchItem
    {
        private final String link;
        private final String title;
        private final String description;
        private final String author;
        private final String age;

        public SearchItem(String link, String title, String description, String author, String age) {
            this.link = link;
            this.title = title;
            this.description = description;
            this.author = author;
            this.age = age;
        }

        public String getLink() { return this.link; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getAuthor() { return author; }
        public String getAge() { return age; }
    }


    /**
     * Mounts the URL to be used in the search.
     */
    private String mountURL() {
        StringBuilder url = new StringBuilder(GNEWS_BASE_URL);
        for(String s: this.searchTerms) {
            url.append(s).append("+");
        }
        url.deleteCharAt(url.length() - 1);

        url.append(NEWS_URL_PARAM);
        if(this.language.length() > 0) {
            url.append(LANGUAGE_URL_PARAM).append(this.language);
        }

        return url.toString();
    }


    /**
     * Executes the search.
     */
    @Override
    public void run() {
        try {
            Document doc = Jsoup.connect(this.mountURL()).get();
            Elements elements = doc.select("div[class=dbsr]");

            for(Element e: elements) {
                String link = e.select("a").attr("href");
                String title = e.select("div[role=heading]").text();
                String description = e.select("div.Y3v8qd").text();
                String author = e.select("div.XTjFC.WF4CUc").text();
                String age = e.select("span.WG9SHc").text();

                this.searchResults.add(new SearchItem(link, title, description, author, age));
            }
        }
        catch (IOException e) {
            Log.e("JSOUP", e.toString());
        }

        this.callback.run();
    }

	
    /**
     * Returns a list with the search results.
     */
    public List<SearchItem> getSearchResults() {
        return this.searchResults;
    }
}
