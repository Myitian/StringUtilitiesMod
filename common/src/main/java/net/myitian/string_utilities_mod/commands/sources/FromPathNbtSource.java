package net.myitian.string_utilities_mod.commands.sources;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.server.commands.data.DataCommands;

public final class FromPathNbtSource extends NbtSource {
    private final DataCommands.DataProvider source;
    private final String sourcePathName;

    public FromPathNbtSource(DataCommands.DataProvider source, String sourcePathName) {
        this.source = source;
        this.sourcePathName = sourcePathName;
    }

    public Tag getSourceElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return source.access(context).getData();
    }

    public NbtPathArgument.NbtPath getSourcePath(CommandContext<CommandSourceStack> context) {
        return NbtPathArgument.getPath(context, sourcePathName);
    }
}