package com.way2jobs.scraper.dto;
import lombok.Data; import java.time.Instant; import java.util.*;
@Data public class ScraperRunResult { private Instant startedAt, finishedAt; private long durationMs; private int pagesVisited, rowsFound, imported, duplicates, skipped, failed; private List<String> messages=new ArrayList<>(); public void addMessage(String value) { if(messages.size()<100) messages.add(value); } }
