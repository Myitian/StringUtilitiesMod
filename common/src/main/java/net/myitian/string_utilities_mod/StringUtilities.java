package net.myitian.string_utilities_mod;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public final class StringUtilities {
    public static final String MOD_ID = "string_utilities_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static Predicate<CommandSourceStack> hasElevatedPermissions = null;

    public static Predicate<CommandSourceStack> hasElevatedPermissions() {
        if (hasElevatedPermissions != null) {
            return hasElevatedPermissions;
        }
        try {
            // 1.21.11+: advanced permission
            Field field = getField(Commands.class,
                "LEVEL_GAMEMASTERS",
                "field_31839",
                "GAMEMASTERS_CHECK");
            if (field != null) {
                Method method = getMethod(Commands.class, field.getType(),
                    "hasPermission",
                    "method_71774",
                    "requirePermissionLevel");
                if (method != null) {
                    Object value = method.invoke(null, field.get(null));
                    hasElevatedPermissions = (Predicate<CommandSourceStack>) value;
                }
            }
        } catch (Exception ignored) {
        }
        if (hasElevatedPermissions == null) {
            // 1.21.10-: legacy int permission
            hasElevatedPermissions = source -> source.hasPermission(2);
        }
        return hasElevatedPermissions;
    }

    public static Field getField(Class<?> clazz, String official, String intermediary, String named) {
        try {
            return clazz.getField(official); // NeoForge & Forge
        } catch (Exception e0) {
            try {
                return clazz.getField(intermediary); // Fabric
            } catch (Exception e1) {
                try {
                    return clazz.getField(named); // Development Environment
                } catch (Exception e2) {
                    return null;
                }
            }
        }
    }

    public static Method getMethod(Class<?> clazz, Class<?> paramType, String official, String intermediary, String named) {
        try {
            return clazz.getMethod(official, paramType);
        } catch (Exception e0) {
            try {
                return clazz.getMethod(intermediary, paramType);
            } catch (Exception e1) {
                try {
                    return clazz.getMethod(named, paramType);
                } catch (Exception e2) {
                    return null;
                }
            }
        }
    }
}