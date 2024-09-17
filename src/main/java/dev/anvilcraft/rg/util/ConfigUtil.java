package dev.anvilcraft.rg.util;

import com.google.gson.JsonObject;
import dev.anvilcraft.rg.api.RGRuleException;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ConfigUtil {

    public static @NotNull JsonObject getOrCreateContent(@NotNull Path path) {
        File file = path.toFile();
        try {
            if (!file.exists() || file.isDirectory()) {
                FileUtils.writeStringToFile(file, "{}", StandardCharsets.UTF_8);
                return new JsonObject();
            }
            String value = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
            return GsonHelper.parse(value);
        } catch (IOException e) {
            throw new RGRuleException("Failed to read rolling gate config file", e);
        }
    }

}
