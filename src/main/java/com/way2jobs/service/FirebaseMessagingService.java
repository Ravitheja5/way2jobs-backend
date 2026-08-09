package com.way2jobs.service;

import java.util.Map;

public interface FirebaseMessagingService {

    void sendNotification(String title, String body, Long jobId, Map<String, String> data);

}
