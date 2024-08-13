package com.hhplus.ecommerce.saga.manager;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class EventStatusManager {
    private final ConcurrentMap<String, Boolean> eventStatusMap = new ConcurrentHashMap<>();

    public boolean isEventProcessed(String eventId) {
        return eventStatusMap.getOrDefault(eventId, false);
    }

    public void markEventAsProcessed(String eventId) {
        eventStatusMap.put(eventId, true);
    }

    public void markEventAsUnprocessed(String eventId) {
        eventStatusMap.remove(eventId);
    }
}
