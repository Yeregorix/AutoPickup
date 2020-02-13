/*
 * Copyright (c) 2018-2019 Hugo Dupanloup (Yeregorix)
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

import com.google.common.collect.ImmutableSet;
import net.smoofyuniverse.autopickup.message.Message;
import net.smoofyuniverse.autopickup.util.collection.BlockSet;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.ItemType;

import java.util.Collection;
import java.util.Set;

@ConfigSerializable
public class BlockPickupConfig extends PickupConfig {
	@Setting(value = "Blacklist-Blocks", comment = "Disable automatic pickup for the specified blocks")
	public BlockSet blacklistBlocks = new BlockSet();

	@Override
	public Immutable toImmutable() {
		return new Immutable(this.autoPickupItem, this.autoPickupExperience, Message.of(this.fullInventoryMessage),
				this.blacklistItems, this.blacklistBlocks.getAll());
	}

	public static class Immutable extends PickupConfig.Immutable {
		public final Set<BlockState> blacklistBlocks;

		public Immutable(boolean autoPickupItem, boolean autoPickupExperience, Message fullInventoryMessage,
						 Collection<ItemType> blacklistItems, Collection<BlockState> blacklistBlocks) {
			super(autoPickupItem, autoPickupExperience, fullInventoryMessage, blacklistItems);
			this.blacklistBlocks = ImmutableSet.copyOf(blacklistBlocks);
		}
	}
}
