package net.myitian.string_utilities_mod.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.nbt.*;
import net.myitian.string_utilities_mod.JsonNbt;
import net.myitian.string_utilities_mod.StringExtension;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static net.myitian.string_utilities_mod.commands.StringCommandCore.*;

public final class StringCommands {

    public static int isBlank(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        return toInt(src.isBlank());
    }

    public static int isEmpty(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        return toInt(src.isEmpty());
    }

    public static int length(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        return src.length();
    }

    public static int toString(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = getNbtValueAsString(ctx.sources[0]);
        return setTarget(ctx, txt);
    }

    public static int escape(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        StringBuilder stringBuilder = new StringBuilder(" ");
        for (int i = 0; i < src.length(); ++i) {
            char d = src.charAt(i);
            if (d == '\\' || d == '"') {
                stringBuilder.append('\\');
            }
            stringBuilder.append(d);
        }
        return setTarget(ctx, stringBuilder.toString());
    }

    public static int escapeNbt(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = StringTag.quoteAndEscape(getNbtValueAsString(ctx.sources[0]));
        return setTarget(ctx, txt);
    }

    public static int escapeRegex(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = Pattern.quote(getNbtValueAsString(ctx.sources[0]));
        return setTarget(ctx, txt);
    }

    public static int toLowerCase(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = getNbtValueAsString(ctx.sources[0]).toLowerCase();
        return setTarget(ctx, txt);
    }

    public static int toUpperCase(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = getNbtValueAsString(ctx.sources[0]).toUpperCase();
        return setTarget(ctx, txt);
    }

    public static int toLowerCaseInvariant(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = getNbtValueAsString(ctx.sources[0]).toLowerCase(Locale.ROOT);
        return setTarget(ctx, txt);
    }

    public static int toUpperCaseInvariant(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = getNbtValueAsString(ctx.sources[0]).toUpperCase(Locale.ROOT);
        return setTarget(ctx, txt);
    }

    public static int strip(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = getNbtValueAsString(ctx.sources[0]).strip();
        return setTarget(ctx, txt);
    }

    public static int stripLeading(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = getNbtValueAsString(ctx.sources[0]).stripLeading();
        return setTarget(ctx, txt);
    }

    public static int stripTrailing(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String txt = getNbtValueAsString(ctx.sources[0]).stripTrailing();
        return setTarget(ctx, txt);
    }

    public static int toCharArray(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        ListTag list = new ListTag();
        for (char c : src.toCharArray()) {
            list.add(StringTag.valueOf(Character.toString(c)));
        }
        setTarget(ctx, list);
        return list.size();
    }

    public static int toCodePointStrings(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        ListTag list = new ListTag();
        src.codePoints().mapToObj(Character::toString).map(StringTag::valueOf).forEach(list::add);
        setTarget(ctx, list);
        return list.size();
    }

    public static int toCodePoints(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        IntArrayTag arr = new IntArrayTag(src.codePoints().toArray());
        setTarget(ctx, arr);
        return arr.size();
    }

    public static int fromCodePoints(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        var element = getTag(ctx.sources[0]);
        String result;
        if (!(element instanceof CollectionTag<?> list)) {
            throw EXPECTED_LIST_EXCEPTION.create(element);
        } else if (list.isEmpty()) {
            result = "";
        } else {
            int size = list.size();
            StringBuilder sb = new StringBuilder();
            if (!(list.get(0) instanceof NumericTag)) {
                throw EXPECTED_INT_ARRAY_EXCEPTION.create(element.getType().getPrettyName());
            }
            for (int i = 0; i < size; i++) {
                sb.appendCodePoint(((NumericTag) list.get(i)).getAsInt());
            }
            result = sb.toString();
        }
        return setTarget(ctx, result);
    }

    public static int breakCharacters(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        BreakIterator bi = BreakIterator.getCharacterInstance();
        ListTag list = new ListTag();
        StringExtension.iterate(bi, src, it -> list.add(StringTag.valueOf(it)));
        setTarget(ctx, list);
        return list.size();
    }

    public static int breakCharactersInvariant(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        BreakIterator bi = BreakIterator.getCharacterInstance(Locale.ROOT);
        ListTag list = new ListTag();
        StringExtension.iterate(bi, src, it -> list.add(StringTag.valueOf(it)));
        setTarget(ctx, list);
        return list.size();
    }

    public static int breakLines(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        BreakIterator bi = BreakIterator.getLineInstance();
        ListTag list = new ListTag();
        StringExtension.iterate(bi, src, it -> list.add(StringTag.valueOf(it)));
        setTarget(ctx, list);
        return list.size();
    }

    public static int breakLinesInvariant(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        BreakIterator bi = BreakIterator.getLineInstance(Locale.ROOT);
        ListTag list = new ListTag();
        StringExtension.iterate(bi, src, it -> list.add(StringTag.valueOf(it)));
        setTarget(ctx, list);
        return list.size();
    }

