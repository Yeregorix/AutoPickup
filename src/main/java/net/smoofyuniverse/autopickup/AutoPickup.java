/*
 * Copyright (c) 2018-2024 Hugo Dupanloup (Yeregorix)
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
import net.smoofyuniverse.autopickup.config.world.WorldConfig;
import net.smoofyuniverse.autopickup.event.DropListener;
import net.smoofyuniverse.map.WorldMap;
import net.smoofyuniverse.map.WorldMapLoader;
import net.smoofyuniverse.ore.update.UpdateChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin("autopickup")
public class AutoPickup {
	public static final Logger LOGGER = LogManager.getLogger("AutoPickup");
	private static AutoPickup instance;

	@Inject
	private Game game;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	@Inject
	private PluginContainer container;

	private ConfigurationOptions configOptions;

	private WorldMapLoader<WorldConfig.Resolved> configMapLoader;
	private WorldMap<WorldConfig.Resolved> configMap;

	public AutoPickup() {
		if (instance != null)
			throw new IllegalStateException();
		instance = this;
	}

	@Listener
	public void onConstructPlugin(ConstructPluginEvent e) {
		this.configOptions = ConfigurationOptions.defaults().serializers(this.game.configManager().serializers());

		try {
			Files.createDirectories(this.configDir);
		} catch (IOException ignored) {
		}

		this.configMapLoader = new WorldMapLoader<WorldConfig.Resolved>(LOGGER,
				createConfigLoader(this.configDir.resolve("map.conf")),
				this.configDir.resolve("configs"), new WorldConfig.Resolved(false, null, null)) {
			@Override
			protected WorldConfig.Resolved loadConfig(Path file) throws Exception {
				return WorldConfig.load(file).resolve();
			}
		};
	}

	public ConfigurationLoader<CommentedConfigurationNode> createConfigLoader(Path file) {
		return HoconConfigurationLoader.builder().defaultOptions(this.configOptions).path(file).build();
	}

	@Listener
	public void onServerStarting(StartingEngineEvent<Server> e) {
		loadConfigs();

		this.game.eventManager().registerListeners(this.container, new DropListener(this));

		this.game.eventManager().registerListeners(this.container, new UpdateChecker(LOGGER, this.container,
				createConfigLoader(this.configDir.resolve("update.conf")), "Yeregorix", "AutoPickup"));
	}

	private void loadConfigs() {
		this.configMap = this.configMapLoader.load();
	}

	@Listener
	public void onRefreshGame(RefreshGameEvent e) {
		loadConfigs();
	}

	@Listener
	public void onServerStarted(StartedEngineEvent<Server> e) {
		LOGGER.info("AutoPickup {} was loaded successfully.", this.container.metadata().version());
	}

	public WorldConfig.Resolved getConfig(ServerWorld world) {
		return this.configMap.get(world.properties());
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
