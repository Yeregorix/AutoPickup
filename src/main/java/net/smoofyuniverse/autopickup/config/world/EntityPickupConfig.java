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

package net.smoofyuniverse.autopickup.config.world;

import com.google.common.collect.ImmutableSet;
import net.smoofyuniverse.autopickup.message.Message;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.ItemType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ConfigSerializable
public class EntityPickupConfig extends PickupConfig {
	@Setting(value = "Blacklist-Entities", comment = "Disable automatic pickup for the specified entities")
	public Set<EntityType> blacklistEntities = new HashSet<>();
	@Setting(value = "NoDrop-Item", comment = "Enable or disable removing items when it's not caused by a player")
	public boolean noDropItem = false;
	@Setting(value = "NoDrop-Experience", comment = "Enable or disable removing experience orbs when it's not caused by a player")
	public boolean noDropExperience = false;

	@Override
	public Immutable toImmutable() {
		return new Immutable(this.autoPickupItem, this.autoPickupExperience, Message.of(this.fullInventoryMessage),
				this.blacklistItems, this.blacklistEntities, this.noDropItem, this.noDropExperience);
	}

	public static class Immutable extends PickupConfig.Immutable {
		public final boolean noDropItem, noDropExperience;
		public final Set<EntityType> blacklistEntities;

		public Immutable(boolean autoPickupItem, boolean autoPickupExperience, Message fullInventoryMessage,
						 Collection<ItemType> blacklistItems, Collection<EntityType> blacklistEntities,
						 boolean noDropItem, boolean noDropExperience) {
			super(autoPickupItem, autoPickupExperience, fullInventoryMessage, blacklistItems);
			this.blacklistEntities = ImmutableSet.copyOf(blacklistEntities);
			this.noDropItem = noDropItem;
			this.noDropExperience = noDropExperience;
		}
	}
}
