package com.example.notes.utils;

import android.content.Context;
import com.example.notes.R;

public class TimeUtils {
    
    public static String getTimeAgo(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;
        
        // Convert to seconds
        long seconds = timeDifference / 1000;
        
        if (seconds < 60) {
            return "now";
        }
        
        // Convert to minutes
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m ago";
        }
        
        // Convert to hours
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h ago";
        }
        
        // Convert to days
        long days = hours / 24;
        return days + "d ago";
    }
    
    public static String getTimeAgoWithContext(Context context, long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;
        
        // Convert to seconds
        long seconds = timeDifference / 1000;
        
        if (seconds < 60) {
            return context.getString(R.string.time_ago_now);
        }
        
        // Convert to minutes
        long minutes = seconds / 60;
        if (minutes < 60) {
            return context.getString(R.string.time_ago_minutes, minutes);
        }
        
        // Convert to hours
        long hours = minutes / 60;
        if (hours < 24) {
            return context.getString(R.string.time_ago_hours, hours);
        }
        
        // Convert to days
        long days = hours / 24;
        return context.getString(R.string.time_ago_days, days);
    }
}

