package net.myitian;

import java.util.Arrays;

public class Java11StringShim {
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
}