    public static int breakSentences(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        BreakIterator bi = BreakIterator.getSentenceInstance();
        ListTag list = new ListTag();
        StringExtension.iterate(bi, src, it -> list.add(StringTag.valueOf(it)));
        setTarget(ctx, list);
        return list.size();
    }

    public static int breakSentencesInvariant(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        BreakIterator bi = BreakIterator.getSentenceInstance(Locale.ROOT);
        ListTag list = new ListTag();
        StringExtension.iterate(bi, src, it -> list.add(StringTag.valueOf(it)));
        setTarget(ctx, list);
        return list.size();
    }

    public static int breakWords(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        BreakIterator bi = BreakIterator.getWordInstance();
        ListTag list = new ListTag();
        StringExtension.iterate(bi, src, it -> list.add(StringTag.valueOf(it)));
        setTarget(ctx, list);
        return list.size();
    }

    public static int breakWordsInvariant(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        String src = getNbtValueAsString(ctx.sources[0]);
        BreakIterator bi = BreakIterator.getWordInstance(Locale.ROOT);
        ListTag list = new ListTag();
        StringExtension.iterate(bi, src, it -> list.add(StringTag.valueOf(it)));
        setTarget(ctx, list);
        return list.size();
    }

    public static int concat(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        var element = getTag(ctx.sources[0]);
        if (!(element instanceof CollectionTag<?> list)) {
            throw EXPECTED_LIST_EXCEPTION.create(element);
        }
        String[] strings = new String[list.size()];
        int len = 0;
        for (int i = 0; i < strings.length; i++) {
            len += (strings[i] = list.get(i).getAsString()).length();
        }
        var sb = new StringBuilder(len);
        for (String string : strings) {
            sb.append(string);
        }
        return setTarget(ctx, sb.toString());
    }

    public static int toJson(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        boolean isPrettyPrinting = ctx.sources.length > 1 && getNbtValueAsInt(ctx.sources[1]) != 0;
        String json = JsonNbt.convert(getTag(ctx.sources[0]), isPrettyPrinting);
        return setTarget(ctx, json);
    }

    public static int trim(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        var src = getNbtValueAsString(ctx.sources[0]);
        CharSet trimChars = createTrimCharsSet(ctx);
        return setTarget(ctx, StringExtension.trim(src, trimChars));
    }

    public static int trimStart(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        var src = getNbtValueAsString(ctx.sources[0]);
        CharSet trimChars = createTrimCharsSet(ctx);
        return setTarget(ctx, StringExtension.trimStart(src, trimChars));
    }

