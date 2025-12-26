package net.myitian.string_utilities_mod;

import com.google.gson.Strictness;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.*;

import java.io.IOException;
import java.io.StringWriter;

public class JsonNbt {
    public static String convert(Tag element, boolean isPrettyPrinting) {
        try (var writer = new StringWriter(); var jw = new JsonWriter(writer)) {
            jw.setIndent(isPrettyPrinting ? "  " : "");
            jw.setHtmlSafe(false);
            jw.setStrictness(Strictness.LENIENT);
            jw.setSerializeNulls(true);
            convert(jw, element);
            return writer.toString();
        } catch (Exception e) {
            StringUtilities.LOGGER.error("An unexpected exception occurred: {}.", e.getClass().getSimpleName(), e);
            return null;
        }
    }

    public static void convert(JsonWriter writer, Tag element) throws IOException {
        if (element instanceof CompoundTag compound) {
            writer.beginObject();
            for (var childKey : compound.keySet()) {
                var child = compound.get(childKey);
                writer.name(childKey);
                convert(writer, child);
            }
            writer.endObject();
        } else if (element instanceof CollectionTag list) {
            writer.beginArray();
            for (var item : list) {
                convert(writer, item);
            }
            writer.endArray();
        } else if (element instanceof EndTag) {
            writer.nullValue();
        } else if (element instanceof FloatTag nbtFloat) {
            writer.value(nbtFloat.asFloat().orElseThrow());
        } else if (element instanceof DoubleTag nbtDouble) {
            writer.value(nbtDouble.asDouble().orElseThrow());
        } else if (element instanceof NumericTag number) {
            writer.value(number.asLong().orElseThrow());
        } else if (element instanceof StringTag nbtString) {
            writer.value(nbtString.asString().orElseThrow());
        }
    }
}