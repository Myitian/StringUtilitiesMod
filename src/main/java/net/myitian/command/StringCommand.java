package net.myitian.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.command.argument.NbtElementArgumentType.nbtElement;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class StringCommand {
    private static final Dynamic2CommandExceptionType INTEGER_TOO_LOW =
            new Dynamic2CommandExceptionType((found, min) -> Text.translatable("argument.integer.low", min, found));
    private static final Dynamic2CommandExceptionType INTEGER_TOO_HIGH =
            new Dynamic2CommandExceptionType((found, max) -> Text.translatable("argument.integer.big", max, found));
    private static final DynamicCommandExceptionType EXPECTED_LIST_EXCEPTION =
            new DynamicCommandExceptionType(nbt -> Text.translatable("commands.data.modify.expected_list", nbt));
    private static final SimpleCommandExceptionType ARGUMENT_TOO_FEW_EXCEPTION = // [Should not show]
            new SimpleCommandExceptionType(Text.translatable("commands.string-utilities.string.too_few_arguments"));
    private static final DynamicCommandExceptionType INVALID_CHAR_ARRAY_EXCEPTION = // Invalid char array: %s
            new DynamicCommandExceptionType(name -> Text.translatable("commands.string-utilities.string.invalid_char_array", name));
    private static final DynamicCommandExceptionType EXPECTED_STRING_LIST_EXCEPTION = // Invalid argument type: %s, expected String
            new DynamicCommandExceptionType(name -> Text.translatable("commands.string-utilities.string.unexpected_type", name, NbtString.TYPE.getCrashReportName()));
    private static final DynamicCommandExceptionType EXPECTED_INT_ARRAY_EXCEPTION = // Invalid argument type: %s, expected IntArray
            new DynamicCommandExceptionType(name -> Text.translatable("commands.string-utilities.string.unexpected_type", name, NbtIntArray.TYPE.getCrashReportName()));
    private static final DynamicCommandExceptionType EXPECTED_INT_EXCEPTION = // Invalid argument type: %s, expected Int
            new DynamicCommandExceptionType(name -> Text.translatable("commands.string-utilities.string.unexpected_type", name, NbtInt.TYPE.getCrashReportName()));

    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        register(dispatcher);
    }
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        register(dispatcher);
    }
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> stringCommand = literal("string")
                .requires(source -> source.hasPermissionLevel(2))
                .then(addOneInZeroOutArgument("isBlank", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    return toInt(src.isBlank());
                }))
                .then(addOneInZeroOutArgument("isEmpty", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    return toInt(src.isEmpty());
                }))
                .then(addOneInZeroOutArgument("length", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    return src.length();
                }))
                .then(addOneInOneOutArgument("toString", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, NbtString.of(src));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("escape", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    StringBuilder stringBuilder = new StringBuilder(" ");
                    for (int i = 0; i < src.length(); ++i) {
                        char d = src.charAt(i);
                        if (d == '\\' || d == '"') {
                            stringBuilder.append('\\');
                        }
                        stringBuilder.append(d);
                    }
                    setTarget(ctx, scc, NbtString.of(stringBuilder.toString()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("escapeNbt", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, NbtString.of(NbtString.escape(src)));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("escapeRegex", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, NbtString.of(escapeRegex(src)));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("toLowerCase", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, NbtString.of(src.toLowerCase()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("toUpperCase", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, NbtString.of(src.toUpperCase()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("strip", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, NbtString.of(src.strip()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("stripLeading", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, NbtString.of(src.stripLeading()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("stripTrailing", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, NbtString.of(src.stripTrailing()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("toCharArray", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    NbtList list = new NbtList();
                    for (char c : src.toCharArray()) {
                        list.add(NbtString.of(Character.toString(c)));
                    }
                    setTarget(ctx, scc, list);
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("toCodePointStrings", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    NbtList list = new NbtList();
                    list.addAll(src.codePoints().mapToObj(Character::toString).map(NbtString::of).toList());
                    setTarget(ctx, scc, list);
                    return list.size();
                }))
                .then(addOneInOneOutArgument("toCodePoints", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    NbtIntArray arr = new NbtIntArray(src.codePoints().toArray());
                    setTarget(ctx, scc, arr);
                    return arr.size();
                }))
                .then(addOneInOneOutArgument("fromCodePoints", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    var element = getNbtElement(scc.sources[0]);
                    String result;
                    if (!(element instanceof AbstractNbtList<?> list)) {
                        throw EXPECTED_LIST_EXCEPTION.create(element);
                    } else if (list.isEmpty()) {
                        result = "";
                    } else {
                        int size = list.size();
                        var sb = new StringBuilder();
                        if (list.get(0) instanceof AbstractNbtNumber num) {
                            sb.appendCodePoint(num.intValue());
                        } else {
                            throw EXPECTED_INT_ARRAY_EXCEPTION.create(element.getNbtType().getCrashReportName());
                        }
                        for (int i = 1; i < size; i++) {
                            sb.appendCodePoint(((AbstractNbtNumber) list.get(i)).intValue());
                        }
                        result = sb.toString();
                    }
                    setTarget(ctx, scc, NbtString.of(result));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("concat", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    var element = getNbtElement(scc.sources[0]);
                    if (!(element instanceof AbstractNbtList<?> list)) {
                        throw EXPECTED_LIST_EXCEPTION.create(element);
                    }
                    String[] strings = new String[list.size()];
                    int len = 0;
                    for (int i = 0; i < strings.length; i++) {
                        len += (strings[i] = list.get(i).asString()).length();
                    }
                    var sb = new StringBuilder(len);
                    for (String string : strings) {
                        sb.append(string);
                    }
                    setTarget(ctx, scc, NbtString.of(sb.toString()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOptionalInOneOutArgument("trim",
                        "sourcePath",
                        "value",
                        "trimCharsSourcePath",
                        "trimCharsValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 1);
                            var src = getNbtValueAsString(scc.sources[0]);
                            Set<Character> trimChars = createTrimCharsSet(scc);
                            setTarget(ctx, scc, NbtString.of(trim(src, trimChars)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addOneInOneOptionalInOneOutArgument("trimStart",
                        "sourcePath",
                        "value",
                        "trimCharsSourcePath",
                        "trimCharsValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 1);
                            var src = getNbtValueAsString(scc.sources[0]);
                            Set<Character> trimChars = createTrimCharsSet(scc);
                            setTarget(ctx, scc, NbtString.of(trimStart(src, trimChars)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addOneInOneOptionalInOneOutArgument("trimEnd",
                        "sourcePath",
                        "value",
                        "trimCharsSourcePath",
                        "trimCharsValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 1);
                            var src = getNbtValueAsString(scc.sources[0]);
                            Set<Character> trimChars = createTrimCharsSet(scc);
                            setTarget(ctx, scc, NbtString.of(trimEnd(src, trimChars)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOutArgument("at",
                        "sourcePath",
                        "value",
                        "indexSourcePath",
                        "indexValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var i = convertIndex(getNbtValueAsInt(scc.sources[1]), src);
                            checkIndex(i, src);
                            setTarget(ctx, scc, NbtString.of(Character.toString(src.charAt(i))));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOutArgument("repeat",
                        "sourcePath",
                        "value",
                        "countSourcePath",
                        "countValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var r = getNbtValueAsInt(scc.sources[1]);
                            checkNotBelowZero(r);
                            setTarget(ctx, scc, NbtString.of(src.repeat(r)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOutArgument("matchesAll",
                        "sourcePath",
                        "value",
                        "patternSourcePath",
                        "patternValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var p = getNbtValueAsString(scc.sources[1]);
                            var regex = Pattern.compile(p);
                            var matcher = regex.matcher(src);
                            var list = new NbtList();
                            list.addAll(matcher.results().map(r -> {
                                var nbt = new NbtCompound();
                                nbt.putInt("start", r.start());
                                nbt.putInt("end", r.end());
                                return nbt;
                            }).toList());
                            setTarget(ctx, scc, list);
                            return list.size();
                        }))
                .then(addTwoInOneOutArgument("matchesAllFully",
                        "sourcePath",
                        "value",
                        "countSourcePath",
                        "countValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var p = getNbtValueAsString(scc.sources[1]);
                            var regex = Pattern.compile(p);
                            var matcher = regex.matcher(src);
                            var list = new NbtList();
                            for (var r : matchesAll(matcher)) {
                                var nbt = new NbtCompound();
                                nbt.putInt("start", r.start());
                                nbt.putInt("end", r.end());
                                list.add(nbt);
                            }
                            setTarget(ctx, scc, list);
                            return list.size();
                        }))
                .then(addTwoInOneOutArgument("join",
                        "delimiterSourcePath",
                        "delimiterValue",
                        "elementsSourcePath",
                        "elementsValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 1);
                            var delimiter = getNbtValueAsString(scc.sources[0]);
                            var element = getNbtElement(scc.sources[1]);
                            String result;
                            if (element instanceof NbtString str) {
                                char[] chars = str.asString().toCharArray();
                                var sb = new StringBuilder((chars.length - 1) * delimiter.length() + 1);
                                sb.append(chars[0]);
                                for (int i = 1; i < chars.length; ) {
                                    sb.append(delimiter).append(chars[i++]);
                                }
                                result = sb.toString();
                            } else if (element instanceof AbstractNbtList<?> list) {
                                String[] strings = new String[list.size()];
                                int len = 0;
                                for (int i = 0; i < strings.length; i++) {
                                    len += (strings[i] = list.get(i).asString()).length();
                                }
                                len += (strings.length - 1) * delimiter.length();
                                var sb = new StringBuilder(len);
                                sb.append(strings[0]);
                                for (int i = 1; i < strings.length; ) {
                                    sb.append(delimiter).append(strings[i++]);
                                }
                                result = sb.toString();
                            } else {
                                throw EXPECTED_LIST_EXCEPTION.create(element);
                            }
                            setTarget(ctx, scc, NbtString.of(result));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOptionalInOneOutArgument("concat2",
                        "sourcePath0",
                        "value0",
                        "sourcePath1",
                        "value1",
                        "sourcePath2",
                        "value2",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var s0 = getNbtValueAsString(scc.sources[0]);
                            var s1 = getNbtValueAsString(scc.sources[1]);
                            var result = s0 + s1;
                            if (scc.sources.length > 2) {
                                result += getNbtValueAsString(scc.sources[2]);
                            }
                            setTarget(ctx, scc, NbtString.of(result));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOptionalInOneOutArgument("substring",
                        "sourcePath",
                        "value",
                        "beginIndexSourcePath",
                        "beginIndexValue",
                        "endIndexSourcePath",
                        "endIndexValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            int begin = convertIndex(getNbtValueAsInt(scc.sources[1]), src);
                            checkIndex(begin, src);
                            String result;
                            if (scc.sources.length > 2) {
                                int end = convertIndex(getNbtValueAsInt(scc.sources[2]), src);
                                checkInt(end, begin, src.length() - 1);
                                result = src.substring(begin, end);
                            } else {
                                result = src.substring(begin);
                            }
                            setTarget(ctx, scc, NbtString.of(result));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOptionalInOneOutArgument("substring2",
                        "sourcePath",
                        "value",
                        "beginIndexSourcePath",
                        "beginIndexValue",
                        "lengthSourcePath",
                        "lengthValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            int begin = convertIndex(getNbtValueAsInt(scc.sources[1]), src);
                            checkIndex(begin, src);
                            String result;
                            if (scc.sources.length > 2) {
                                int length = convertIndex(getNbtValueAsInt(scc.sources[2]), src);
                                checkInt(length, 0, src.length() - begin);
                                result = src.substring(begin, begin + length);
                            } else {
                                result = src.substring(begin);
                            }
                            setTarget(ctx, scc, NbtString.of(result));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOptionalInOneOutArgument("split",
                        "sourcePath",
                        "value",
                        "separatorRegexSourcePath",
                        "separatorRegexValue",
                        "limitSourcePath",
                        "limitValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var sep = getNbtValueAsString(scc.sources[1]);
                            String[] result;
                            if (scc.sources.length > 2) {
                                var i = getNbtValueAsInt(scc.sources[2]);
                                checkNotBelowZero(i);
                                result = src.split(sep, i);
                            } else {
                                result = src.split(sep);
                            }
                            NbtList list = new NbtList();
                            for (String s : result) {
                                list.add(NbtString.of(s));
                            }
                            setTarget(ctx, scc, list);
                            return result.length;
                        }))
                .then(addTwoInOneOptionalInZeroOutArgument("indexOf",
                        "sourcePath",
                        "value",
                        "substringSourcePath",
                        "substringValue",
                        "fromIndexSourcePath",
                        "fromIndexValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            String src = getNbtValueAsString(scc.sources[0]);
                            String sub = getNbtValueAsString(scc.sources[1]);
                            if (scc.sources.length > 2) {
                                return src.indexOf(sub, convertIndex(getNbtValueAsInt(scc.sources[2]), src));
                            } else {
                                return src.indexOf(sub);
                            }
                        }))
                .then(addTwoInOneOptionalInZeroOutArgument("lastIndexOf",
                        "sourcePath",
                        "value",
                        "substringSourcePath",
                        "substringValue",
                        "fromIndexSourcePath",
                        "fromIndexValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            String src = getNbtValueAsString(scc.sources[0]);
                            String sub = getNbtValueAsString(scc.sources[1]);
                            if (scc.sources.length > 2) {
                                return src.lastIndexOf(sub, convertIndex(getNbtValueAsInt(scc.sources[2]), src));
                            } else {
                                return src.lastIndexOf(sub);
                            }
                        }))
                .then(addTwoInOneOptionalInZeroOutArgument("startsWith",
                        "sourcePath",
                        "value",
                        "prefixSourcePath",
                        "prefixValue",
                        "offsetSourcePath",
                        "offsetValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var prefix = getNbtValueAsString(scc.sources[1]);
                            boolean result;
                            if (scc.sources.length > 2) {
                                result = src.startsWith(prefix, convertIndex(getNbtValueAsInt(scc.sources[2]), src));
                            } else {
                                result = src.startsWith(prefix);
                            }
                            return toInt(result);
                        }))
                .then(addTwoInZeroOutArgument("endsWith",
                        "sourcePath",
                        "value",
                        "suffixSourcePath",
                        "suffixValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var suffix = getNbtValueAsString(scc.sources[1]);
                            return toInt(src.endsWith(suffix));
                        }))
                .then(addTwoInZeroOutArgument("contains",
                        "sourcePath",
                        "value",
                        "substringSourcePath",
                        "substringValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var substring = getNbtValueAsString(scc.sources[1]);
                            return toInt(src.contains(substring));
                        }))
                .then(addTwoInZeroOutArgument("matches",
                        "sourcePath",
                        "value",
                        "regexSourcePath",
                        "regexValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var regex = getNbtValueAsString(scc.sources[1]);
                            return toInt(src.matches(regex));
                        }))
                .then(addThreeInOneOutArgument("replace",
                        "sourcePath",
                        "value",
                        "targetSourcePath",
                        "targetValue",
                        "replacementSourcePath",
                        "replacementValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 3);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var target = getNbtValueAsString(scc.sources[1]);
                            var replacement = getNbtValueAsString(scc.sources[2]);
                            setTarget(ctx, scc, NbtString.of(src.replace(target, replacement)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addThreeInOneOutArgument("replaceAll",
                        "sourcePath",
                        "value",
                        "regexSourcePath",
                        "regexValue",
                        "replacementSourcePath",
                        "replacementValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 3);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var regex = getNbtValueAsString(scc.sources[1]);
                            var replacement = getNbtValueAsString(scc.sources[2]);
                            setTarget(ctx, scc, NbtString.of(src.replaceAll(regex, replacement)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addThreeInOneOutArgument("replaceFirst",
                        "sourcePath",
                        "value",
                        "regexSourcePath",
                        "regexValue",
                        "replacementSourcePath",
                        "replacementValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 3);
                            var src = getNbtValueAsString(scc.sources[0]);
                            var regex = getNbtValueAsString(scc.sources[1]);
                            var replacement = getNbtValueAsString(scc.sources[2]);
                            setTarget(ctx, scc, NbtString.of(src.replaceFirst(regex, replacement)));
                            return SINGLE_SUCCESS;
                        }));
        dispatcher.register(stringCommand);
    }

    public static NbtElement getNbtElement(Pair<NbtElement, NbtPathArgumentType.NbtPath> pair) throws CommandSyntaxException {
        var l = pair.getLeft();
        var r = pair.getRight();
        return (r == null ? l : r.get(l).get(0));
    }

    public static int getNbtValueAsInt(Pair<NbtElement, NbtPathArgumentType.NbtPath> pair) throws CommandSyntaxException {
        var e = getNbtElement(pair);
        if (e instanceof AbstractNbtNumber num) {
            return num.intValue();
        } else {
            throw EXPECTED_INT_EXCEPTION.create(e.getNbtType().getCrashReportName());
        }
    }

    public static String getNbtValueAsString(Pair<NbtElement, NbtPathArgumentType.NbtPath> pair) throws CommandSyntaxException {
        return getNbtElement(pair).asString();
    }

    public static int toInt(boolean bool) {
        return bool ? 1 : 0;
    }

    public static int convertIndex(int index, String s) {
        return index >= 0 ? index : s.length() + index;
    }

    public static String escapeRegex(String s) {
        return SPECIAL_REGEX_CHARS.matcher(s).replaceAll("\\\\$0");
    }

    public static void checkIndex(int index, CharSequence count) throws CommandSyntaxException {
        if (index < 0) {
            throw INTEGER_TOO_LOW.create(index, 0);
        } else if (index >= count.length()) {
            throw INTEGER_TOO_HIGH.create(index, count.length());
        }
    }

    public static void checkInt(int index, int min, int max) throws CommandSyntaxException {
        if (index < min) {
            throw INTEGER_TOO_LOW.create(index, min);
        } else if (index > max) {
            throw INTEGER_TOO_HIGH.create(index, max);
        }
    }

    public static void checkNotBelowZero(int i) throws CommandSyntaxException {
        if (i < 0) {
            throw INTEGER_TOO_LOW.create(i, 0);
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
        var results = new ArrayList<MatchResult>();
        while (matcher.find(start)) {
            results.add(matcher.toMatchResult());
            start = matcher.start() + 1;
        }
        return results;
    }

    private static int nullablePairArraySize(Pair<?, ?>[] array) {
        return array == null ? -1 : array.length;
    }

    private static void checkArgumentCount(Pair<?, ?>[] sources, int count) throws CommandSyntaxException {
        if (nullablePairArraySize(sources) < count) {
            throw ARGUMENT_TOO_FEW_EXCEPTION.create();
        }
    }

    private static Set<Character> createTrimCharsSet(StringCommandContext scc) throws CommandSyntaxException {
        if (scc.sources.length <= 1) {
            return null;
        }
        var tc = getNbtElement(scc.sources[1]);
        var trimChars = new HashSet<Character>();
        if (tc instanceof NbtString str) {
            for (char c : str.asString().toCharArray()) {
                trimChars.add(c);
            }
        } else if (tc instanceof NbtList list && list.getHeldType() == NbtElement.STRING_TYPE) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                String str = list.get(i).asString();
                if (str.length() != 1) {
                    throw INVALID_CHAR_ARRAY_EXCEPTION.create(list);
                }
                trimChars.add(str.charAt(0));
            }
        } else {
            throw EXPECTED_STRING_LIST_EXCEPTION.create(tc);
        }
        return trimChars;
    }

    private static void setTarget(CommandContext<ServerCommandSource> ctx, StringCommandContext scc, NbtElement element) throws CommandSyntaxException {
        scc.targetPath.put(scc.targetRoot, element);
        scc.target.setNbt(scc.targetRoot);
        ctx.getSource().sendFeedback(scc.target.feedbackModify(), true);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addTarget(
            ArgumentBuilder<ServerCommandSource, ?> argument,
            BiFunction<ArgumentBuilder<ServerCommandSource, ?>, DataCommand.ObjectType, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
        for (DataCommand.ObjectType target : DataCommand.TARGET_OBJECT_TYPES) {
            target.addArgumentsToBuilder(argument,
                    builder -> builder.then(argumentAdder.apply(
                            argument("targetPath", NbtPathArgumentType.nbtPath()),
                            target)));
        }
        return argument;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addSource(
            ArgumentBuilder<ServerCommandSource, ?> argument,
            String sourcePathName,
            String valueName,
            BiFunction<ArgumentBuilder<ServerCommandSource, ?>, SourceGetter, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
        for (DataCommand.ObjectType source : DataCommand.SOURCE_OBJECT_TYPES) {
            argument.then(source.addArgumentsToBuilder(literal("from"),
                    innerBuilder -> argumentAdder.apply(innerBuilder, new FromWithoutPathSourceGetter(source))
                            .then(argumentAdder.apply(argument(sourcePathName, NbtPathArgumentType.nbtPath()),
                                    new FromWithPathSourceGetter(source, sourcePathName)))));
        }
        argument.then(literal("value")
                .then(argumentAdder.apply(argument(valueName, nbtElement()), new ValueSourceGetter(valueName))));
        return argument;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addOneInZeroOutArgument(
            String name,
            StringCommandExec<ServerCommandSource> command) {
        return addSource(literal(name),
                "sourcePath",
                "value",
                (builder, source) -> builder.executes(
                        ctx -> command.apply(ctx, new StringCommandContext(
                                null,
                                null,
                                source.getSourceElement(ctx),
                                source.getSourcePath(ctx)))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addOneInOneOutArgument(
            String name,
            StringCommandExec<ServerCommandSource> command) {
        return addTarget(literal(name),
                (builder, target) -> addSource(builder,
                        "sourcePath",
                        "value",
                        (innerBuilder, source) -> innerBuilder.executes(
                                ctx -> command.apply(ctx, new StringCommandContext(
                                        target.getObject(ctx),
                                        NbtPathArgumentType.getNbtPath(ctx, "targetPath"),
                                        source.getSourceElement(ctx),
                                        source.getSourcePath(ctx))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addOneInOneOptionalInOneOutArgument(
            String name,
            String sourcePathName0,
            String valueName0,
            String sourcePathName1,
            String valueName1,
            StringCommandExec<ServerCommandSource> command) {
        return addTarget(literal(name),
                (builder, target) -> addSource(builder,
                        sourcePathName0,
                        valueName0,
                        (innerBuilder, source0) -> addSource(
                                innerBuilder.executes(ctx -> command.apply(ctx, new StringCommandContext(
                                        target.getObject(ctx),
                                        NbtPathArgumentType.getNbtPath(ctx, "targetPath"),
                                        source0.getSourceElement(ctx),
                                        source0.getSourcePath(ctx)))),
                                sourcePathName1,
                                valueName1,
                                (inner2Builder, source1) -> inner2Builder.executes(
                                        ctx -> command.apply(ctx, new StringCommandContext(
                                                target.getObject(ctx),
                                                NbtPathArgumentType.getNbtPath(ctx, "targetPath"),
                                                new Pair[]{
                                                        source0.CreatePair(ctx),
                                                        source1.CreatePair(ctx)
                                                }))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addTwoInZeroOutArgument(
            String name,
            String sourcePathName0,
            String valueName0,
            String sourcePathName1,
            String valueName1,
            StringCommandExec<ServerCommandSource> command) {
        return addSource(literal(name),
                sourcePathName0,
                valueName0,
                (builder, source0) -> addSource(builder,
                        sourcePathName1,
                        valueName1,
                        (innerBuilder, source1) -> innerBuilder.executes(
                                ctx -> command.apply(ctx, new StringCommandContext(
                                        null,
                                        null,
                                        new Pair[]{
                                                source0.CreatePair(ctx),
                                                source1.CreatePair(ctx)
                                        })))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addTwoInOneOutArgument(
            String name,
            String sourcePathName0,
            String valueName0,
            String sourcePathName1,
            String valueName1,
            StringCommandExec<ServerCommandSource> command) {
        return addTarget(literal(name),
                (builder, target) -> addSource(builder,
                        sourcePathName0,
                        valueName0,
                        (innerBuilder, source0) -> addSource(innerBuilder,
                                sourcePathName1,
                                valueName1,
                                (inner2Builder, source1) -> inner2Builder.executes(
                                        ctx -> command.apply(ctx, new StringCommandContext(
                                                target.getObject(ctx),
                                                NbtPathArgumentType.getNbtPath(ctx, "targetPath"),
                                                new Pair[]{
                                                        source0.CreatePair(ctx),
                                                        source1.CreatePair(ctx)
                                                }))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addTwoInOneOptionalInZeroOutArgument(
            String name,
            String sourcePathName0,
            String valueName0,
            String sourcePathName1,
            String valueName1,
            String sourcePathName2,
            String valueName2,
            StringCommandExec<ServerCommandSource> command) {
        return addSource(literal(name),
                sourcePathName0,
                valueName0,
                (builder, source0) -> addSource(builder,
                        sourcePathName1,
                        valueName1,
                        (inner1Builder, source1) -> addSource(
                                inner1Builder.executes(ctx -> command.apply(ctx, new StringCommandContext(
                                        null,
                                        null,
                                        new Pair[]{
                                                source0.CreatePair(ctx),
                                                source1.CreatePair(ctx)
                                        }))),
                                sourcePathName2,
                                valueName2,
                                (inner2Builder, source2) -> inner2Builder.executes(
                                        ctx -> command.apply(ctx, new StringCommandContext(
                                                null,
                                                null,
                                                new Pair[]{
                                                        source0.CreatePair(ctx),
                                                        source1.CreatePair(ctx),
                                                        source2.CreatePair(ctx)
                                                }))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addTwoInOneOptionalInOneOutArgument(
            String name,
            String sourcePathName0,
            String valueName0,
            String sourcePathName1,
            String valueName1,
            String sourcePathName2,
            String valueName2,
            StringCommandExec<ServerCommandSource> command) {
        return addTarget(literal(name),
                (builder, target) -> addSource(builder,
                        sourcePathName0,
                        valueName0,
                        (innerBuilder, source0) -> addSource(innerBuilder,
                                sourcePathName1,
                                valueName1,
                                (inner2Builder, source1) -> addSource(
                                        inner2Builder.executes(ctx -> command.apply(ctx, new StringCommandContext(
                                                target.getObject(ctx),
                                                NbtPathArgumentType.getNbtPath(ctx, "targetPath"),
                                                new Pair[]{
                                                        source0.CreatePair(ctx),
                                                        source1.CreatePair(ctx)
                                                }))),
                                        sourcePathName2,
                                        valueName2,
                                        (inner3Builder, source2) -> inner3Builder.executes(
                                                ctx -> command.apply(ctx, new StringCommandContext(
                                                        target.getObject(ctx),
                                                        NbtPathArgumentType.getNbtPath(ctx, "targetPath"),
                                                        new Pair[]{
                                                                source0.CreatePair(ctx),
                                                                source1.CreatePair(ctx),
                                                                source2.CreatePair(ctx)
                                                        })))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addThreeInOneOutArgument(
            String name,
            String sourcePathName0,
            String valueName0,
            String sourcePathName1,
            String valueName1,
            String sourcePathName2,
            String valueName2,
            StringCommandExec<ServerCommandSource> command) {
        return addTarget(literal(name),
                (builder, target) -> addSource(builder,
                        sourcePathName0,
                        valueName0,
                        (innerBuilder, source0) -> addSource(innerBuilder,
                                sourcePathName1,
                                valueName1,
                                (inner2Builder, source1) -> addSource(inner2Builder,
                                        sourcePathName2,
                                        valueName2,
                                        (inner3Builder, source2) -> inner3Builder.executes(
                                                ctx -> command.apply(ctx, new StringCommandContext(
                                                        target.getObject(ctx),
                                                        NbtPathArgumentType.getNbtPath(ctx, "targetPath"),
                                                        new Pair[]{
                                                                source0.CreatePair(ctx),
                                                                source1.CreatePair(ctx),
                                                                source2.CreatePair(ctx)
                                                        })))))));
    }

    @FunctionalInterface
    public interface StringCommandExec<S> {
        int apply(CommandContext<S> ctx, StringCommandContext scc) throws CommandSyntaxException;
    }

    public abstract static class SourceGetter {
        public abstract NbtElement getSourceElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

        public abstract NbtPathArgumentType.NbtPath getSourcePath(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

        public Pair<NbtElement, NbtPathArgumentType.NbtPath> CreatePair(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return new Pair<>(getSourceElement(context), getSourcePath(context));
        }
    }

    public static class FromWithoutPathSourceGetter extends SourceGetter {
        private final DataCommand.ObjectType source;

        public FromWithoutPathSourceGetter(DataCommand.ObjectType source) {
            this.source = source;
        }

        public NbtElement getSourceElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return source.getObject(context).getNbt();
        }

        public NbtPathArgumentType.NbtPath getSourcePath(CommandContext<ServerCommandSource> context) {
            return null;
        }
    }

    public static class FromWithPathSourceGetter extends SourceGetter {
        private final DataCommand.ObjectType source;
        private final String sourcePathName;

        public FromWithPathSourceGetter(DataCommand.ObjectType source, String sourcePathName) {
            this.source = source;
            this.sourcePathName = sourcePathName;
        }

        public NbtElement getSourceElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return source.getObject(context).getNbt();
        }

        public NbtPathArgumentType.NbtPath getSourcePath(CommandContext<ServerCommandSource> context) {
            return NbtPathArgumentType.getNbtPath(context, sourcePathName);
        }
    }

    public static class ValueSourceGetter extends SourceGetter {
        private final String valueName;

        public ValueSourceGetter(String valueName) {
            this.valueName = valueName;
        }

        public NbtElement getSourceElement(CommandContext<ServerCommandSource> context) {
            return NbtElementArgumentType.getNbtElement(context, valueName);
        }

        public NbtPathArgumentType.NbtPath getSourcePath(CommandContext<ServerCommandSource> context) {
            return null;
        }
    }
}
