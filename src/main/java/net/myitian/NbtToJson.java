package net.myitian;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.*;

import java.io.IOException;
import java.io.StringWriter;

public class NbtToJson {
    public static String convert(Tag element, boolean isPrettyPrinting) {
        GsonBuilder gb = new GsonBuilder().disableHtmlEscaping();
        if (isPrettyPrinting)
            gb.setPrettyPrinting();
        com.google.gson.Gson gson = gb.create();
        try (StringWriter writer = new StringWriter(); JsonWriter jw = gson.newJsonWriter(writer)) {
            convert(jw, element);
            return writer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static void convert(JsonWriter jw, Tag element) throws IOException {
        if (element instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag) element;
            jw.beginObject();
            for (String childKey : compound.getKeys()) {
                Tag child = compound.getTag(childKey);
                jw.name(childKey);
                convert(jw, child);
            }
            jw.endObject();
        } else if (element instanceof AbstractListTag<?>) {
            jw.beginArray();
            for (Tag item : (AbstractListTag<?>) element) {
                convert(jw, item);
            }
            jw.endArray();
        } else if (element instanceof EndTag) {
            jw.nullValue();
        } else if (element instanceof FloatTag) {
            jw.value(((FloatTag) element).getFloat());
        } else if (element instanceof DoubleTag) {
            jw.value(((DoubleTag) element).getDouble());
        } else if (element instanceof AbstractNumberTag) {
            jw.value(((AbstractNumberTag) element).getLong());
        } else if (element instanceof StringTag) {
            jw.value(element.asString());
        }
    }
}
