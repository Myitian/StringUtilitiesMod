package net.myitian.string_utilities_mod.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.myitian.string_utilities_mod.commands.StringCommandCore;

public class StringUtilitiesFabric implements ModInitializer {
    private static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext context,
        Commands.CommandSelection selection) {
        StringCommandCore.register(dispatcher);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(StringUtilitiesFabric::register);
    }
}