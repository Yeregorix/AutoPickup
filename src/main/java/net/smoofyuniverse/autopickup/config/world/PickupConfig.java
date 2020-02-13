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
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.ItemType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ConfigSerializable
public class PickupConfig {
	@Setting(value = "AutoPickup-Item", comment = "Enable or disable automatic pickup for items")
	public boolean autoPickupItem = true;
	@Setting(value = "AutoPickup-Experience", comment = "Enable or disable automatic pickup for experience orbs")
	public boolean autoPickupExperience = true;
	@Setting(value = "FullInventory-Message", comment = "Message sent to the player when an item can't be picked up")
	public String fullInventoryMessage = "(action_bar)&4Your inventory is full.";
	@Setting(value = "Blacklist-Items", comment = "Disable automatic pickup for the specified items")
	public Set<ItemType> blacklistItems = new HashSet<>();

	public Immutable toImmutable() {
		return new Immutable(this.autoPickupItem, this.autoPickupExperience, Message.of(this.fullInventoryMessage),
				this.blacklistItems);
	}

	public static class Immutable {
		public final boolean autoPickupItem, autoPickupExperience;
		public final Message fullInventoryMessage;
		public final Set<ItemType> blacklistItems;

		public Immutable(boolean autoPickupItem, boolean autoPickupExperience, Message fullInventoryMessage,
						 Collection<ItemType> blacklistItems) {
			this.autoPickupItem = autoPickupItem;
			this.autoPickupExperience = autoPickupExperience;
			this.fullInventoryMessage = fullInventoryMessage;
			this.blacklistItems = ImmutableSet.copyOf(blacklistItems);
		}
	}
}
