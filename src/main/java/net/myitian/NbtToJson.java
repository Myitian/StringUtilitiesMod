package net.myitian;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.*;

import java.io.IOException;
import java.io.StringWriter;

public class NbtToJson {
    public static String convert(NbtElement element, boolean isPrettyPrinting) {
        var gb = new GsonBuilder().disableHtmlEscaping();
        if (isPrettyPrinting)
            gb.setPrettyPrinting();
        var gson = gb.create();
        try (var writer = new StringWriter(); var jw = gson.newJsonWriter(writer)) {
            convert(jw, element);
            return writer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static void convert(JsonWriter jw, NbtElement element) throws IOException {
        if (element instanceof NbtCompound compound) {
            jw.beginObject();
            for (var childKey : compound.getKeys()) {
                var child = compound.get(childKey);
                jw.name(childKey);
                convert(jw, child);
            }
            jw.endObject();
        } else if (element instanceof AbstractNbtList<?> list) {
            jw.beginArray();
            for (var item : list) {
                convert(jw, item);
            }
            jw.endArray();
        } else if (element instanceof NbtEnd) {
            jw.nullValue();
        } else if (element instanceof NbtFloat nbtFloat) {
            jw.value(nbtFloat.floatValue());
        } else if (element instanceof NbtDouble nbtDouble) {
            jw.value(nbtDouble.doubleValue());
        } else if (element instanceof AbstractNbtNumber number) {
            jw.value(number.longValue());
        } else if (element instanceof NbtString nbtString) {
            jw.value(nbtString.asString());
        }
    }
}
