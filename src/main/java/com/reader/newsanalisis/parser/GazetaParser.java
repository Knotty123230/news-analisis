package com.reader.newsanalisis.parser;

import com.reader.newsanalisis.entity.News;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Component
public class GazetaParser implements NewsParser {
    private static final Logger logger = LoggerFactory.getLogger(GazetaParser.class);
    private String html;
    @Value("${GAZETA.URL}")
    private String url;

    @PostConstruct
    public void init(){
        try {
            this.html = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .get()
                    .html();
        } catch (IOException e) {
            logger.error("Constructor in class {} throw exception {} {}",this.getClass().getName(), e.getMessage(), IOException.class);
        }
    }

    @Override
    public List<News> parse() {
        logger.info("Start parsing new news from gazeta");
        Document parse = Jsoup.parse(html);
        Elements lastNews = parse
                .getElementsByClass("mb15");
        Elements hideElements = parse.getElementsByClass("item bot hide");
        lastNews.addAll(hideElements);
        List<News> item = findNews(lastNews.get(0).getElementsByClass("item"));
        logger.info( "Finished parsing new news from gazeta");
        return item;
    }

    private List<News> findNews(Elements lastNews) {
        List<News> newsList = new ArrayList<>();
        lastNews.forEach(news -> {
            Element a = news.getAllElements().get(2);
            String url = this.url + a.attr("href");
            String title = a.text();
            String contentArticle = findContentArticle(url);
            News newNews = new News();
            newNews.setContent(contentArticle);
            newNews.setTitle(title);
            newNews.setUrl(url);
            newNews.setPublishedAt(Instant.now());
            newsList.add(newNews);
        });

        return newsList;
    }

    private String findContentArticle(String url) {
        try {
            return Jsoup.connect(url).ignoreContentType(true)
                    .get()
                    .getElementsByTag("article")
                    .get(0)
                    .text();
        } catch (IOException e) {
            logger.error("Method findContentArticle in class {} throw exception {} {}",this.getClass().getName(), e.getMessage(), IOException.class);
        }
        return url;
    }

}
