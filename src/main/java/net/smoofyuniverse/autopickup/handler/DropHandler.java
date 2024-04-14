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

package net.smoofyuniverse.autopickup.handler;

import net.smoofyuniverse.autopickup.config.world.PickupConfig;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.*;

public class DropHandler<C extends PickupConfig.Resolved> {
	private final String category;

	public DropHandler(String category) {
		this.category = category;
	}

	public void handle(C config, AffectEntityEvent event, ServerPlayer player) {
		Set<Entity> entities = new HashSet<>(event.entities());
		handle(config, entities, player);
		event.filterEntities(entities::contains);
	}

	public void handle(C config, Collection<Entity> entities, ServerPlayer player) {
		List<ExperienceOrb> orbs = new ArrayList<>();
		List<Item> items = new ArrayList<>();

		Iterator<Entity> it = entities.iterator();
		while (it.hasNext()) {
			Entity y = it.next();
			if (y instanceof Item) {
				items.add((Item) y);
				it.remove();
			} else if (y instanceof ExperienceOrb) {
				orbs.add((ExperienceOrb) y);
				it.remove();
			}
		}

		if (orbs.isEmpty() && items.isEmpty())
			return;

		if (player != null) {
			handlePlayer(config, orbs, items, player);
		} else {
			handleOther(config, orbs, items);
		}

		entities.addAll(orbs);
		entities.addAll(items);
	}

	protected void handlePlayer(C config, List<ExperienceOrb> orbs, List<Item> items, ServerPlayer player) {
		if (config.autoPickupExperience && !orbs.isEmpty() && player.hasPermission("autopickup." + this.category + ".experience")) {
			int orbsExp = 0;
			for (ExperienceOrb orb : orbs)
				orbsExp += orb.experience().get();

			final int expToAdd = orbsExp;
			if (player.transform(Keys.EXPERIENCE, t -> t + expToAdd).isSuccessful())
				orbs.clear();
		}

		if (config.autoPickupItem && !items.isEmpty() && player.hasPermission("autopickup." + this.category + ".item")) {
			Inventory inv = player.inventory().primary();

			boolean full = false;
			Iterator<Item> it = items.iterator();
			while (it.hasNext()) {
				Item item = it.next();
				ItemStackSnapshot stack = item.item().get();

				if (config.blacklistItems.contains(stack.type()))
					continue;

				List<ItemStackSnapshot> rejectedList = inv.offer(stack.createStack()).rejectedItems();
				ItemStackSnapshot rejected = rejectedList.isEmpty() ? null : rejectedList.get(0);

				if (rejected == null || rejected.isEmpty()) {
					it.remove();
				} else {
					item.offer(item.item().set(rejected));
					full = true;
				}
			}

			if (full)
				config.fullInventoryMessage.sendTo(player);
		}
	}

	protected void handleOther(C config, List<ExperienceOrb> orbs, List<Item> items) {}
}
