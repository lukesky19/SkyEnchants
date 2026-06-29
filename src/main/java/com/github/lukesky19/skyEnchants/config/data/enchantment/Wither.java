package com.github.lukesky19.skyEnchants.config.data.enchantment;

import com.github.lukesky19.skyEnchants.config.data.enchantment.interfaces.IEnchantmentConfig;
import com.github.lukesky19.skyEnchants.config.data.misc.Registration;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NonNull;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.Map;

/**
 * This record contains the configuration for the wither enchantment.
 * @param version The config version.
 * @param enabled Is the enchantment enabled?
 * @param registration The {@link Registration} config for the enchantment.
 * @param effectDurationPerLevel The {@link Map} mapping enchantment levels to effect durations.
 * @param effectAmplifierPerLevel The {@link Map} mapping enchantment levels to effect amplifiers.
 */
@ConfigSerializable
public record Wither(
        int version,
        boolean enabled,
        @NonNull Registration registration,
        @NonNull Map<Integer, Integer> effectDurationPerLevel,
        @NonNull Map<Integer, Integer> effectAmplifierPerLevel) implements IEnchantmentConfig {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NonNull String getName() {
        return "Wither";
    }

    @Override
    public @NonNull TypedKey<@NonNull Enchantment> getTypedKey() {
        return EnchantmentKeys.create(Key.key("skyenchants", "wither"));
    }

    @Override
    public @NonNull Registration getRegistrationConfig() {
        return registration;
    }
}