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

import com.google.common.collect.ImmutableSet;
import net.smoofyuniverse.autopickup.message.Message;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static net.smoofyuniverse.autopickup.util.RegistryUtil.resolveBlockStates;
import static net.smoofyuniverse.autopickup.util.RegistryUtil.resolveItemTypes;

@ConfigSerializable
public class BlockPickupConfig extends PickupConfig {

	@Comment("Disable automatic pickup for the specified blocks")
	@Setting("Blacklist-Blocks")
	public List<String> blacklistBlocks = new ArrayList<>();

	@Override
	public Resolved resolve() {
		return new Resolved(this.autoPickupItem, this.autoPickupExperience, Message.of(this.fullInventoryMessage),
				resolveItemTypes(this.blacklistItems), resolveBlockStates(this.blacklistBlocks));
	}

	public static class Resolved extends PickupConfig.Resolved {
		public final Set<BlockState> blacklistBlocks;

		public Resolved(boolean autoPickupItem, boolean autoPickupExperience, Message fullInventoryMessage,
						Collection<ItemType> blacklistItems, Collection<BlockState> blacklistBlocks) {
			super(autoPickupItem, autoPickupExperience, fullInventoryMessage, blacklistItems);
			this.blacklistBlocks = ImmutableSet.copyOf(blacklistBlocks);
		}
	}
}
