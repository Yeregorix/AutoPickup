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

package net.smoofyuniverse.autopickup.config.world;

import net.smoofyuniverse.autopickup.message.Message;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashSet;
import java.util.Set;

import static net.smoofyuniverse.autopickup.util.RegistryUtil.resolveEntityTypes;
import static net.smoofyuniverse.autopickup.util.RegistryUtil.resolveItemTypes;

@ConfigSerializable
public class EntityPickupConfig extends PickupConfig {

	@Comment("Disable automatic pickup for the specified entities")
	@Setting("Blacklist-Entities")
	public Set<ResourceKey> blacklistEntities = new HashSet<>();

	@Comment("Enable or disable removing items when it's not caused by a player")
	@Setting("NoDrop-Item")
	public boolean noDropItem = false;

	@Comment("Enable or disable removing experience orbs when it's not caused by a player")
	@Setting("NoDrop-Experience")
	public boolean noDropExperience = false;

	@Override
	public Resolved resolve() {
		return new Resolved(this.autoPickupItem, this.autoPickupExperience, Message.of(this.fullInventoryMessage),
				resolveItemTypes(this.blacklistItems), resolveEntityTypes(this.blacklistEntities),
				this.noDropItem, this.noDropExperience);
	}

	public static class Resolved extends PickupConfig.Resolved {
		public final boolean noDropItem, noDropExperience;
		public final Set<EntityType<?>> blacklistEntities;

		public Resolved(boolean autoPickupItem, boolean autoPickupExperience, Message fullInventoryMessage,
						Set<ItemType> blacklistItems, Set<EntityType<?>> blacklistEntities,
						boolean noDropItem, boolean noDropExperience) {
			super(autoPickupItem, autoPickupExperience, fullInventoryMessage, blacklistItems);
			this.blacklistEntities = Set.of(blacklistEntities.toArray(new EntityType[0]));
			this.noDropItem = noDropItem;
			this.noDropExperience = noDropExperience;
		}
	}
}
