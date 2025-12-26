package net.myitian.string_utilities_mod.commands.sources;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;

public abstract class NbtSource {
    public abstract Tag getSourceElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;

    public NbtPathArgument.NbtPath getSourcePath(CommandContext<CommandSourceStack> context) {
        return null;
    }

    public final Tuple<Tag, NbtPathArgument.NbtPath> createPair(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return new Tuple<>(getSourceElement(context), getSourcePath(context));
    }
}