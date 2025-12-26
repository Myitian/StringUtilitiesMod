package net.myitian.string_utilities_mod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.*;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.util.Tuple;
import net.myitian.string_utilities_mod.commands.sources.FromDirectNbtSource;
import net.myitian.string_utilities_mod.commands.sources.FromPathNbtSource;
import net.myitian.string_utilities_mod.commands.sources.NbtSource;
import net.myitian.string_utilities_mod.commands.sources.ValueNbtSource;

import java.util.function.BiFunction;

public final class StringCommandCore {
    public static final DynamicCommandExceptionType EXPECTED_LIST_EXCEPTION =
        new DynamicCommandExceptionType(nbt -> Component.translatable(
            "commands.data.modify.expected_list",
            nbt));
    public static final SimpleCommandExceptionType TOO_FEW_ARGUMENT_EXCEPTION =
        new SimpleCommandExceptionType(Component.translatableWithFallback(
            "commands.string_utilities.string.too_few_arguments",
            "Too few arguments"));
    public static final DynamicCommandExceptionType INVALID_CHAR_ARRAY_EXCEPTION =
        new DynamicCommandExceptionType(name -> Component.translatableWithFallback(
            "commands.string_utilities.string.invalid_char_array",
            "Invalid char array: %s",
            name));
    public static final DynamicCommandExceptionType EXPECTED_STRING_EXCEPTION =
        new DynamicCommandExceptionType(name -> Component.translatableWithFallback(
            "commands.string_utilities.string.unexpected_type",
            "Invalid argument type: %s, expected %s",
            name,
            StringTag.TYPE.getPrettyName()));
    public static final DynamicCommandExceptionType EXPECTED_INT_ARRAY_EXCEPTION =
        new DynamicCommandExceptionType(name -> Component.translatableWithFallback(
            "commands.string_utilities.string.unexpected_type",
            "Invalid argument type: %s, expected %s",
            name,
            IntArrayTag.TYPE.getPrettyName()));
    public static final DynamicCommandExceptionType EXPECTED_INT_EXCEPTION =
        new DynamicCommandExceptionType(name -> Component.translatableWithFallback(
            "commands.string_utilities.string.unexpected_type",
            "Invalid argument type: %s, expected %s",
            name,
            IntTag.TYPE.getPrettyName()));

