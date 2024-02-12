package net.myitian.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.arguments.NbtPathArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Pair;

public class StringCommandContext {public final DataCommandObject target;
    public final CompoundTag targetRoot;
    public final NbtPathArgumentType.NbtPath targetPath;
    public final Pair<Tag, NbtPathArgumentType.NbtPath>[] sources;

    public StringCommandContext(
            DataCommandObject target,
            NbtPathArgumentType.NbtPath targetPath,
            Tag source,
            NbtPathArgumentType.NbtPath sourcePath) throws CommandSyntaxException {
        this.target = target;
        this.targetRoot = target == null ? null : target.getTag();
        this.targetPath = targetPath;
        sources = new Pair[]{new Pair<>(source, sourcePath)};
    }

    public StringCommandContext(
            DataCommandObject target,
            NbtPathArgumentType.NbtPath targetPath,
            Pair<Tag, NbtPathArgumentType.NbtPath>[] sources) throws CommandSyntaxException {
        this.target = target;
        this.targetRoot = target == null ? null : target.getTag();
        this.targetPath = targetPath;
        this.sources = sources;
    }
}
