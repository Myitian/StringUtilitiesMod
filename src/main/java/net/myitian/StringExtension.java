package net.myitian;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.myitian.command.StringCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringExtension {
    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    private static int codePointAt(char[] value, int index, int end) {
        char c1 = value[index];
        if (Character.isHighSurrogate(c1) && ++index < end) {
            char c2 = value[index];
            if (Character.isLowSurrogate(c2)) {
                return Character.toCodePoint(c1, c2);
            }
        }
        return c1;
    }

    private static int codePointBefore(char[] value, int index) {
        --index;
        char c2 = value[index];
        if (Character.isLowSurrogate(c2) && index > 0) {
            --index;
            char c1 = value[index];
            if (Character.isHighSurrogate(c1)) {
                return Character.toCodePoint(c1, c2);
            }
        }
        return c2;
    }

    public static int indexOfNonWhitespace(char[] value) {
        int length = value.length;
        int left = 0;
        while (left < length) {
            int codepoint = codePointAt(value, left, length);
            if (codepoint != ' ' && codepoint != '\t' && !Character.isWhitespace(codepoint)) {
                break;
            }
            left += Character.charCount(codepoint);
        }
        return left;
    }

    public static int lastIndexOfNonWhitespace(char[] value) {
        int right = value.length;
        while (0 < right) {
            int codepoint = codePointBefore(value, right);
            if (codepoint != ' ' && codepoint != '\t' && !Character.isWhitespace(codepoint)) {
                break;
            }
            right -= Character.charCount(codepoint);
        }
        return right;
    }

    public static boolean isBlank(String s) {
        char[] chars = s.toCharArray();
        return indexOfNonWhitespace(chars) == chars.length;
    }

    public static String repeat(String s, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count is negative: " + count);
        }
        if (count == 1) {
            return s;
        }
        final int len = s.length();
        if (len == 0 || count == 0) {
            return "";
        }
        if (Integer.MAX_VALUE / count < len) {
            throw new OutOfMemoryError("Required length exceeds implementation limit");
        }
        if (len == 1) {
            final char[] single = new char[count];
            Arrays.fill(single, s.charAt(0));
            return new String(single);
        }
        final int limit = len * count;
        final char[] value = s.toCharArray();
        final char[] multiple = new char[limit];
        System.arraycopy(value, 0, multiple, 0, len);
        int copied = len;
        for (; copied < limit - copied; copied <<= 1) {
            System.arraycopy(multiple, 0, multiple, copied, copied);
        }
        System.arraycopy(multiple, 0, multiple, copied, limit - copied);
        return new String(multiple);
    }

    public static String strip(String s) {
        char[] chars = s.toCharArray();
        int length = chars.length;
        int left = indexOfNonWhitespace(chars);
        if (left == length) {
            return "";
        }
        int right = lastIndexOfNonWhitespace(chars);
        boolean ifChanged = (left > 0) || (right < length);
        return ifChanged ? new String(chars, left, right - left) : s;
    }

    public static String stripLeading(String s) {
        char[] chars = s.toCharArray();
        int length = chars.length;
        int left = indexOfNonWhitespace(chars);
        return (left != 0) ? new String(chars, left, length - left) : s;
    }

    public static String stripTrailing(String s) {
        char[] chars = s.toCharArray();
        int length = chars.length;
        int right = lastIndexOfNonWhitespace(chars);
        return (right != length) ? new String(chars, 0, right) : s;
    }

    public static void checkNotBelowZero(int i) throws CommandSyntaxException {
        if (i < 0) {
            throw StringCommand.INTEGER_TOO_LOW.create(i, 0);
        }
    }

    public static String trim(String s, Set<Character> trimChars) {
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

    public static String trimStart(String s, Set<Character> trimChars) {
        return s.substring(firstNotTrimmedPos(s, trimChars));
    }

    public static String trimEnd(String s, Set<Character> trimChars) {
        return s.substring(0, lastNotTrimmedPos(s, trimChars) + 1);
    }

    public static int firstNotTrimmedPos(String s, Set<Character> trimChars) {
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

    public static int lastNotTrimmedPos(String s, Set<Character> trimChars) {
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

    public static String escapeRegex(CharSequence s) {
        return SPECIAL_REGEX_CHARS.matcher(s).replaceAll("\\\\$0");
    }

    public static int convertIndex(int index, CharSequence s) {
        return index >= 0 ? index : s.length() + index;
    }

    public static int convertAndCheckIndexWider(int index, CharSequence s) throws CommandSyntaxException {
        checkInt(index, -s.length(), s.length());
        return convertIndex(index, s);
    }

    public static int convertAndCheckIndex(int index, CharSequence s) throws CommandSyntaxException {
        checkInt(index, -s.length(), s.length() - 1);
        return convertIndex(index, s);
    }

    public static void checkIndex(int index, CharSequence s) throws CommandSyntaxException {
        checkInt(index, 0, s.length());
    }

    public static void checkInt(int value, int min, int max) throws CommandSyntaxException {
        if (value < min) {
            throw StringCommand.INTEGER_TOO_LOW.create(value, min);
        } else if (value > max) {
            throw StringCommand.INTEGER_TOO_HIGH.create(value, max);
        }
    }
}
