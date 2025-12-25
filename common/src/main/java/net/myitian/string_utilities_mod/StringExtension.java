package net.myitian.string_utilities_mod;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.CharSet;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import static net.myitian.string_utilities_mod.commands.StringCommandCore.checkInt;

public class StringExtension {
    public static void iterate(BreakIterator bi, String text, Consumer<String> consumer) {
        bi.setText(text);
        int start = bi.first();
        for (int end = bi.next(); end != BreakIterator.DONE; start = end, end = bi.next()) {
            consumer.accept(text.substring(start, end));
        }
    }

    public static String trim(String s, CharSet trimChars) {
        if (trimChars == null) {
            return s.trim();
        } else {
            int begin = firstNotTrimmedPos(s, trimChars);
            int end = lastNotTrimmedPos(s, trimChars);
            if (begin >= end) {
                return "";
            } else {
                return s.substring(begin, end);
            }
        }
    }

    public static String trimStart(String s, CharSet trimChars) {
        return s.substring(firstNotTrimmedPos(s, trimChars));
    }

    public static String trimEnd(String s, CharSet trimChars) {
        return s.substring(0, lastNotTrimmedPos(s, trimChars) + 1);
    }

    public static int firstNotTrimmedPos(String s, CharSet trimChars) {
        int len = s.length();
        int i = 0;
        if (trimChars == null) {
            while (i < len && s.charAt(i) <= ' ') {
                i++;
            }
        } else {
            while (i < len && trimChars.contains(s.charAt(i))) {
                i++;
            }
        }
        return i;
    }

    public static int lastNotTrimmedPos(String s, CharSet trimChars) {
        int len = s.length();
        int i = len - 1;
        if (trimChars == null) {
            while (i >= 0 && s.charAt(i) <= ' ') {
                i--;
            }
        } else {
            while (i >= 0 && trimChars.contains(s.charAt(i))) {
                i--;
            }
        }
        return i;
    }

    public static ArrayList<MatchResult> matchesAll(Matcher matcher) {
        int start = 0;
        ArrayList<MatchResult> results = new ArrayList<>();
        while (matcher.find(start)) {
            results.add(matcher.toMatchResult());
            start = matcher.end();
        }
        return results;
    }

    public static ArrayList<MatchResult> matchesAllFully(Matcher matcher) {
        int start = 0;
        ArrayList<MatchResult> results = new ArrayList<>();
        while (matcher.find(start)) {
            results.add(matcher.toMatchResult());
            start = matcher.start() + 1;
        }
        return results;
    }

    public static int convertIndex(int index, String s) {
        return index >= 0 ? index : s.length() + index;
    }

    public static int convertAndCheckIndexWider(int index, String s) throws CommandSyntaxException {
        checkInt(index, -s.length(), s.length());
        return convertIndex(index, s);
    }

    public static int convertAndCheckIndex(int index, String s) throws CommandSyntaxException {
        checkInt(index, -s.length(), s.length() - 1);
        return convertIndex(index, s);
    }

    public static int convertAndCheckIndexBefore(int index, String s) throws CommandSyntaxException {
        checkInt(index, 1 - s.length(), -1, 1, s.length());
        return convertIndex(index, s);
    }

    public static void checkIndex(int index, String s) throws CommandSyntaxException {
        checkInt(index, 0, s.length());
    }
}