    public static final Dynamic2CommandExceptionType INTEGER_TOO_LOW_EXCEPTION =
        new Dynamic2CommandExceptionType((found, min) -> Component.translatable(
            "argument.integer.low",
            min,
            found));
    public static final Dynamic2CommandExceptionType INTEGER_TOO_HIGH_EXCEPTION =
        new Dynamic2CommandExceptionType((found, max) -> Component.translatable(
            "argument.integer.big",
            max,
            found));
    public static final Dynamic3CommandExceptionType INTEGER_NOT_IN_RANGE_2_EXCEPTION =
        new Dynamic3CommandExceptionType((found, range0, range1) -> Component.translatableWithFallback(
            "argument.string_utilities.integer.not_in_range",
            "Integer %s is not in range %s and %s",
            found,
            range0,
            range1));
    public static final SimpleCommandExceptionType STRING_EMPTY_EXCEPTION =
        new SimpleCommandExceptionType(Component.translatableWithFallback(
            "argument.string_utilities.string.empty",
            "String cannot be empty"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var stringCommand = Commands.literal("string")
            .requires(source -> source.hasPermission(2))
            .then(addOneInZeroOutArgument("isBlank", StringCommands::isBlank))
            .then(addOneInZeroOutArgument("isEmpty", StringCommands::isEmpty))
            .then(addOneInZeroOutArgument("length", StringCommands::length))
            .then(addOneInOneOutArgument("toString", StringCommands::toString))
            .then(addOneInOneOutArgument("escape", StringCommands::escape))
            .then(addOneInOneOutArgument("escapeNbt", StringCommands::escapeNbt))
            .then(addOneInOneOutArgument("escapeRegex", StringCommands::escapeRegex))
            .then(addOneInOneOutArgument("toLowerCase", StringCommands::toLowerCase))
            .then(addOneInOneOutArgument("toUpperCase", StringCommands::toUpperCase))
            .then(addOneInOneOutArgument("toLowerCaseInvariant", StringCommands::toLowerCaseInvariant))
            .then(addOneInOneOutArgument("toUpperCaseInvariant", StringCommands::toUpperCaseInvariant))
            .then(addOneInOneOutArgument("strip", StringCommands::strip))
            .then(addOneInOneOutArgument("stripLeading", StringCommands::stripLeading))
            .then(addOneInOneOutArgument("stripTrailing", StringCommands::stripTrailing))
            .then(addOneInOneOutArgument("toCharArray", StringCommands::toCharArray))
            .then(addOneInOneOutArgument("toCodePointStrings", StringCommands::toCodePointStrings))
            .then(addOneInOneOutArgument("toCodePoints", StringCommands::toCodePoints))
            .then(addOneInOneOutArgument("fromCodePoints", StringCommands::fromCodePoints))
            .then(addOneInOneOutArgument("breakCharacters", StringCommands::breakCharacters))
            .then(addOneInOneOutArgument("breakCharactersInvariant", StringCommands::breakCharactersInvariant))
            .then(addOneInOneOutArgument("breakLines", StringCommands::breakLines))
            .then(addOneInOneOutArgument("breakLinesInvariant", StringCommands::breakLinesInvariant))
            .then(addOneInOneOutArgument("breakSentences", StringCommands::breakSentences))
            .then(addOneInOneOutArgument("breakSentencesInvariant", StringCommands::breakSentencesInvariant))
            .then(addOneInOneOutArgument("breakWords", StringCommands::breakWords))
            .then(addOneInOneOutArgument("breakWordsInvariant", StringCommands::breakWordsInvariant))
            .then(addOneInOneOutArgument("concat", StringCommands::concat))
            .then(addOneInOneOptionalInOneOutArgument("toJson",
                "sourcePath",
                "value",
                "isPrettyPrintingSourcePath",
                "isPrettyPrinting",
                StringCommands::toJson))
            .then(addOneInOneOptionalInOneOutArgument("trim",
                "sourcePath",
                "value",
                "trimCharsSourcePath",
                "trimCharsValue",
                StringCommands::trim))
            .then(addOneInOneOptionalInOneOutArgument("trimStart",
                "sourcePath",
                "value",
                "trimCharsSourcePath",
                "trimCharsValue",
                StringCommands::trimStart))
            .then(addOneInOneOptionalInOneOutArgument("trimEnd",
                "sourcePath",
                "value",
                "trimCharsSourcePath",
                "trimCharsValue",
                StringCommands::trimEnd))
            .then(addTwoInOneOutArgument("at",
                "sourcePath",
                "value",
                "indexSourcePath",
                "indexValue",
                StringCommands::at))
            .then(addTwoInOneOutArgument("codePointAt",
                "sourcePath",
                "value",
                "indexSourcePath",
                "indexValue",
                StringCommands::codePointAt))
            .then(addTwoInOneOutArgument("codePointBefore",
                "sourcePath",
                "value",
                "indexSourcePath",
                "indexValue",
                StringCommands::codePointBefore))
            .then(addTwoInOneOutArgument("repeat",
                "sourcePath",
                "value",
                "countSourcePath",
                "countValue",
                StringCommands::repeat))
            .then(addTwoInOneOutArgument("matchesAll",
                "sourcePath",
                "value",
                "patternSourcePath",
                "patternValue",
                StringCommands::matchesAll))
            .then(addTwoInOneOutArgument("matchesAllFully",
                "sourcePath",
                "value",
                "countSourcePath",
                "countValue",
                StringCommands::matchesAllFully))
            .then(addTwoInOneOutArgument("join",
                "delimiterSourcePath",
                "delimiterValue",
                "elementsSourcePath",
                "elementsValue",
                StringCommands::join))
            .then(addTwoInOneOptionalInOneOutArgument("concat2",
                "sourcePath0",
                "value0",
                "sourcePath1",
                "value1",
                "sourcePath2",
                "value2",
                StringCommands::concat2))
            .then(addTwoInOneOptionalInOneOutArgument("substring",
                "sourcePath",
                "value",
                "beginIndexSourcePath",
                "beginIndexValue",
                "endIndexSourcePath",
                "endIndexValue",
                StringCommands::substring))
            .then(addTwoInOneOptionalInOneOutArgument("substring2",
                "sourcePath",
                "value",
                "beginIndexSourcePath",
                "beginIndexValue",
                "lengthSourcePath",
                "lengthValue",
                StringCommands::substring2))
            .then(addTwoInOneOptionalInOneOutArgument("split",
                "sourcePath",
                "value",
                "separatorRegexSourcePath",
                "separatorRegexValue",
                "limitSourcePath",
                "limitValue",
                StringCommands::split))
            .then(addTwoInOneOptionalInZeroOutArgument("indexOf",
                "sourcePath",
                "value",
                "substringSourcePath",
                "substringValue",
                "fromIndexSourcePath",
                "fromIndexValue",
                StringCommands::indexOf))
            .then(addTwoInOneOptionalInZeroOutArgument("lastIndexOf",
                "sourcePath",
                "value",
                "substringSourcePath",
                "substringValue",
                "fromIndexSourcePath",
                "fromIndexValue",
                StringCommands::lastIndexOf))
            .then(addTwoInOneOptionalInZeroOutArgument("startsWith",
                "sourcePath",
                "value",
                "prefixSourcePath",
                "prefixValue",
                "offsetSourcePath",
                "offsetValue",
                StringCommands::startsWith))
            .then(addTwoInZeroOutArgument("endsWith",
                "sourcePath",
                "value",
                "suffixSourcePath",
                "suffixValue",
                StringCommands::endsWith))
            .then(addTwoInZeroOutArgument("contains",
                "sourcePath",
                "value",
                "substringSourcePath",
                "substringValue",
                StringCommands::contains))
            .then(addTwoInZeroOutArgument("matches",
                "sourcePath",
                "value",
                "regexSourcePath",
                "regexValue",
                StringCommands::matches))
            .then(addThreeInOneOutArgument("replace",
                "sourcePath",
                "value",
                "targetSourcePath",
                "targetValue",
                "replacementSourcePath",
                "replacementValue",
                StringCommands::replace))
            .then(addThreeInOneOutArgument("replaceAll",
                "sourcePath",
                "value",
                "regexSourcePath",
                "regexValue",
                "replacementSourcePath",
                "replacementValue",
                StringCommands::replaceAll))
            .then(addThreeInOneOutArgument("replaceFirst",
                "sourcePath",
                "value",
                "regexSourcePath",
                "regexValue",
                "replacementSourcePath",
                "replacementValue",
                StringCommands::replaceFirst));
        dispatcher.register(stringCommand);
    }

    public static Tag getTag(Tuple<Tag, NbtPathArgument.NbtPath> pair) throws CommandSyntaxException {
        var l = pair.getA();
        var r = pair.getB();
        return (r == null ? l : r.get(l).get(0));
    }

    public static int getNbtValueAsInt(Tuple<Tag, NbtPathArgument.NbtPath> pair) throws CommandSyntaxException {
        var e = getTag(pair);
        if (e instanceof NumericTag num) {
            return num.getAsInt();
        } else {
            throw EXPECTED_INT_EXCEPTION.create(e.getType().getPrettyName());
        }
    }

    public static String getNbtValueAsString(Tuple<Tag, NbtPathArgument.NbtPath> pair) throws CommandSyntaxException {
        return getTag(pair).getAsString();
    }

    public static int toInt(boolean bool) {
        return bool ? 1 : 0;
    }

    public static <T> int nullableArraySize(T[] array) {
        return array == null ? -1 : array.length;
    }

    public static <T> void checkArgumentCount(T[] sources, int count) throws CommandSyntaxException {
        if (nullableArraySize(sources) < count) {
            throw TOO_FEW_ARGUMENT_EXCEPTION.create();
        }
    }

    public static CharSet createTrimCharsSet(StringCommandContext ctx) throws CommandSyntaxException {
        if (ctx.sources.length <= 1) {
            return null;
        }
        var tc = getTag(ctx.sources[1]);
        var trimChars = new CharOpenHashSet();
        if (tc instanceof StringTag str) {
            String s = str.getAsString();
            int length = s.length();
            for (int i = 0; i < length; i++) {
                trimChars.add(s.charAt(i));
            }
        } else if (tc instanceof ListTag list) {
            var len = list.size();
            for (var i = 0; i < len; i++) {
                var str = list.get(i).getAsString();
                if (str.length() != 1) {
                    throw INVALID_CHAR_ARRAY_EXCEPTION.create(list);
                }
                trimChars.add(str.charAt(0));
            }
        } else {
            throw EXPECTED_STRING_EXCEPTION.create(tc);
        }
        return trimChars;
    }

    public static void setTarget(StringCommandContext ctx, Tag element) throws CommandSyntaxException {
        ctx.targetPath.set(ctx.targetRoot, element);
        ctx.target.setData(ctx.targetRoot);
        ctx.rawContext.getSource().sendSuccess(ctx.target::getModifiedSuccess, true);
    }

    public static int setTarget(StringCommandContext ctx, String element) throws CommandSyntaxException {
        setTarget(ctx, StringTag.valueOf(element));
        return element.length();
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addTarget(
        ArgumentBuilder<CommandSourceStack, ?> argument,
        BiFunction<ArgumentBuilder<CommandSourceStack, ?>, DataCommands.DataProvider, ArgumentBuilder<CommandSourceStack, ?>> argumentAdder) {
        for (var target : DataCommands.TARGET_PROVIDERS) {
            target.wrap(argument,
                builder -> builder.then(argumentAdder.apply(
                    Commands.argument("targetPath", NbtPathArgument.nbtPath()),
                    target)));
        }
        return argument;
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addSource(
        ArgumentBuilder<CommandSourceStack, ?> argument,
        String sourcePathName,
        String valueName,
        BiFunction<ArgumentBuilder<CommandSourceStack, ?>, NbtSource, ArgumentBuilder<CommandSourceStack, ?>> argumentAdder) {
        for (var source : DataCommands.SOURCE_PROVIDERS) {
            argument.then(source.wrap(Commands.literal("from"),
                innerBuilder -> argumentAdder.apply(innerBuilder, new FromDirectNbtSource(source))
                    .then(argumentAdder.apply(Commands.argument(sourcePathName, NbtPathArgument.nbtPath()),
                        new FromPathNbtSource(source, sourcePathName)))));
        }
        argument.then(Commands.literal("value")
            .then(argumentAdder.apply(Commands.argument(valueName, NbtTagArgument.nbtTag()), new ValueNbtSource(valueName))));
        return argument;
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addOneInZeroOutArgument(
        String name,
        StringCommandFunc command) {
        return addSource(Commands.literal(name),
            "sourcePath",
            "value",
            (builder, source) -> builder.executes(
                ctx -> command.apply(new StringCommandContext(ctx,
                    null,
                    null,
                    source.createPair(ctx)))));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addOneInOneOutArgument(
        String name,
        StringCommandFunc command) {
        return addTarget(Commands.literal(name),
            (builder, target) -> addSource(builder,
                "sourcePath",
                "value",
                (innerBuilder, source) -> innerBuilder.executes(
                    ctx -> command.apply(new StringCommandContext(ctx,
                        target.access(ctx),
                        NbtPathArgument.getPath(ctx, "targetPath"),
                        source.createPair(ctx))))));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addOneInOneOptionalInOneOutArgument(
        String name,
        String sourcePathName0,
        String valueName0,
        String sourcePathName1,
        String valueName1,
        StringCommandFunc command) {
        return addTarget(Commands.literal(name),
            (builder, target) -> addSource(builder,
                sourcePathName0,
                valueName0,
                (innerBuilder, source0) -> addSource(
                    innerBuilder.executes(ctx -> command.apply(new StringCommandContext(ctx,
                        target.access(ctx),
                        NbtPathArgument.getPath(ctx, "targetPath"),
                        source0.createPair(ctx)))),
                    sourcePathName1,
                    valueName1,
                    (inner2Builder, source1) -> inner2Builder.executes(
                        ctx -> command.apply(new StringCommandContext(ctx,
                            target.access(ctx),
                            NbtPathArgument.getPath(ctx, "targetPath"),
                            source0.createPair(ctx),
                            source1.createPair(ctx)))))));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addTwoInZeroOutArgument(
        String name,
        String sourcePathName0,
        String valueName0,
        String sourcePathName1,
        String valueName1,
        StringCommandFunc command) {
        return addSource(Commands.literal(name),
            sourcePathName0,
            valueName0,
            (builder, source0) -> addSource(builder,
                sourcePathName1,
                valueName1,
                (innerBuilder, source1) -> innerBuilder.executes(
                    ctx -> command.apply(new StringCommandContext(ctx,
                        null,
                        null,
                        source0.createPair(ctx),
                        source1.createPair(ctx))))));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addTwoInOneOutArgument(
        String name,
        String sourcePathName0,
        String valueName0,
        String sourcePathName1,
        String valueName1,
        StringCommandFunc command) {
        return addTarget(Commands.literal(name),
            (builder, target) -> addSource(builder,
                sourcePathName0,
                valueName0,
                (innerBuilder, source0) -> addSource(innerBuilder,
                    sourcePathName1,
                    valueName1,
                    (inner2Builder, source1) -> inner2Builder.executes(
                        ctx -> command.apply(new StringCommandContext(ctx,
                            target.access(ctx),
                            NbtPathArgument.getPath(ctx, "targetPath"),
                            source0.createPair(ctx),
                            source1.createPair(ctx)))))));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addTwoInOneOptionalInZeroOutArgument(
        String name,
        String sourcePathName0,
        String valueName0,
        String sourcePathName1,
        String valueName1,
        String sourcePathName2,
        String valueName2,
        StringCommandFunc command) {
        return addSource(Commands.literal(name),
            sourcePathName0,
            valueName0,
            (builder, source0) -> addSource(builder,
                sourcePathName1,
                valueName1,
                (inner1Builder, source1) -> addSource(
                    inner1Builder.executes(ctx -> command.apply(new StringCommandContext(ctx,
                        null,
                        null,
                        source0.createPair(ctx),
                        source1.createPair(ctx)))),
                    sourcePathName2,
                    valueName2,
                    (inner2Builder, source2) -> inner2Builder.executes(
                        ctx -> command.apply(new StringCommandContext(ctx,
                            null,
                            null,
                            source0.createPair(ctx),
                            source1.createPair(ctx),
                            source2.createPair(ctx)))))));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addTwoInOneOptionalInOneOutArgument(
        String name,
        String sourcePathName0,
        String valueName0,
        String sourcePathName1,
        String valueName1,
        String sourcePathName2,
        String valueName2,
        StringCommandFunc command) {
        return addTarget(Commands.literal(name),
            (builder, target) -> addSource(builder,
                sourcePathName0,
                valueName0,
                (innerBuilder, source0) -> addSource(innerBuilder,
                    sourcePathName1,
                    valueName1,
                    (inner2Builder, source1) -> addSource(
                        inner2Builder.executes(ctx -> command.apply(new StringCommandContext(ctx,
                            target.access(ctx),
                            NbtPathArgument.getPath(ctx, "targetPath"),
                            source0.createPair(ctx),
                            source1.createPair(ctx)))),
                        sourcePathName2,
                        valueName2,
                        (inner3Builder, source2) -> inner3Builder.executes(
                            ctx -> command.apply(new StringCommandContext(ctx,
                                target.access(ctx),
                                NbtPathArgument.getPath(ctx, "targetPath"),
                                source0.createPair(ctx),
                                source1.createPair(ctx),
                                source2.createPair(ctx))))))));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addThreeInOneOutArgument(
        String name,
        String sourcePathName0,
        String valueName0,
        String sourcePathName1,
        String valueName1,
        String sourcePathName2,
        String valueName2,
        StringCommandFunc command) {
        return addTarget(Commands.literal(name),
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
                            ctx -> command.apply(new StringCommandContext(ctx,
                                target.access(ctx),
                                NbtPathArgument.getPath(ctx, "targetPath"),
                                source0.createPair(ctx),
                                source1.createPair(ctx),
                                source2.createPair(ctx))))))));
    }
}