/*
 * Copyright (c) 2018-2022 Hugo Dupanloup (Yeregorix)
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

import net.smoofyuniverse.autopickup.AutoPickup;
import net.smoofyuniverse.autopickup.util.IOUtil;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.io.IOException;
import java.nio.file.Path;

@ConfigSerializable
public class WorldConfig {
	public static final int CURRENT_VERSION = 2, MINIMUM__VERSION = 2;

	@Comment("Enable or disable AutoPickup in this world")
	@Setting("Enabled")
	public boolean enabled = true;

	@Comment("Section related to entities death")
	@Setting("Entity")
	public EntityPickupConfig entity = new EntityPickupConfig();

	@Comment("Section related to blocks break")
	@Setting("Block")
	public BlockPickupConfig block = new BlockPickupConfig();

	public Resolved resolve() {
		return new Resolved(this.enabled, this.entity.resolve(), this.block.resolve());
	}

	public static WorldConfig load(Path file) throws IOException {
		ConfigurationLoader<CommentedConfigurationNode> loader = AutoPickup.get().createConfigLoader(file);

		CommentedConfigurationNode root = loader.load();
		int version = root.node("Version").getInt();
		if ((version > CURRENT_VERSION || version < MINIMUM__VERSION) && IOUtil.backup(file).isPresent()) {
			AutoPickup.LOGGER.info("Your config version is not supported. A new one will be generated.");
			root = loader.createNode();
		}

		ConfigurationNode cfgNode = root.node("Config");
		WorldConfig cfg = cfgNode.get(WorldConfig.class, new WorldConfig());

		root.node("Version").set(CURRENT_VERSION);
		cfgNode.set(cfg);
		loader.save(root);
		return cfg;
	}

	public static class Resolved {
		public final boolean enabled;
		public final EntityPickupConfig.Resolved entity;
		public final BlockPickupConfig.Resolved block;

		public Resolved(boolean enabled, EntityPickupConfig.Resolved entity, BlockPickupConfig.Resolved block) {
			this.enabled = enabled;
			this.entity = entity;
			this.block = block;
		}
	}
}
