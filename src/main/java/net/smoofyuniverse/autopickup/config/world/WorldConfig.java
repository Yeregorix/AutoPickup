/*
 * Copyright (c) 2018-2021 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.autopickup.config.world;

import com.google.common.reflect.TypeToken;
import net.smoofyuniverse.autopickup.AutoPickup;
import net.smoofyuniverse.autopickup.util.IOUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Path;

@ConfigSerializable
public class WorldConfig {
	public static final int CURRENT_VERSION = 1, MINIMUM__VERSION = 1;
	public static final TypeToken<WorldConfig> TOKEN = TypeToken.of(WorldConfig.class);
	public static final Immutable DISABLED;

	@Setting(value = "Enabled", comment = "Enable or disable AutoPickup in this world")
	public boolean enabled = true;
	@Setting(value = "Entity", comment = "Section related to entities death")
	public EntityPickupConfig entity = new EntityPickupConfig();
	@Setting(value = "Block", comment = "Section related to blocks break")
	public BlockPickupConfig block = new BlockPickupConfig();

	public Immutable toImmutable() {
		return new Immutable(this.enabled, this.entity.toImmutable(), this.block.toImmutable());
	}

	public static WorldConfig load(Path file) throws IOException, ObjectMappingException {
		ConfigurationLoader<CommentedConfigurationNode> loader = IOUtil.createConfigLoader(file);

		CommentedConfigurationNode root = loader.load();
		int version = root.getNode("Version").getInt();
		if ((version > CURRENT_VERSION || version < MINIMUM__VERSION) && IOUtil.backup(file).isPresent()) {
			AutoPickup.LOGGER.info("Your config version is not supported. A new one will be generated.");
			root = loader.createEmptyNode();
		}

		ConfigurationNode cfgNode = root.getNode("Config");
		WorldConfig cfg = cfgNode.getValue(TOKEN, new WorldConfig());

		root.getNode("Version").setValue(CURRENT_VERSION);
		cfgNode.setValue(TOKEN, cfg);
		loader.save(root);
		return cfg;
	}

	public static class Immutable {
		public final boolean enabled;
		public final EntityPickupConfig.Immutable entity;
		public final BlockPickupConfig.Immutable block;

		public Immutable(boolean enabled, EntityPickupConfig.Immutable entity, BlockPickupConfig.Immutable block) {
			this.enabled = enabled;
			this.entity = entity;
			this.block = block;
		}
	}

	static {
		WorldConfig cfg = new WorldConfig();
		cfg.enabled = false;
		DISABLED = cfg.toImmutable();
	}
}
