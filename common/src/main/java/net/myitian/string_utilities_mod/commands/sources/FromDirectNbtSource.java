package net.myitian.string_utilities_mod.commands.sources;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.Tag;
import net.minecraft.server.commands.data.DataCommands;

public final class FromDirectNbtSource extends NbtSource {
    private final DataCommands.DataProvider source;

    public FromDirectNbtSource(DataCommands.DataProvider source) {
        this.source = source;
    }

    public Tag getSourceElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return source.access(context).getData();
    }
}