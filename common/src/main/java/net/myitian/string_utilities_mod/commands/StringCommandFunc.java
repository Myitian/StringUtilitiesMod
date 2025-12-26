package net.myitian.string_utilities_mod.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface StringCommandFunc {
    int apply(StringCommandContext ctx) throws CommandSyntaxException;
}