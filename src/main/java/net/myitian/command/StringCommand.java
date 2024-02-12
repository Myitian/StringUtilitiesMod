package net.myitian.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.command.arguments.NbtPathArgumentType;
import net.minecraft.command.arguments.NbtTagArgumentType;
import net.minecraft.nbt.*;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.myitian.StringExtension;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class StringCommand {
    public static final DynamicCommandExceptionType EXPECTED_LIST_EXCEPTION =
            new DynamicCommandExceptionType(nbt -> new TranslatableText("commands.data.modify.expected_list", nbt));
    public static final SimpleCommandExceptionType TOO_FEW_ARGUMENT_EXCEPTION = // [Should not show]
            new SimpleCommandExceptionType(new TranslatableText("commands.string-utilities.string.too_few_arguments"));
    public static final DynamicCommandExceptionType INVALID_CHAR_ARRAY_EXCEPTION = // Invalid char array: %s
            new DynamicCommandExceptionType(name -> new TranslatableText("commands.string-utilities.string.invalid_char_array", name));
    public static final DynamicCommandExceptionType EXPECTED_STRING_LIST_EXCEPTION = // Invalid argument type: %s, expected STRING
            new DynamicCommandExceptionType(name -> new TranslatableText("commands.string-utilities.string.unexpected_type", name, StringTag.READER.getCrashReportName()));
    public static final DynamicCommandExceptionType EXPECTED_INT_ARRAY_EXCEPTION = // Invalid argument type: %s, expected INT[]
            new DynamicCommandExceptionType(name -> new TranslatableText("commands.string-utilities.string.unexpected_type", name, IntArrayTag.READER.getCrashReportName()));
    public static final DynamicCommandExceptionType EXPECTED_INT_EXCEPTION = // Invalid argument type: %s, expected INT
            new DynamicCommandExceptionType(name -> new TranslatableText("commands.string-utilities.string.unexpected_type", name, IntTag.READER.getCrashReportName()));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> stringCommand = literal("string")
                .requires(source -> source.hasPermissionLevel(2))
                .then(addOneInZeroOutArgument("isBlank", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    return toInt(StringExtension.isBlank(src));
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
                    setTarget(ctx, scc, StringTag.of(src));
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
                    setTarget(ctx, scc, StringTag.of(stringBuilder.toString()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("escapeNbt", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, StringTag.of(StringTag.escape(src)));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("escapeRegex", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, StringTag.of(StringExtension.escapeRegex(src)));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("toLowerCase", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, StringTag.of(src.toLowerCase()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("toUpperCase", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, StringTag.of(src.toUpperCase()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("strip", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, StringTag.of(StringExtension.strip(src)));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("stripLeading", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, StringTag.of(StringExtension.stripLeading(src)));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("stripTrailing", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    setTarget(ctx, scc, StringTag.of(StringExtension.stripTrailing(src)));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("toCharArray", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    ListTag list = new ListTag();
                    for (char c : src.toCharArray()) {
                        list.add(StringTag.of(Character.toString(c)));
                    }
                    setTarget(ctx, scc, list);
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("toCodePointStrings", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    int[] cps = src.codePoints().toArray();
                    ListTag list = new ListTag();
                    for (int i = 0; i < cps.length; i++) {
                        list.add(StringTag.of(new String(cps, i, 1)));
                    }
                    setTarget(ctx, scc, list);
                    return list.size();
                }))
                .then(addOneInOneOutArgument("toCodePoints", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    String src = getNbtValueAsString(scc.sources[0]);
                    IntArrayTag arr = new IntArrayTag(src.codePoints().toArray());
                    setTarget(ctx, scc, arr);
                    return arr.size();
                }))
                .then(addOneInOneOutArgument("fromCodePoints", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    Tag element = getNbtElement(scc.sources[0]);
                    String result;
                    if (!(element instanceof AbstractListTag<?>)) {
                        throw EXPECTED_LIST_EXCEPTION.create(element);
                    }
                    AbstractListTag<?> list = (AbstractListTag<?>) element;
                    if (list.isEmpty()) {
                        result = "";
                    } else {
                        int size = list.size();
                        StringBuilder sb = new StringBuilder();
                        if (!(list.get(0) instanceof AbstractNumberTag)) {
                            throw EXPECTED_INT_ARRAY_EXCEPTION.create(element.getReader().getCrashReportName());
                        }
                        for (int i = 0; i < size; i++) {
                            sb.appendCodePoint(((AbstractNumberTag) list.get(i)).getInt());
                        }
                        result = sb.toString();
                    }
                    setTarget(ctx, scc, StringTag.of(result));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOutArgument("concat", (ctx, scc) -> {
                    checkArgumentCount(scc.sources, 1);
                    Tag element = getNbtElement(scc.sources[0]);
                    if (!(element instanceof AbstractListTag<?>)) {
                        throw EXPECTED_LIST_EXCEPTION.create(element);
                    }
                    AbstractListTag<?> list = (AbstractListTag<?>) element;
                    String[] strings = new String[list.size()];
                    int len = 0;
                    for (int i = 0; i < strings.length; i++) {
                        len += (strings[i] = list.get(i).asString()).length();
                    }
                    StringBuilder sb = new StringBuilder(len);
                    for (String string : strings) {
                        sb.append(string);
                    }
                    setTarget(ctx, scc, StringTag.of(sb.toString()));
                    return SINGLE_SUCCESS;
                }))
                .then(addOneInOneOptionalInOneOutArgument("trim",
                        "sourcePath",
                        "value",
                        "trimCharsSourcePath",
                        "trimCharsValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 1);
                            String src = getNbtValueAsString(scc.sources[0]);
                            Set<Character> trimChars = createTrimCharsSet(scc);
                            setTarget(ctx, scc, StringTag.of(StringExtension.trim(src, trimChars)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addOneInOneOptionalInOneOutArgument("trimStart",
                        "sourcePath",
                        "value",
                        "trimCharsSourcePath",
                        "trimCharsValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 1);
                            String src = getNbtValueAsString(scc.sources[0]);
                            Set<Character> trimChars = createTrimCharsSet(scc);
                            setTarget(ctx, scc, StringTag.of(StringExtension.trimStart(src, trimChars)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addOneInOneOptionalInOneOutArgument("trimEnd",
                        "sourcePath",
                        "value",
                        "trimCharsSourcePath",
                        "trimCharsValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 1);
                            String src = getNbtValueAsString(scc.sources[0]);
                            Set<Character> trimChars = createTrimCharsSet(scc);
                            setTarget(ctx, scc, StringTag.of(StringExtension.trimEnd(src, trimChars)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOutArgument("at",
                        "sourcePath",
                        "value",
                        "indexSourcePath",
                        "indexValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            String src = getNbtValueAsString(scc.sources[0]);
                            int i = StringExtension.convertAndCheckIndex(getNbtValueAsInt(scc.sources[1]), src);
                            setTarget(ctx, scc, StringTag.of(Character.toString(src.charAt(i))));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOutArgument("repeat",
                        "sourcePath",
                        "value",
                        "countSourcePath",
                        "countValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            String src = getNbtValueAsString(scc.sources[0]);
                            int r = getNbtValueAsInt(scc.sources[1]);
                            StringExtension.checkNotBelowZero(r);
                            setTarget(ctx, scc, StringTag.of(StringExtension.repeat(src, r)));
                            return SINGLE_SUCCESS;
                        }))
                .then(addTwoInOneOutArgument("matchesAll",
                        "sourcePath",
                        "value",
                        "patternSourcePath",
                        "patternValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            String src = getNbtValueAsString(scc.sources[0]);
                            String p = getNbtValueAsString(scc.sources[1]);
                            Pattern regex = Pattern.compile(p);
                            Matcher matcher = regex.matcher(src);
                            ListTag list = new ListTag();
                            for (MatchResult r : StringExtension.matchesAll(matcher)) {
                                CompoundTag nbt = new CompoundTag();
                                nbt.putInt("start", r.start());
                                nbt.putInt("end", r.end());
                                list.add(nbt);
                            }
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            String p = getNbtValueAsString(scc.sources[1]);
                            Pattern regex = Pattern.compile(p);
                            Matcher matcher = regex.matcher(src);
                            ListTag list = new ListTag();
                            for (MatchResult r : StringExtension.matchesAllFully(matcher)) {
                                CompoundTag nbt = new CompoundTag();
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
                            String delimiter = getNbtValueAsString(scc.sources[0]);
                            Tag element = getNbtElement(scc.sources[1]);
                            String result;
                            if (element instanceof StringTag) {
                                char[] chars = element.asString().toCharArray();
                                StringBuilder sb = new StringBuilder((chars.length - 1) * delimiter.length() + 1);
                                sb.append(chars[0]);
                                for (int i = 1; i < chars.length; ) {
                                    sb.append(delimiter).append(chars[i++]);
                                }
                                result = sb.toString();
                            } else if (element instanceof AbstractListTag<?>) {
                                AbstractListTag<?> list = (AbstractListTag<?>) element;
                                String[] strings = new String[list.size()];
                                int len = 0;
                                for (int i = 0; i < strings.length; i++) {
                                    len += (strings[i] = list.get(i).asString()).length();
                                }
                                len += (strings.length - 1) * delimiter.length();
                                StringBuilder sb = new StringBuilder(len);
                                sb.append(strings[0]);
                                for (int i = 1; i < strings.length; ) {
                                    sb.append(delimiter).append(strings[i++]);
                                }
                                result = sb.toString();
                            } else {
                                throw EXPECTED_LIST_EXCEPTION.create(element);
                            }
                            setTarget(ctx, scc, StringTag.of(result));
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
                            String s0 = getNbtValueAsString(scc.sources[0]);
                            String s1 = getNbtValueAsString(scc.sources[1]);
                            String result = s0 + s1;
                            if (scc.sources.length > 2) {
                                result += getNbtValueAsString(scc.sources[2]);
                            }
                            setTarget(ctx, scc, StringTag.of(result));
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            int begin = StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(scc.sources[1]), src);
                            String result;
                            if (scc.sources.length > 2) {
                                int end = getNbtValueAsInt(scc.sources[2]);
                                StringExtension.checkInt(end, begin - src.length(), -1, begin, src.length());
                                result = src.substring(begin, StringExtension.convertIndex(end, src));
                            } else {
                                result = src.substring(begin);
                            }
                            setTarget(ctx, scc, StringTag.of(result));
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            int begin = StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(scc.sources[1]), src);
                            String result;
                            if (scc.sources.length > 2) {
                                int length = getNbtValueAsInt(scc.sources[2]);
                                StringExtension.checkInt(length, 0, src.length() - begin);
                                result = src.substring(begin, begin + length);
                            } else {
                                result = src.substring(begin);
                            }
                            setTarget(ctx, scc, StringTag.of(result));
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            String sep = getNbtValueAsString(scc.sources[1]);
                            String[] result;
                            if (scc.sources.length > 2) {
                                int i = getNbtValueAsInt(scc.sources[2]);
                                StringExtension.checkNotBelowZero(i);
                                result = src.split(sep, i);
                            } else {
                                result = src.split(sep);
                            }
                            ListTag list = new ListTag();
                            for (String s : result) {
                                list.add(StringTag.of(s));
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
                                return src.indexOf(sub, StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(scc.sources[2]), src));
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
                                return src.lastIndexOf(sub, StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(scc.sources[2]), src));
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            String prefix = getNbtValueAsString(scc.sources[1]);
                            boolean result;
                            if (scc.sources.length > 2) {
                                result = src.startsWith(prefix, StringExtension.convertAndCheckIndexWider(getNbtValueAsInt(scc.sources[2]), src));
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            String suffix = getNbtValueAsString(scc.sources[1]);
                            return toInt(src.endsWith(suffix));
                        }))
                .then(addTwoInZeroOutArgument("contains",
                        "sourcePath",
                        "value",
                        "substringSourcePath",
                        "substringValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            String src = getNbtValueAsString(scc.sources[0]);
                            String substring = getNbtValueAsString(scc.sources[1]);
                            return toInt(src.contains(substring));
                        }))
                .then(addTwoInZeroOutArgument("matches",
                        "sourcePath",
                        "value",
                        "regexSourcePath",
                        "regexValue",
                        (ctx, scc) -> {
                            checkArgumentCount(scc.sources, 2);
                            String src = getNbtValueAsString(scc.sources[0]);
                            String regex = getNbtValueAsString(scc.sources[1]);
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            String target = getNbtValueAsString(scc.sources[1]);
                            String replacement = getNbtValueAsString(scc.sources[2]);
                            setTarget(ctx, scc, StringTag.of(src.replace(target, replacement)));
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            String regex = getNbtValueAsString(scc.sources[1]);
                            String replacement = getNbtValueAsString(scc.sources[2]);
                            setTarget(ctx, scc, StringTag.of(src.replaceAll(regex, replacement)));
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
                            String src = getNbtValueAsString(scc.sources[0]);
                            String regex = getNbtValueAsString(scc.sources[1]);
                            String replacement = getNbtValueAsString(scc.sources[2]);
                            setTarget(ctx, scc, StringTag.of(src.replaceFirst(regex, replacement)));
                            return SINGLE_SUCCESS;
                        }));
        dispatcher.register(stringCommand);
    }

    public static Tag getNbtElement(Pair<Tag, NbtPathArgumentType.NbtPath> pair) throws CommandSyntaxException {
        Tag l = pair.getLeft();
        NbtPathArgumentType.NbtPath r = pair.getRight();
        return (r == null ? l : r.get(l).get(0));
    }

    public static int getNbtValueAsInt(Pair<Tag, NbtPathArgumentType.NbtPath> pair) throws CommandSyntaxException {
        Tag e = getNbtElement(pair);
        if (e instanceof AbstractNumberTag) {
            return ((AbstractNumberTag) e).getInt();
        } else {
            throw EXPECTED_INT_EXCEPTION.create(e.getReader().getCrashReportName());
        }
    }

    public static String getNbtValueAsString(Pair<Tag, NbtPathArgumentType.NbtPath> pair) throws CommandSyntaxException {
        return getNbtElement(pair).asString();
    }

    public static int toInt(boolean bool) {
        return bool ? 1 : 0;
    }

    private static int nullablePairArraySize(Pair<?, ?>[] array) {
        return array == null ? -1 : array.length;
    }

    private static void checkArgumentCount(Pair<?, ?>[] sources, int count) throws CommandSyntaxException {
        if (nullablePairArraySize(sources) < count) {
            throw TOO_FEW_ARGUMENT_EXCEPTION.create();
        }
    }

    private static Set<Character> createTrimCharsSet(StringCommandContext scc) throws CommandSyntaxException {
        if (scc.sources.length <= 1) {
            return null;
        }
        Tag tc = getNbtElement(scc.sources[1]);
        HashSet<Character> trimChars = new HashSet<>();
        ListTag list;
        if (tc instanceof StringTag) {
            for (char c : tc.asString().toCharArray()) {
                trimChars.add(c);
            }
        } else if (tc instanceof ListTag && (list = (ListTag) tc).getElementType() == NbtType.STRING) {
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

    private static void setTarget(CommandContext<ServerCommandSource> ctx, StringCommandContext scc, Tag element) throws CommandSyntaxException {
        scc.targetPath.put(scc.targetRoot, () -> element);
        scc.target.setTag(scc.targetRoot);
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
                .then(argumentAdder.apply(argument(valueName, NbtTagArgumentType.nbtTag()), new ValueSourceGetter(valueName))));
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
        public abstract Tag getSourceElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

        public abstract NbtPathArgumentType.NbtPath getSourcePath(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

        public Pair<Tag, NbtPathArgumentType.NbtPath> CreatePair(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return new Pair<>(getSourceElement(context), getSourcePath(context));
        }
    }

    public static class FromWithoutPathSourceGetter extends SourceGetter {
        private final DataCommand.ObjectType source;

        public FromWithoutPathSourceGetter(DataCommand.ObjectType source) {
            this.source = source;
        }

        public Tag getSourceElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return source.getObject(context).getTag();
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

        public Tag getSourceElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return source.getObject(context).getTag();
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

        public Tag getSourceElement(CommandContext<ServerCommandSource> context) {
            return NbtTagArgumentType.getTag(context, valueName);
        }

        public NbtPathArgumentType.NbtPath getSourcePath(CommandContext<ServerCommandSource> context) {
            return null;
        }
    }
}
