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

package net.smoofyuniverse.autopickup.event;

import net.smoofyuniverse.autopickup.AutoPickup;
import net.smoofyuniverse.autopickup.config.world.EntityPickupConfig;
import net.smoofyuniverse.autopickup.config.world.PickupConfig;
import net.smoofyuniverse.autopickup.config.world.WorldConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.util.*;

public class DropListener {
	private final Map<UUID, Data> trackedCauses = new HashMap<>();
	private final AutoPickup plugin;

	public DropListener(AutoPickup plugin) {
		this.plugin = plugin;
	}

	@Listener(order = Order.POST)
	public void onPlayerBreakBlock(ChangeBlockEvent.Break e, @Root Player player) {
		WorldConfig.Immutable cfg = this.plugin.getConfig(player.getWorld());
		if (!cfg.enabled)
			return;

		Set<BlockState> blacklist = cfg.block.blacklistBlocks;
		for (Transaction<BlockSnapshot> t : e.getTransactions()) {
			if (blacklist.contains(t.getOriginal().getState()))
				return;
		}

		track(new Data(false, player.getUniqueId(), player.getUniqueId()), 1);
	}

	private void track(Data data, int ticks) {
		this.trackedCauses.put(data.livingCauseId, data);
		Task.builder().delayTicks(ticks).execute(() -> this.trackedCauses.remove(data.livingCauseId)).submit(this.plugin);
	}

	@Listener(order = Order.POST)
	public void onEntityDeath(DestructEntityEvent.Death e) {
		Living entity = e.getTargetEntity();
		if (entity instanceof Humanoid || entity instanceof ArmorStand)
			return;

		WorldConfig.Immutable cfg = this.plugin.getConfig(entity.getWorld());
		if (!cfg.enabled || cfg.entity.blacklistEntities.contains(entity.getType()))
			return;

		track(new Data(true, entity.getUniqueId(), entity.lastAttacker().get().flatMap(EntitySnapshot::getUniqueId).orElse(null)), 21);
	}

	@Listener(order = Order.EARLY)
	public void onSpawnEntity(SpawnEntityEvent e, @First Living livingCause) {
		Data data = this.trackedCauses.get(livingCause.getUniqueId());
		if (data == null)
			return;

		World world = null;
		for (Entity y : e.getEntities()) {
			if (world == null) {
				world = y.getWorld();
			} else if (world != y.getWorld()) {
				AutoPickup.LOGGER.debug("A SpawnEntityEvent with entities from different worlds have been detected. This is not supported by the plugin.");
				return;
			}
		}

		if (world == null)
			return;

		WorldConfig.Immutable cfg = this.plugin.getConfig(world);
		if (!cfg.enabled)
			return;

		List<ExperienceOrb> orbs = new ArrayList<>();
		List<Item> items = new ArrayList<>();
		{
			Iterator<Entity> it = e.getEntities().iterator();
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
		}
		if (orbs.isEmpty() && items.isEmpty())
			return;

		Player player = data.playerId == null ? null : Sponge.getServer().getPlayer(data.playerId).orElse(null);
		if (player != null) {
			PickupConfig.Immutable pickupConfig;
			String typeId;
			if (data.entity) {
				pickupConfig = cfg.entity;
				typeId = "entity";
			} else {
				pickupConfig = cfg.block;
				typeId = "block";
			}

			if (pickupConfig.autoPickupExperience && !orbs.isEmpty() && player.hasPermission("autopickup." + typeId + ".experience")) {
				int exp = 0;
				for (ExperienceOrb orb : orbs)
					exp += orb.experience().get();

				final int expf = exp;
				if (player.transform(Keys.TOTAL_EXPERIENCE, t -> t + expf).isSuccessful())
					orbs.clear();
			}

			if (pickupConfig.autoPickupItem && !items.isEmpty() && player.hasPermission("autopickup." + typeId + ".item")) {
				Inventory inv = ((PlayerInventory) player.getInventory()).getMain();

				boolean full = false;
				Iterator<Item> it = items.iterator();
				while (it.hasNext()) {
					Item item = it.next();
					ItemStack stack = item.item().get().createStack();

					if (pickupConfig.blacklistItems.contains(stack.getType()))
						continue;

					inv.offer(stack);

					if (stack.isEmpty()) {
						it.remove();
					} else {
						item.offer(item.item().set(stack.createSnapshot()));
						full = true;
					}
				}

				if (full)
					pickupConfig.fullInventoryMessage.sendTo(player);
			}
		} else if (data.entity) {
			EntityPickupConfig.Immutable pickupConfig = cfg.entity;

			if (pickupConfig.noDropExperience)
				orbs.clear();

			if (pickupConfig.noDropItem)
				items.clear();
		}

		e.getEntities().addAll(orbs);
		e.getEntities().addAll(items);
	}

	private static class Data {
		public final boolean entity;
		public final UUID livingCauseId;
		public final UUID playerId;

		public Data(boolean entity, UUID livingCauseId, UUID playerId) {
			if (livingCauseId == null)
				throw new IllegalArgumentException("livingCauseId");

			this.entity = entity;
			this.livingCauseId = livingCauseId;
			this.playerId = playerId;
		}
	}
}
