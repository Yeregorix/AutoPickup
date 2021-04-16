/*
 * Copyright (c) 2018-2020 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.autopickup;

import com.google.inject.Inject;
import net.smoofyuniverse.autopickup.config.serializer.BlockSetSerializer;
import net.smoofyuniverse.autopickup.config.world.WorldConfig;
import net.smoofyuniverse.autopickup.event.EntityEventListener;
import net.smoofyuniverse.autopickup.event.WorldEventListener;
import net.smoofyuniverse.autopickup.util.IOUtil;
import net.smoofyuniverse.autopickup.util.collection.BlockSet;
import net.smoofyuniverse.autopickup.util.collection.BlockSet.SerializationPredicate;
import net.smoofyuniverse.ore.update.UpdateChecker;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(id = "autopickup", name = "AutoPickup", version = "1.0.6", authors = "Yeregorix", description = "Automatic pickup for items and experience orbs")
public class AutoPickup {
	public static final Logger LOGGER = LoggerFactory.getLogger("AutoPickup");
	private static AutoPickup instance;

	@Inject
	private Game game;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	@Inject
	private PluginContainer container;
	@Inject
	private GuiceObjectMapperFactory factory;

	private ConfigurationOptions configOptions;
	private Path worldConfigsDir;

	private final Map<String, WorldConfig.Immutable> configs = new HashMap<>();

	public AutoPickup() {
		if (instance != null)
			throw new IllegalStateException();
		instance = this;
	}

	@Listener
	public void onGamePreInit(GamePreInitializationEvent e) {
		TypeSerializerCollection.defaults().register(BlockSet.TOKEN, new BlockSetSerializer(SerializationPredicate.limit(0.6f)));

		this.worldConfigsDir = this.configDir.resolve("worlds");
		try {
			Files.createDirectories(this.worldConfigsDir);
		} catch (IOException ignored) {
		}
		this.configOptions = ConfigurationOptions.defaults().withObjectMapperFactory(this.factory);

		this.game.getEventManager().registerListeners(this, new WorldEventListener());
		this.game.getEventManager().registerListeners(this, new EntityEventListener());

		this.game.getEventManager().registerListeners(this, new UpdateChecker(LOGGER, this.container,
				createConfigLoader(this.configDir.resolve("update.conf")), "Yeregorix", "AutoPickup"));
	}

	public ConfigurationLoader<CommentedConfigurationNode> createConfigLoader(Path file) {
		return HoconConfigurationLoader.builder().setPath(file).setDefaultOptions(this.configOptions).build();
	}

	@Listener
	public void onGameReload(GameReloadEvent e) {
		this.configs.clear();
		this.game.getServer().getWorlds().forEach(this::loadConfig);
	}

	public void loadConfig(World world) {
		String name = world.getName();

		LOGGER.info("Loading configuration for world " + name + " ..");
		try {
			Path file = this.worldConfigsDir.resolve(name + ".conf");
			ConfigurationLoader<CommentedConfigurationNode> loader = createConfigLoader(file);

			CommentedConfigurationNode root = loader.load();
			int version = root.getNode("Version").getInt();
			if ((version > WorldConfig.CURRENT_VERSION || version < WorldConfig.MINIMUM__VERSION) && IOUtil.backupFile(file)) {
				LOGGER.info("Your config version is not supported. A new one will be generated.");
				root = loader.createEmptyNode();
			}

			ConfigurationNode cfgNode = root.getNode("Config");
			WorldConfig cfg = cfgNode.getValue(WorldConfig.TOKEN, new WorldConfig());

			root.getNode("Version").setValue(WorldConfig.CURRENT_VERSION);
			cfgNode.setValue(WorldConfig.TOKEN, cfg);
			loader.save(root);

			this.configs.put(name, cfg.toImmutable());
		} catch (Exception e) {
			LOGGER.error("Failed to load configuration for world " + name, e);
		}
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent e) {
		LOGGER.info("AutoPickup " + this.container.getVersion().orElse("?") + " was loaded successfully.");
	}

	public WorldConfig.Immutable getConfig(World world) {
		WorldConfig.Immutable cfg = this.configs.get(world.getName());
		if (cfg == null)
			throw new IllegalArgumentException();
		return cfg;
	}

	public boolean isEnabled(World world) {
		WorldConfig.Immutable cfg = this.configs.get(world.getName());
		return cfg != null && cfg.enabled;
	}

	public PluginContainer getContainer() {
		return this.container;
	}

	public static AutoPickup get() {
		if (instance == null)
			throw new IllegalStateException("Instance not available");
		return instance;
	}
}
