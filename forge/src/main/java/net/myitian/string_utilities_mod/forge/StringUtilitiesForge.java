package net.myitian.string_utilities_mod.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.myitian.string_utilities_mod.StringUtilities;
import net.myitian.string_utilities_mod.commands.StringCommandCore;

import java.lang.reflect.Field;
import java.util.function.Consumer;

@Mod(StringUtilities.MOD_ID)
public class StringUtilitiesForge {
    public StringUtilitiesForge() throws ReflectiveOperationException {
        try {
            // 1.21.6+: EventBus 7
            RegisterCommandsEvent.BUS.addListener(StringUtilitiesForge::registerCommands);
        } catch (LinkageError e) {
            // 1.21.5-: EventBus 6
            Field field = MinecraftForge.class.getField("EVENT_BUS");
            field.getType()
                .getMethod("addListener", Consumer.class)
                .invoke(field.get(null), (Consumer<RegisterCommandsEvent>) StringUtilitiesForge::registerCommands);
        }
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        StringCommandCore.register(dispatcher);
    }
}