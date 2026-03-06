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

import com.github.lukesky19.skyEnchants.command.SkyEnchantsCommand;
import com.github.lukesky19.skyEnchants.config.manager.options.EnchantmentOptionsConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.enchantment.*;
import com.github.lukesky19.skyEnchants.listener.attribute.ChunkLoadListener;
import com.github.lukesky19.skyEnchants.listener.attribute.ItemClickListener;
import com.github.lukesky19.skyEnchants.listener.attribute.JoinListener;
import com.github.lukesky19.skyEnchants.listener.anvil.AnvilListener;
import com.github.lukesky19.skyEnchants.listener.block.BlockStatusListener;
import com.github.lukesky19.skyEnchants.listener.block.BuildToolListener;
import com.github.lukesky19.skyEnchants.listener.island.IslandListener;
import com.github.lukesky19.skyEnchants.listener.enchantment.*;
import com.github.lukesky19.skyEnchants.manager.attribute.AttributeManager;
import com.github.lukesky19.skyEnchants.config.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyEnchants.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyEnchants.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyEnchants.manager.block.BlockManager;
import com.github.lukesky19.skyEnchants.manager.enchantment.EnchantmentManager;
import com.github.lukesky19.skyEnchants.integration.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIListener;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This is the plugin's main class and entry point.
 */
public final class SkyEnchants extends SkyPlugin {
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

    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private GUIConfigManager guiConfigManager;
    private EnchantmentOptionsConfigManager anvilConfigManager;

    private AttributeManager attributeManager;
    private UUIDGUIManager guiManager;

    /**
     * Default Constructor.
     */
    public SkyEnchants() {}

    /**
     * Constructor
     * @param doubleDropConfigManager A {@link DoubleDropConfigManager} instance.
     * @param doubleJumpConfigManager A {@link DoubleJumpConfigManager} instance.
     * @param durabilityConfigManager A {@link DurabilityConfigManager} instance.
     * @param explosiveConfigManager An {@link ExplosiveConfigManager} instance.
     * @param hasteConfigManager A {@link HasteConfigManager} instance.
     * @param healthConfigManager A {@link HealthConfigManager} instance.
     * @param magnetConfigManager A {@link MagnetConfigManager} instance.
     * @param multibreakConfigManager A {@link MultibreakConfigManager} instance.
     * @param poisonConfigManager A {@link PoisonConfigManager} instance.
     * @param reachConfigManager A {@link ReachConfigManager} instance.
     * @param replantConfigManager A {@link ReplantConfigManager} instance.
     * @param shieldBashConfigManager A {@link ShieldBashConfigManager} instance.
     * @param smeltConfigManager A {@link SmeltConfigManager} instance.
     * @param speedConfigManager A {@link SpeedConfigManager} instance.
     * @param treeFellerConfigManager A {@link TreeFellerConfigManager} instance.
     * @param witherConfigManager A {@link WitherConfigManager} instance.
     */
    public SkyEnchants(
            @NotNull DoubleDropConfigManager doubleDropConfigManager,
            @NotNull DoubleJumpConfigManager doubleJumpConfigManager,
            @NotNull DurabilityConfigManager durabilityConfigManager,
            @NotNull ExplosiveConfigManager explosiveConfigManager,
            @NotNull HasteConfigManager hasteConfigManager,
            @NotNull HealthConfigManager healthConfigManager,
            @NotNull MagnetConfigManager magnetConfigManager,
            @NotNull MultibreakConfigManager multibreakConfigManager,
            @NotNull PoisonConfigManager poisonConfigManager,
            @NotNull ReachConfigManager reachConfigManager,
            @NotNull ReplantConfigManager replantConfigManager,
            @NotNull ShieldBashConfigManager shieldBashConfigManager,
            @NotNull SmeltConfigManager smeltConfigManager,
            @NotNull SpeedConfigManager speedConfigManager,
            @NotNull TreeFellerConfigManager treeFellerConfigManager,
            @NotNull WitherConfigManager witherConfigManager) {
        this.doubleDropConfigManager = doubleDropConfigManager;
        this.doubleJumpConfigManager = doubleJumpConfigManager;
        this.durabilityConfigManager = durabilityConfigManager;
        this.explosiveConfigManager = explosiveConfigManager;
        this.hasteConfigManager = hasteConfigManager;
        this.healthConfigManager = healthConfigManager;
        this.magnetConfigManager = magnetConfigManager;
        this.multibreakConfigManager = multibreakConfigManager;
        this.poisonConfigManager = poisonConfigManager;
        this.reachConfigManager = reachConfigManager;
        this.replantConfigManager = replantConfigManager;
        this.shieldBashConfigManager = shieldBashConfigManager;
        this.smeltConfigManager = smeltConfigManager;
        this.speedConfigManager = speedConfigManager;
        this.treeFellerConfigManager = treeFellerConfigManager;
        this.witherConfigManager = witherConfigManager;
    }

