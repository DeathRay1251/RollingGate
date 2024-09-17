package dev.anvilcraft.rg.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.anvilcraft.rg.RollingGate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.include.com.google.common.base.Charsets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RGRuleManager {
    private static final RGRuleManager INSTANCE = new RGRuleManager();
    private static final Path GlobalConfigPath = FMLPaths.CONFIGDIR.get().resolve("rolling_gate.json");
    private static final LevelResource WorldConfigPath = new LevelResource("rolling_gate.json");
    private final Map<String, RGRule<?>> rules;

    public RGRuleManager() {
        this.rules = new HashMap<>();
    }

    public static void reInitSaveRules(MinecraftServer server) {
        Path path = server.getWorldPath(RGRuleManager.WorldConfigPath);
        setSaveRules(getOrCreateContent(GlobalConfigPath));
        setSaveRules(getOrCreateContent(path));
    }

    private static void setSaveRules(JsonObject config) {
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            RGRule<?> rule = INSTANCE.rules.get(entry.getKey());
            if (rule == null) {
                RollingGate.LOGGER.warn("{}({}) not exist.", entry.getKey(), entry.getValue());
                continue;
            }
            rule.setFieldValue(entry.getValue());
        }
    }

    public void register(RGRule<?> rule) {
        this.rules.put(rule.namespace, rule);
    }

    private static JsonObject getOrCreateContent(Path path) {
        File file = path.toFile();
        try {
            if (!file.exists() || file.isDirectory()) {
                FileUtils.writeStringToFile(file, "{}", Charsets.UTF_8);
                return new JsonObject();
            }
            String value = FileUtils.readFileToString(path.toFile(), Charsets.UTF_8);
            return GsonHelper.parse(value);
        } catch (IOException e) {
            throw new RGRuleException("Failed to read rolling gate config file", e);
        }
    }

    public static @NotNull List<RGRule<?>> of(String namespace, @NotNull Class<?> rules) {
        List<RGRule<?>> ruleList = new ArrayList<>();
        for (Field field : rules.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!Modifier.isPublic(field.getModifiers())) continue;
            if (Modifier.isFinal(field.getModifiers())) continue;
            RGRule.checkType(field);
            ruleList.add(RGRule.of(namespace, field));
        }
        return ruleList;
    }

}
