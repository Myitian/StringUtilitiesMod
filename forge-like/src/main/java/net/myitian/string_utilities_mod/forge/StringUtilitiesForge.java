package net.myitian.string_utilities_mod.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.myitian.string_utilities_mod.StringUtilities;
import net.myitian.string_utilities_mod.commands.StringCommandCore;

@Mod(StringUtilities.MOD_ID)
public class StringUtilitiesForge {
    public StringUtilitiesForge() {
        MinecraftForge.EVENT_BUS.addListener(StringUtilitiesForge::registerCommands);
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        StringCommandCore.register(dispatcher);
    }
}