    /**
     * This method is run when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        if(!checkSkyLibVersion()) return;
        ComponentLogger logger = this.getComponentLogger();

        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        guiConfigManager = new GUIConfigManager(this);
        anvilConfigManager = new EnchantmentOptionsConfigManager(this);
        guiManager = new UUIDGUIManager();
        EnchantmentManager enchantmentManager = new EnchantmentManager(logger);
        attributeManager = new AttributeManager(logger, healthConfigManager, reachConfigManager, speedConfigManager, enchantmentManager);
        HookManager hookManager = new HookManager(this);
        BlockManager blockManager = new BlockManager(this);

        // Set up command
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(new SkyEnchantsCommand(this, guiManager, settingsManager, localeManager, anvilConfigManager, guiConfigManager, hookManager).createCommand(),
                                "Command to manage and use the SkyEnchants plugin.",
                                List.of("enchants")));

        reload();

        PluginManager pluginManager = this.getServer().getPluginManager();

        // Anvils
        pluginManager.registerEvents(new AnvilListener(logger, anvilConfigManager, localeManager), this);

        // GUI
        pluginManager.registerEvents(new UUIDGUIListener(guiManager), this);

        // Player-placed block status
        pluginManager.registerEvents(new BlockStatusListener(this, blockManager), this);
        if(pluginManager.isPluginEnabled("SkyTools")) {
            pluginManager.registerEvents(new BuildToolListener(blockManager), this);
        }
        if(pluginManager.isPluginEnabled("BentoBox")) {
            pluginManager.registerEvents(new IslandListener(blockManager), this);
        }

        // Attributes
        pluginManager.registerEvents(new ChunkLoadListener(attributeManager), this);
        pluginManager.registerEvents(new ItemClickListener(attributeManager), this);
        pluginManager.registerEvents(new JoinListener(attributeManager), this);

        // Enchantments
        pluginManager.registerEvents(new BlockDropItemListener(this, doubleDropConfigManager, smeltConfigManager, magnetConfigManager, enchantmentManager, blockManager), this);
        pluginManager.registerEvents(new DoubleJumpEnchantmentListener(logger, doubleJumpConfigManager, enchantmentManager), this);
        pluginManager.registerEvents(new DurabilityEnchantmentListener(durabilityConfigManager, enchantmentManager), this);
        pluginManager.registerEvents(new ExplosiveEnchantmentListener(logger, explosiveConfigManager, enchantmentManager), this);
        pluginManager.registerEvents(new HasteEnchantmentListener(this, hasteConfigManager, enchantmentManager), this);
        pluginManager.registerEvents(new MultiBlockBreakListener(this, doubleDropConfigManager, smeltConfigManager, magnetConfigManager, enchantmentManager, blockManager), this);
        pluginManager.registerEvents(new MultibreakEnchantmentListener(this, durabilityConfigManager, multibreakConfigManager, enchantmentManager, hookManager), this);
        pluginManager.registerEvents(new PoisonEnchantmentListener(logger, poisonConfigManager, enchantmentManager), this);
        pluginManager.registerEvents(new ReplantEnchantmentListener(this, replantConfigManager, enchantmentManager), this);
        pluginManager.registerEvents(new ShieldBashEnchantmentListener(logger, shieldBashConfigManager, enchantmentManager), this);
        pluginManager.registerEvents(new TreeFellerEnchantmentListener(this, durabilityConfigManager, treeFellerConfigManager, enchantmentManager, hookManager), this);
        pluginManager.registerEvents(new WitherEnchantmentListener(logger, witherConfigManager, enchantmentManager), this);
    }

    /**
     * This method is run when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(guiManager != null) guiManager.closeOpenGUIs(true);
    }

    /**
     * Reloads all plugin data.
     */
    public void reload() {
        guiManager.closeOpenGUIs(false);

        doubleDropConfigManager.loadConfiguration();
        doubleJumpConfigManager.loadConfiguration();
        durabilityConfigManager.loadConfiguration();
        explosiveConfigManager.loadConfiguration();
        hasteConfigManager.loadConfiguration();
        healthConfigManager.loadConfiguration();
        magnetConfigManager.loadConfiguration();
        multibreakConfigManager.loadConfiguration();
        poisonConfigManager.loadConfiguration();
        reachConfigManager.loadConfiguration();
        replantConfigManager.loadConfiguration();
        shieldBashConfigManager.loadConfiguration();
        smeltConfigManager.loadConfiguration();
        speedConfigManager.loadConfiguration();
        treeFellerConfigManager.loadConfiguration();
        witherConfigManager.loadConfiguration();

        settingsManager.loadConfiguration();
        localeManager.loadConfiguration();
        guiConfigManager.reload();
        anvilConfigManager.loadConfiguration();

        this.getServer().getOnlinePlayers().forEach(player ->
                attributeManager.applyAttributes(player.getEquipment()));

        this.getServer().getWorlds().forEach(world ->
                world.getEntities().stream()
                        .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
                        .map(entity -> (LivingEntity) entity)
                        .forEach(livingEntity -> {
                            EntityEquipment entityEquipment = livingEntity.getEquipment();

                            if(entityEquipment != null) attributeManager.applyAttributes(entityEquipment);
                        }));
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if(skyLib != null && skyLib.isEnabled()) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 5) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.deserialize("SkyLib Version 1.5.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }
}