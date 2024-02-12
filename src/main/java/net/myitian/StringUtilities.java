package net.myitian;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.myitian.command.StringCommand;

public class StringUtilities implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistry.INSTANCE.register(false, StringCommand::register);
    }
}