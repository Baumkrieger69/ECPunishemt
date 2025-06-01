package de.ecpunishment.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhdw])");
    
    public static long parseDuration(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        
        input = input.toLowerCase().trim();
        
        // Handle special cases
        if (input.equals("perm") || input.equals("permanent")) {
            return -1;
        }
        
        long totalMillis = 0;
        Matcher matcher = TIME_PATTERN.matcher(input);
        
        while (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "s":
                    totalMillis += amount * 1000L;
                    break;
                case "m":
                    totalMillis += amount * 60 * 1000L;
                    break;
                case "h":
                    totalMillis += amount * 60 * 60 * 1000L;
                    break;
                case "d":
                    totalMillis += amount * 24 * 60 * 60 * 1000L;
                    break;
                case "w":
                    totalMillis += amount * 7 * 24 * 60 * 60 * 1000L;
                    break;
            }
        }
        
        return totalMillis;
    }
    
    public static String formatDuration(long duration) {
        if (duration == -1) {
            return "Permanent";
        }
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        
        StringBuilder sb = new StringBuilder();
        
        if (weeks > 0) {
            sb.append(weeks).append("w ");
            days %= 7;
        }
        if (days > 0) {
            sb.append(days).append("d ");
            hours %= 24;
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
            minutes %= 60;
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
            seconds %= 60;
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }
        
        return sb.toString().trim();
    }
    
    public static String formatDurationGerman(long duration) {
        if (duration == -1) {
            return "Permanent";
        }
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        
        if (weeks > 0) {
            return weeks + " Woche(n)";
        } else if (days > 0) {
            return days + " Tag(e)";
        } else if (hours > 0) {
            return hours + " Stunde(n)";
        } else if (minutes > 0) {
            return minutes + " Minute(n)";
        } else {
            return seconds + " Sekunde(n)";
        }
    }
}
