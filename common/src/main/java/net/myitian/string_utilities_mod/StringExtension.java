package net.myitian.string_utilities_mod;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.myitian.string_utilities_mod.commands.StringCommandCore;

import java.text.BreakIterator;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

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

    public static void matchesAll(Matcher matcher, Consumer<MatchResult> consumer) {
        int start = 0;
        while (matcher.find(start)) {
            consumer.accept(matcher.toMatchResult());
            start = matcher.end();
        }
    }

    public static void matchesAllFully(Matcher matcher, Consumer<MatchResult> consumer) {
        int start = 0;
        while (matcher.find(start)) {
            consumer.accept(matcher.toMatchResult());
            start = matcher.start() + 1;
        }
    }

    public static int convertIndex(int index, String s) {
        return index >= 0 ? index : s.length() + index;
    }

    public static int convertAndCheckIndexWider(int index, String s) throws CommandSyntaxException {
        checkInt(index, -s.length(), s.length());
        return convertIndex(index, s);
    }

    public static int convertAndCheckIndex(int index, String s) throws CommandSyntaxException {
        if (s.isEmpty()) throw StringCommandCore.STRING_EMPTY_EXCEPTION.create();
        checkInt(index, -s.length(), s.length() - 1);
        return convertIndex(index, s);
    }

    public static int convertAndCheckIndexBefore(int index, String s) throws CommandSyntaxException {
        if (s.isEmpty()) throw StringCommandCore.STRING_EMPTY_EXCEPTION.create();
        checkInt(index, 1 - s.length(), -1, 1, s.length());
        return convertIndex(index, s);
    }

    public static void checkInt(int value, int min, int max) throws CommandSyntaxException {
        if (value < min) {
            throw StringCommandCore.INTEGER_TOO_LOW_EXCEPTION.create(value, min);
        } else if (value > max) {
            throw StringCommandCore.INTEGER_TOO_HIGH_EXCEPTION.create(value, max);
        }
    }

    public static void checkInt(int value, int range0min, int range0max, int range1min, int range1max) throws CommandSyntaxException {
        var min = Math.min(range0min, range1min);
        var max = Math.max(range0max, range1max);
        if (value < min) {
            throw StringCommandCore.INTEGER_TOO_LOW_EXCEPTION.create(value, min);
        } else if (value > max) {
            throw StringCommandCore.INTEGER_TOO_HIGH_EXCEPTION.create(value, max);
        } else if ((value > range0max && value < range1min) || (value > range1max && value < range0min)) {
            throw StringCommandCore.INTEGER_NOT_IN_RANGE_2_EXCEPTION.create(value,
                "[" + range0min + ".." + range0max + "]",
                "[" + range1min + ".." + range1max + "]");
        }
    }
}