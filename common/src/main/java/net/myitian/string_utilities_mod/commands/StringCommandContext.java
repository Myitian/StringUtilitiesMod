package net.myitian.string_utilities_mod.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.util.Tuple;

public final class StringCommandContext {
    public final CommandContext<CommandSourceStack> rawContext;
    public final DataAccessor target;
    public final CompoundTag targetRoot;
    public final NbtPathArgument.NbtPath targetPath;
    public final Tuple<Tag, NbtPathArgument.NbtPath>[] sources;

    @SafeVarargs
    public StringCommandContext(
        CommandContext<CommandSourceStack> rawContext,
        DataAccessor target,
        NbtPathArgument.NbtPath targetPath,
        Tuple<Tag, NbtPathArgument.NbtPath>... sources) throws CommandSyntaxException {
        this.rawContext = rawContext;
        this.target = target;
        targetRoot = target == null ? null : target.getData();
        this.targetPath = targetPath;
        this.sources = sources;
    }
}