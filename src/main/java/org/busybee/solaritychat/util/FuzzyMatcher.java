package org.busybee.solaritychat.util;

public class FuzzyMatcher {

    public static int getLevenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= s1.length(); i++) {
            int[] curr = new int[s2.length() + 1];
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int d1 = prev[j] + 1;
                int d2 = curr[j - 1] + 1;
                int d3 = prev[j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1);
                curr[j] = Math.min(Math.min(d1, d2), d3);
            }
            prev = curr;
        }
        return prev[s2.length()];
    }

    public static boolean isSimilar(String s1, String s2, double threshold) {
        if (s1.isEmpty() || s2.isEmpty()) return false;
        if (s1.equals(s2)) return true;
        
        int distance = getLevenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        double similarity = 1.0 - ((double) distance / maxLength);
        
        return similarity >= threshold;
    }
}
