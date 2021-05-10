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
import net.smoofyuniverse.autopickup.util.IOUtil;
import net.smoofyuniverse.autopickup.util.collection.BlockSet;
import net.smoofyuniverse.autopickup.util.collection.BlockSet.SerializationPredicate;
import net.smoofyuniverse.map.WorldMap;
import net.smoofyuniverse.map.WorldMapLoader;
import net.smoofyuniverse.ore.update.UpdateChecker;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

	private WorldMapLoader<WorldConfig.Immutable> configMapLoader;
	private WorldMap<WorldConfig.Immutable> configMap;

	public AutoPickup() {
		if (instance != null)
			throw new IllegalStateException();
		instance = this;
	}

	@Listener
	public void onGamePreInit(GamePreInitializationEvent e) {
		TypeSerializerCollection.defaults().register(BlockSet.TOKEN, new BlockSetSerializer(SerializationPredicate.limit(0.6f)));

		try {
			Files.createDirectories(this.configDir);
		} catch (IOException ignored) {
		}

		this.configMapLoader = new WorldMapLoader<WorldConfig.Immutable>(LOGGER,
				IOUtil.createConfigLoader(this.configDir.resolve("map.conf")),
				this.configDir.resolve("configs"), WorldConfig.DISABLED) {
			@Override
			protected WorldConfig.Immutable loadConfig(Path file) throws Exception {
				return WorldConfig.load(file).toImmutable();
			}
		};
	}

	@Listener
	public void onGameInit(GameInitializationEvent e) {
		loadConfigs();

		this.game.getEventManager().registerListeners(this, new EntityEventListener(this));

		this.game.getEventManager().registerListeners(this, new UpdateChecker(LOGGER, this.container,
				IOUtil.createConfigLoader(this.configDir.resolve("update.conf")), "Yeregorix", "AutoPickup"));
	}

	private void loadConfigs() {
		if (Files.exists(this.configDir.resolve("worlds")) && Files.notExists(this.configDir.resolve("map.conf"))) {
			LOGGER.info("Updating config directory structure ...");
			Path worlds = IOUtil.backup(this.configDir).orElse(this.configDir).resolve("worlds");
			this.configMap = this.configMapLoader.importWorlds(worlds);
		} else {
			this.configMap = this.configMapLoader.load();
		}
	}

	@Listener
	public void onGameReload(GameReloadEvent e) {
		loadConfigs();
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent e) {
		LOGGER.info("AutoPickup " + this.container.getVersion().orElse("?") + " was loaded successfully.");
	}

	public WorldConfig.Immutable getConfig(World world) {
		return this.configMap.get(world.getProperties());
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
