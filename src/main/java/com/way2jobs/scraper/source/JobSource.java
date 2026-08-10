package com.way2jobs.scraper.source;
import com.way2jobs.scraper.config.ScraperProperties; import com.way2jobs.scraper.model.ScrapedJob; import java.util.List;
public interface JobSource { String name(); List<ScrapedJob> fetch(String stateName, String url, ScraperProperties props) throws Exception; }
