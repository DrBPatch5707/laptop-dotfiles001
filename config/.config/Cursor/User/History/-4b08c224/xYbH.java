package org.drbpatch;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationManager handles notifications for filesystem and project events.
 * All methods are static.
 */
public class NotificationManager {
    private static final List<String> notifications = new ArrayList<>();

    /**
     * Returns a copy of the notifications list.
     * @return List of notifications
     */
    public static List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    /**
     * Clears all notifications.
     */
    public static void clearNotifications() {
        notifications.clear();
    }

    /**
     * Adds a notification to the list.
     * @param notification The notification message
     */
    public static void addNotification(String notification) {
        notifications.add(notification);
    }
} 