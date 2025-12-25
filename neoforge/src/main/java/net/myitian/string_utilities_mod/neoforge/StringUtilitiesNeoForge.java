package net.myitian.string_utilities_mod.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.myitian.string_utilities_mod.StringUtilities;
import net.myitian.string_utilities_mod.commands.StringCommandCore;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(StringUtilities.MOD_ID)
public class StringUtilitiesNeoForge {
    public StringUtilitiesNeoForge() {
        NeoForge.EVENT_BUS.addListener(StringUtilitiesNeoForge::registerCommands);
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        StringCommandCore.register(dispatcher);
    }
}