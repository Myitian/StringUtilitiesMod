package net.myitian.string_utilities_mod.commands.sources;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.Tag;

public class ValueNbtSource extends NbtSource {
    private final String valueName;

    public ValueNbtSource(String valueName) {
        this.valueName = valueName;
    }

    public Tag getSourceElement(CommandContext<CommandSourceStack> context) {
        return NbtTagArgument.getNbtTag(context, valueName);
    }
}