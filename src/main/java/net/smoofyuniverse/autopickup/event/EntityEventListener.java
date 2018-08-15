/*
 * Copyright (c) 2018 Hugo Dupanloup (Yeregorix)
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
import net.smoofyuniverse.autopickup.config.world.ExtendedTypeConfig;
import net.smoofyuniverse.autopickup.config.world.TypeConfig;
import net.smoofyuniverse.autopickup.config.world.WorldConfig;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.util.*;

public class EntityEventListener {
	private final Map<UUID, Data> trackedDatas = new HashMap<>();

	@Listener(order = Order.POST)
	public void onPlayerBreakBlock(ChangeBlockEvent.Break e, @Root Player p) {
		if (AutoPickup.get().isEnabled(p.getWorld()))
			track(new Data(Type.BLOCK, p.getUniqueId(), p), 2);
	}

	private void track(Data data, int ticks) {
		this.trackedDatas.put(data.livingId, data);
		Task.builder().delayTicks(ticks).execute(() -> this.trackedDatas.remove(data.livingId)).submit(AutoPickup.get());
	}

	@Listener(order = Order.POST)
	public void onEntityDeath(DestructEntityEvent.Death e) {
		Living living = e.getTargetEntity();
		if (living instanceof Humanoid || living instanceof ArmorStand)
			return;

		if (AutoPickup.get().isEnabled(living.getWorld()))
			track(new Data(Type.ENTITY, living.getUniqueId(), living.lastAttacker().get().flatMap(EntitySnapshot::getUniqueId).flatMap(id -> Sponge.getServer().getPlayer(id)).orElse(null)), 22);
	}

	@Listener(order = Order.EARLY)
	public void onSpawnEntity(SpawnEntityEvent e) {
		boolean uniqueWorld = true;
		World world = null;

		for (Entity y : e.getEntities()) {
			if (world == null)
				world = y.getWorld();
			else if (world != y.getWorld()) {
				uniqueWorld = false;
				break;
			}
		}

		if (!uniqueWorld) {
			AutoPickup.LOGGER.debug("A SpawnEntityEvent with entities from different worlds have been detected. This is not supported by the plugin.");
			return;
		}

		if (world == null || !AutoPickup.get().isEnabled(world))
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

		if (!orbs.isEmpty() || !items.isEmpty()) {
			Living livingCause = e.getCause().first(Living.class).orElse(null);
			if (livingCause != null) {
				Data data = this.trackedDatas.get(livingCause.getUniqueId());
				if (data != null) {
					WorldConfig.Immutable worldConfig = AutoPickup.get().getConfig(world).get();
					TypeConfig.Immutable typeConfig = data.type == Type.ENTITY ? worldConfig.entity : worldConfig.block;

					if (data.player != null) {
						if (typeConfig.autoPickupExperience && !orbs.isEmpty() && data.player.hasPermission("autopickup." + data.type.id + ".experience")) {
							int exp = 0;
							for (ExperienceOrb orb : orbs)
								exp += orb.experience().get();

							final int expf = exp;
							if (data.player.transform(Keys.TOTAL_EXPERIENCE, t -> t + expf).isSuccessful())
								orbs.clear();
						}

						if (typeConfig.autoPickupItem && !items.isEmpty() && data.player.hasPermission("autopickup." + data.type.id + ".item")) {
							Inventory inv = ((PlayerInventory) data.player.getInventory()).getMain();

							Iterator<Item> it = items.iterator();
							while (it.hasNext()) {
								Item item = it.next();
								ItemStack stack = item.item().get().createStack();

								inv.offer(stack);

								if (stack.isEmpty())
									it.remove();
								else
									item.offer(item.item().set(stack.createSnapshot()));
							}
						}
					} else if (typeConfig instanceof ExtendedTypeConfig.Immutable) {
						if (((ExtendedTypeConfig.Immutable) typeConfig).noDropExperience)
							orbs.clear();

						if (((ExtendedTypeConfig.Immutable) typeConfig).noDropItem)
							items.clear();
					}
				}
			}

			e.getEntities().addAll(orbs);
			e.getEntities().addAll(items);
		}
	}

	private static enum Type {
		ENTITY("entity"), BLOCK("block");

		public final String id;

		private Type(String id) {
			this.id = id;
		}
	}

	private static class Data {
		public final Type type;
		public final UUID livingId;
		public final Player player;

		public Data(Type type, UUID livingId, Player player) {
			if (type == null)
				throw new IllegalArgumentException("type");
			if (livingId == null)
				throw new IllegalArgumentException("livingId");

			this.type = type;
			this.livingId = livingId;
			this.player = player;
		}
	}
}
