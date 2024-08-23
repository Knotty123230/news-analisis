package com.reader.newsanalisis.parser;

import com.reader.newsanalisis.entity.News;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.*;

@Component
public class NVParser implements NewsParser {

    private final Logger logger = LoggerFactory.getLogger(NVParser.class.getName());

    @Value("${NV.URL}")
    private String url;
    private String html;

    @PostConstruct
    public void init() {
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
        logger.info("Start parsing News for bean NVParser");
        Elements newsDiv = Jsoup.parse(html)
                .getElementsByClass("col-lg-9")
                .get(0)
                .getElementsByClass("row-result-body");
        List<News> news = findNews(newsDiv);
        logger.info( "End parsing News for bean NVParser");
        return news;
    }

    private List<News> findNews(Elements newsDiv) {
        List<News> newsList = new ArrayList<>();
        newsDiv.forEach(news -> {
            String href = news.getElementsByClass("row-result-body").attr("href");
            String title = news.getElementsByClass("title ").text();
            String[] authorAndContent = findContent(href);
            if (authorAndContent.length == 0) return;
            News e = new News();
            e.setTitle(title);
            e.setUrl(href);
            e.setPublishedAt(Instant.now());
            e.setContent(authorAndContent[1]);
            e.setAuthor(authorAndContent[0]);
            newsList.add(e);
        });
        return newsList;
    }

    private String[] findContent(String href) {
        try {
            Document document = Jsoup.connect(href)
                    .timeout(2000)
                    .get();
            String authorName = document.getElementsByClass("author_text").text();
            String content = document
                    .getElementsByClass("article-content-body")
                    .text();
            return new String[]{authorName, content};
        } catch (IOException e) {
            logger.error("Method findContent in class {} throw exception {} {}",this.getClass().getName(), e.getMessage(), IOException.class);
        }
        return new String[0];
    }
}
