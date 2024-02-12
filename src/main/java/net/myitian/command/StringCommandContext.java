package net.myitian.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Pair;

public class StringCommandContext {public final DataCommandObject target;
    public final NbtCompound targetRoot;
    public final NbtPathArgumentType.NbtPath targetPath;
    public final Pair<NbtElement, NbtPathArgumentType.NbtPath>[] sources;

    public StringCommandContext(
            DataCommandObject target,
            NbtPathArgumentType.NbtPath targetPath,
            NbtElement source,
            NbtPathArgumentType.NbtPath sourcePath) throws CommandSyntaxException {
        this.target = target;
        this.targetRoot = target == null ? null : target.getNbt();
        this.targetPath = targetPath;
        sources = new Pair[]{new Pair<>(source, sourcePath)};
    }

    public StringCommandContext(
            DataCommandObject target,
            NbtPathArgumentType.NbtPath targetPath,
            Pair<NbtElement, NbtPathArgumentType.NbtPath>[] sources) throws CommandSyntaxException {
        this.target = target;
        this.targetRoot = target == null ? null : target.getNbt();
        this.targetPath = targetPath;
        this.sources = sources;
    }
}
