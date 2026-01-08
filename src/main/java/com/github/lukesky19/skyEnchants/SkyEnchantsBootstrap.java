/*
    SkyEnchants adds custom enchantments.
    Copyright (C) 2026 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyEnchants;

import com.github.lukesky19.skyEnchants.config.data.enchantment.interfaces.IEnchantmentConfig;
import com.github.lukesky19.skyEnchants.config.data.misc.Registration;
import com.github.lukesky19.skyEnchants.config.data.enchantment.*;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.*;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.event.WritableRegistry;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static com.github.lukesky19.skyEnchants.util.PluginUtils.*;

/**
 * This class registers the custom enchantments for the plugin using the bootstrap API.
 */
@SuppressWarnings("unused")
public final class SkyEnchantsBootstrap implements PluginBootstrap {
    /**
     * Default Constructor
     */
    public SkyEnchantsBootstrap() {}

    /**
     * Registers the custom enchantments for the plugin.
     * @param context the server provided context
     */
    @Override
    public void bootstrap(BootstrapContext context) {
        File dataDirectory = context.getDataDirectory().toFile();
        ComponentLogger logger = context.getLogger();

        doubleDropConfigManager = new DoubleDropConfigManager(dataDirectory, logger);
        doubleDropConfigManager.loadConfiguration();
        doubleDrop = doubleDropConfigManager.getConfiguration();

        doubleJumpConfigManager = new DoubleJumpConfigManager(dataDirectory, logger);
        doubleJumpConfigManager.loadConfiguration();
        doubleJump = doubleJumpConfigManager.getConfiguration();

        durabilityConfigManager = new DurabilityConfigManager(dataDirectory, logger);
        durabilityConfigManager.loadConfiguration();
        durability = durabilityConfigManager.getConfiguration();

        explosiveConfigManager = new ExplosiveConfigManager(dataDirectory, logger);
        explosiveConfigManager.loadConfiguration();
        explosive = explosiveConfigManager.getConfiguration();

        hasteConfigManager = new HasteConfigManager(dataDirectory, logger);
        hasteConfigManager.loadConfiguration();
        haste = hasteConfigManager.getConfiguration();

        healthConfigManager = new HealthConfigManager(dataDirectory, logger);
        healthConfigManager.loadConfiguration();
        health = healthConfigManager.getConfiguration();

        magnetConfigManager = new MagnetConfigManager(dataDirectory, logger);
        magnetConfigManager.loadConfiguration();
        magnet = magnetConfigManager.getConfiguration();

        multibreakConfigManager = new MultibreakConfigManager(dataDirectory, logger);
        multibreakConfigManager.loadConfiguration();
        multibreak = multibreakConfigManager.getConfiguration();

        poisonConfigManager = new PoisonConfigManager(dataDirectory, logger);
        poisonConfigManager.loadConfiguration();
        poison = poisonConfigManager.getConfiguration();

        reachConfigManager = new ReachConfigManager(dataDirectory, logger);
        reachConfigManager.loadConfiguration();
        reach = reachConfigManager.getConfiguration();

        replantConfigManager = new ReplantConfigManager(dataDirectory, logger);
        replantConfigManager.loadConfiguration();
        replant = replantConfigManager.getConfiguration();

        shieldBashConfigManager = new ShieldBashConfigManager(dataDirectory, logger);
        shieldBashConfigManager.loadConfiguration();
        shieldBash = shieldBashConfigManager.getConfiguration();

        smeltConfigManager = new SmeltConfigManager(dataDirectory, logger);
        smeltConfigManager.loadConfiguration();
        smelt = smeltConfigManager.getConfiguration();

        speedConfigManager = new SpeedConfigManager(dataDirectory, logger);
        speedConfigManager.loadConfiguration();
        speed = speedConfigManager.getConfiguration();

        treeFellerConfigManager = new TreeFellerConfigManager(dataDirectory, logger);
        treeFellerConfigManager.loadConfiguration();
        treeFeller = treeFellerConfigManager.getConfiguration();

        witherConfigManager = new WitherConfigManager(dataDirectory, logger);
        witherConfigManager.loadConfiguration();
        wither = witherConfigManager.getConfiguration();

        // Register enchantments in enchantment table if configured to do so
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT), event -> {
            Set<@NotNull TypedKey<@NotNull Enchantment>> enchantmentSet = getInEnchantmentTableSet();

            if(!enchantmentSet.isEmpty()) {
                PostFlattenTagRegistrar<@NotNull Enchantment> registrar = event.registrar();

                registrar.addToTag(
                        EnchantmentTagKeys.IN_ENCHANTING_TABLE,
                        enchantmentSet
                );
            }
        });

        // Register enchantments in the enchantment registry
        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.compose()
            .newHandler(event -> {
                WritableRegistry<@NotNull Enchantment, EnchantmentRegistryEntry.@NotNull Builder> writableRegistry = event.registry();

                if(doubleDrop != null) {
                    registerEnchantment(doubleDrop, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the double drop enchantment due to invalid settings."));
                }

                if(doubleJump != null) {
                    registerEnchantment(doubleJump, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the double jump enchantment due to invalid settings."));
                }

                if(durability != null) {
                    registerEnchantment(durability, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the durability enchantment due to invalid settings."));
                }

                if(explosive != null) {
                    registerEnchantment(explosive, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the explosive enchantment due to invalid settings."));
                }

                if(haste != null) {
                    registerEnchantment(haste, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the haste enchantment due to invalid settings."));
                }

                if(health != null) {
                    registerEnchantment(health, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the health enchantment due to invalid settings."));
                }

                if(magnet != null) {
                    registerEnchantment(magnet, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the magnet enchantment due to invalid settings."));
                }

                if(multibreak != null) {
                    registerEnchantment(multibreak, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the multibreak enchantment due to invalid settings."));
                }

                if(poison != null) {
                    registerEnchantment(poison, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the poison enchantment due to invalid settings."));
                }

                if(reach != null) {
                    registerEnchantment(reach, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the reach enchantment due to invalid settings."));
                }

                if(replant != null) {
                    registerEnchantment(replant, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the replant enchantment due to invalid settings."));
                }

                if(shieldBash != null) {
                    registerEnchantment(shieldBash, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the shield bash enchantment due to invalid settings."));
                }

                if(smelt != null) {
                    registerEnchantment(smelt, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the smelt enchantment due to invalid settings."));
                }

                if(speed != null) {
                    registerEnchantment(speed, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the speed enchantment due to invalid settings."));
                }

                if(treeFeller != null) {
                    registerEnchantment(treeFeller, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the tree feller enchantment due to invalid settings."));
                }

                if(wither != null) {
                    registerEnchantment(wither, writableRegistry);
                } else {
                    logger.error(AdventureUtil.deserialize("Unable to register the wither enchantment due to invalid settings."));
                }
            }));
    }

    /**
     * Create the {@link JavaPlugin} while passing the configuration managers used here.
     * @param context the server created bootstrap object
     * @return A {@link JavaPlugin}.
     */
    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new SkyEnchants(
                doubleDropConfigManager,
                doubleJumpConfigManager,
                durabilityConfigManager,
                explosiveConfigManager,
                hasteConfigManager,
                healthConfigManager,
                magnetConfigManager,
                multibreakConfigManager,
                poisonConfigManager,
                reachConfigManager,
                replantConfigManager,
                shieldBashConfigManager,
                smeltConfigManager,
                speedConfigManager,
                treeFellerConfigManager,
                witherConfigManager);
    }

    /**
     * Register the enchantment.
     * @param enchantmentConfig The enchantment config.
     * @param enchantmentRegistry The enchantment registry.
     */
    private void registerEnchantment(
            @NotNull IEnchantmentConfig enchantmentConfig,
            @NotNull WritableRegistry<@NotNull Enchantment, EnchantmentRegistryEntry.@NotNull Builder> enchantmentRegistry) {
        if(enchantmentConfig.isEnabled()) {
            Registration registrationConfig = enchantmentConfig.getRegistrationConfig();
            RegistryKeySet<@NotNull ItemType> supportedItems = RegistrySet.keySet(RegistryKey.ITEM, getSupportedItemTypeTypedKeys(registrationConfig.supportedItems()));
            EquipmentSlotGroup[] equipmentSlotGroups = getEquipmentSlotGroups(registrationConfig.equipmentSlots());
            RegistryKeySet<@NotNull Enchantment> exclusiveEnchantments = getExclusiveEnchantments(registrationConfig.exclusive());

            enchantmentRegistry.register(enchantmentConfig.getTypedKey(), builder -> {
                builder.description(Component.text(enchantmentConfig.getName()));

                if(!supportedItems.isEmpty()) builder.supportedItems(supportedItems);

                if(equipmentSlotGroups.length > 0) builder.activeSlots(equipmentSlotGroups);

                if(!exclusiveEnchantments.isEmpty()) builder.exclusiveWith(exclusiveEnchantments);

                builder.anvilCost(registrationConfig.anvilCost());

                builder.maxLevel(registrationConfig.maxLevel());

                builder.weight(registrationConfig.weight());

                builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(registrationConfig.minimumCost().baseCost(), registrationConfig.minimumCost().additionalCostPerLevel()));

                builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(registrationConfig.maximumCost().baseCost(), registrationConfig.maximumCost().additionalCostPerLevel()));
            });
        }
    }

    /**
     * Get the {@link Set} of {@link TypedKey}s of type {@link Enchantment} to register to the enchantment table.
     * @return A {@link Set} of {@link TypedKey}s of type {@link Enchantment}.
     */
    private @NotNull Set<@NotNull TypedKey<@NotNull Enchantment>> getInEnchantmentTableSet() {
        Set<TypedKey<@NotNull Enchantment>> set = new HashSet<>();

        if(doubleDrop != null && doubleDrop.enabled() && doubleDrop.registration().showInEnchantmentTable()) set.add(doubleDrop.getTypedKey());
        if(doubleJump != null && doubleJump.enabled() && doubleJump.registration().showInEnchantmentTable()) set.add(doubleJump.getTypedKey());
        if(durability != null && durability.enabled() && durability.registration().showInEnchantmentTable()) set.add(durability.getTypedKey());
        if(shieldBash != null && shieldBash.enabled() && shieldBash.registration().showInEnchantmentTable()) set.add(shieldBash.getTypedKey());
        if(explosive != null && explosive.enabled() && explosive.registration().showInEnchantmentTable()) set.add(explosive.getTypedKey());
        if(haste != null && haste.enabled() && haste.registration().showInEnchantmentTable()) set.add(haste.getTypedKey());
        if(health != null && health.enabled() && health.registration().showInEnchantmentTable()) set.add(health.getTypedKey());
        if(magnet != null && magnet.enabled() && magnet.registration().showInEnchantmentTable()) set.add(magnet.getTypedKey());
        if(multibreak != null && multibreak.enabled() && multibreak.registration().showInEnchantmentTable()) set.add(multibreak.getTypedKey());
        if(poison != null && poison.enabled() && poison.registration().showInEnchantmentTable()) set.add(poison.getTypedKey());
        if(reach != null && reach.enabled() && reach.registration().showInEnchantmentTable()) set.add(reach.getTypedKey());
        if(replant != null && replant.enabled() && replant.registration().showInEnchantmentTable()) set.add(replant.getTypedKey());
        if(speed != null && speed.enabled() && speed.registration().showInEnchantmentTable()) set.add(speed.getTypedKey());
        if(smelt != null && smelt.enabled() && smelt.registration().showInEnchantmentTable()) set.add(smelt.getTypedKey());
        if(treeFeller != null && treeFeller.enabled() && treeFeller.registration().showInEnchantmentTable()) set.add(treeFeller.getTypedKey());
        if(wither != null && wither.enabled() && wither.registration().showInEnchantmentTable()) set.add(wither.getTypedKey());

        return set;
    }

    private DoubleDropConfigManager doubleDropConfigManager;
    private DoubleJumpConfigManager doubleJumpConfigManager;
    private DurabilityConfigManager durabilityConfigManager;
    private ExplosiveConfigManager explosiveConfigManager;
    private HasteConfigManager hasteConfigManager;
    private HealthConfigManager healthConfigManager;
    private MagnetConfigManager magnetConfigManager;
    private MultibreakConfigManager multibreakConfigManager;
    private PoisonConfigManager poisonConfigManager;
    private ReachConfigManager reachConfigManager;
    private ReplantConfigManager replantConfigManager;
    private ShieldBashConfigManager shieldBashConfigManager;
    private SmeltConfigManager smeltConfigManager;
    private SpeedConfigManager speedConfigManager;
    private TreeFellerConfigManager treeFellerConfigManager;
    private WitherConfigManager witherConfigManager;

    private @Nullable DoubleDrop doubleDrop;
    private @Nullable DoubleJump doubleJump;
    private @Nullable Durability durability;
    private @Nullable Explosive explosive;
    private @Nullable Haste haste;
    private @Nullable Health health;
    private @Nullable Magnet magnet;
    private @Nullable Multibreak multibreak;
    private @Nullable Poison poison;
    private @Nullable Reach reach;
    private @Nullable Replant replant;
    private @Nullable ShieldBash shieldBash;
    private @Nullable Smelt smelt;
    private @Nullable Speed speed;
    private @Nullable TreeFeller treeFeller;
    private @Nullable Wither wither;
}