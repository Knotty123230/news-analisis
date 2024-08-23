package com.reader.newsanalisis.parser;

import com.reader.newsanalisis.entity.News;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
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
import java.util.Objects;

@Component
public class RBCParser implements NewsParser {
    @Value("${RBC.URL}")
    private String url;
    private String html;
    private final Logger logger = LoggerFactory.getLogger(RBCParser.class.getName());


    @PostConstruct
    public void init() {
        try {
            this.html = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .timeout(3000)
                    .get()
                    .html();
        } catch (IOException e) {
            logger.error("Constructor in class {} throw exception {} {}",this.getClass().getName(), e.getMessage(), IOException.class);
        }
    }


    @Override
    public List<News> parse() {
        logger.info( "starting parse from bean RBCParser");
        Elements divNews = Jsoup.parse(html)
                .getElementsByClass("newsline")
                .get(0)
                .getElementsByClass("item");
        List<News> news = findNews(divNews);
        logger.info("finished parse from bean RBCParser");
        return news;

    }

    private List<News> findNews(Elements divNews) {
        List<News> newsList = new ArrayList<>();
        divNews.forEach(news -> {
            Element a = news.getElementsByTag("a")
                    .first();
            String href = Objects.requireNonNull(a)
                    .attr("href");
            String title = a.text().substring(5);
            var content = findContent(href);
            if (content.isEmpty()) return;
            News newNews = new News();
            newNews.setTitle(title);
            newNews.setContent(content);
            newNews.setPublishedAt(Instant.now());
            newNews.setUrl(href);
            newsList.add(newNews);
        });
        return newsList;
    }

    private String findContent(String href) {
        try {
            return Jsoup.connect(href)
                    .ignoreContentType(true)
                    .timeout(2000)
                    .get()
                    .getElementsByTag("article")
                    .text();
        } catch (IOException e) {
            logger.error("Method findContent in class {} throw exception {} {}",this.getClass().getName(), e.getMessage(), IOException.class);
        }
        return "";
    }


}