    public static int trimEnd(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 1);
        var src = getNbtValueAsString(ctx.sources[0]);
        CharSet trimChars = createTrimCharsSet(ctx);
        return setTarget(ctx, StringExtension.trimEnd(src, trimChars));
    }

    public static int at(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        int i = StringExtension.convertAndCheckIndex(getNbtValueAsInt(ctx.sources[1]), src);
        char cp = src.charAt(i);
        setTarget(ctx, Character.toString(cp));
        return cp;
    }

    public static int codePointAt(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        int i = StringExtension.convertAndCheckIndex(getNbtValueAsInt(ctx.sources[1]), src);
        int cp = src.codePointAt(i);
        setTarget(ctx, Character.toString(cp));
        return cp;
    }

    public static int codePointBefore(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        int i = StringExtension.convertAndCheckIndexBefore(getNbtValueAsInt(ctx.sources[1]), src);
        int cp = src.codePointBefore(i);
        setTarget(ctx, Character.toString(cp));
        return cp;
    }

    public static int repeat(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        var r = getNbtValueAsInt(ctx.sources[1]);
        checkInt(r, 0, Integer.MAX_VALUE);
        return setTarget(ctx, src.repeat(r));
    }

    public static int matchesAll(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        var p = getNbtValueAsString(ctx.sources[1]);
        var regex = Pattern.compile(p);
        var matcher = regex.matcher(src);
        var list = new ListTag();
        for (MatchResult r : StringExtension.matchesAll(matcher)) {
            var nbt = new CompoundTag();
            nbt.putInt("start", r.start());
            nbt.putInt("end", r.end());
            list.add(nbt);
        }
        setTarget(ctx, list);
        return list.size();
    }

    public static int matchesAllFully(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        var p = getNbtValueAsString(ctx.sources[1]);
        var regex = Pattern.compile(p);
        var matcher = regex.matcher(src);
        var list = new ListTag();
        for (MatchResult r : StringExtension.matchesAllFully(matcher)) {
            var nbt = new CompoundTag();
            nbt.putInt("start", r.start());
            nbt.putInt("end", r.end());
            list.add(nbt);
        }
        setTarget(ctx, list);
        return list.size();
    }

    public static int join(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var delimiter = getNbtValueAsString(ctx.sources[0]);
        var element = getTag(ctx.sources[1]);
        String result;
        if (element instanceof StringTag str) {
            var s = str.getAsString();
            if (s.isEmpty()) {
                result = "";
            } else {
                char[] chars = str.getAsString().toCharArray();
                var sb = new StringBuilder((chars.length - 1) * delimiter.length() + 1);
                sb.append(chars[0]);
                for (int i = 1; i < chars.length; ) {
                    sb.append(delimiter).append(chars[i++]);
                }
                result = sb.toString();
            }
        } else if (element instanceof CollectionTag<?> list) {
            if (list.isEmpty()) {
                result = "";
            } else {
                var sb = new StringBuilder();
                boolean first = true;
                for (Tag tag : list) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(delimiter);
                    }
                    sb.append(tag.getAsString());
                }
                result = sb.toString();
            }
        } else {
            throw EXPECTED_LIST_EXCEPTION.create(element);
        }
        return setTarget(ctx, result);
    }

    public static int concat2(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var s0 = getNbtValueAsString(ctx.sources[0]);
        var s1 = getNbtValueAsString(ctx.sources[1]);
        var result = s0 + s1;
        if (ctx.sources.length > 2) {
            result += getNbtValueAsString(ctx.sources[2]);
        }
        return setTarget(ctx, result);
    }

    public static int substring(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        int begin = StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(ctx.sources[1]), src);
        String result;
        if (ctx.sources.length > 2) {
            int end = getNbtValueAsInt(ctx.sources[2]);
            checkInt(end, begin - src.length(), -1, begin, src.length());
            result = src.substring(begin, StringExtension.convertIndex(end, src));
        } else {
            result = src.substring(begin);
        }
        return setTarget(ctx, result);
    }

    public static int substring2(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        int begin = StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(ctx.sources[1]), src);
        String result;
        if (ctx.sources.length > 2) {
            int length = getNbtValueAsInt(ctx.sources[2]);
            checkInt(length, 0, src.length() - begin);
            result = src.substring(begin, begin + length);
        } else {
            result = src.substring(begin);
        }
        return setTarget(ctx, result);
    }

    public static int split(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        var sep = getNbtValueAsString(ctx.sources[1]);
        String[] result;
        if (ctx.sources.length > 2) {
            int i = getNbtValueAsInt(ctx.sources[2]);
            checkInt(i, 0, Integer.MAX_VALUE);
            result = src.split(sep, i);
        } else {
            result = src.split(sep);
        }
        ListTag list = new ListTag();
        for (String s : result) {
            list.add(StringTag.valueOf(s));
        }
        setTarget(ctx, list);
        return result.length;
    }

    public static int indexOf(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        String src = getNbtValueAsString(ctx.sources[0]);
        String sub = getNbtValueAsString(ctx.sources[1]);
        if (ctx.sources.length > 2) {
            return src.indexOf(sub, StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(ctx.sources[2]), src));
        } else {
            return src.indexOf(sub);
        }
    }

    public static int lastIndexOf(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        String src = getNbtValueAsString(ctx.sources[0]);
        String sub = getNbtValueAsString(ctx.sources[1]);
        if (ctx.sources.length > 2) {
            return src.lastIndexOf(sub, StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(ctx.sources[2]), src));
        } else {
            return src.lastIndexOf(sub);
        }
    }

    public static int startsWith(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        var prefix = getNbtValueAsString(ctx.sources[1]);
        boolean result;
        if (ctx.sources.length > 2) {
            result = src.startsWith(prefix, StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(ctx.sources[2]), src));
        } else {
            result = src.startsWith(prefix);
        }
        return toInt(result);
    }

    public static int endsWith(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        var suffix = getNbtValueAsString(ctx.sources[1]);
        return toInt(src.endsWith(suffix));
    }

    public static int contains(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        var substring = getNbtValueAsString(ctx.sources[1]);
        return toInt(src.contains(substring));
    }

    public static int matches(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 2);
        var src = getNbtValueAsString(ctx.sources[0]);
        var regex = getNbtValueAsString(ctx.sources[1]);
        return toInt(src.matches(regex));
    }

    public static int replace(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 3);
        var src = getNbtValueAsString(ctx.sources[0]);
        var target = getNbtValueAsString(ctx.sources[1]);
        var replacement = getNbtValueAsString(ctx.sources[2]);
        return setTarget(ctx, src.replace(target, replacement));
    }

    public static int replaceAll(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 3);
        var src = getNbtValueAsString(ctx.sources[0]);
        var regex = getNbtValueAsString(ctx.sources[1]);
        var replacement = getNbtValueAsString(ctx.sources[2]);
        return setTarget(ctx, src.replaceAll(regex, replacement));
    }

    public static int replaceFirst(StringCommandContext ctx) throws CommandSyntaxException {
        checkArgumentCount(ctx.sources, 3);
        var src = getNbtValueAsString(ctx.sources[0]);
        var regex = getNbtValueAsString(ctx.sources[1]);
        var replacement = getNbtValueAsString(ctx.sources[2]);
        return setTarget(ctx, src.replaceFirst(regex, replacement));
    }
}