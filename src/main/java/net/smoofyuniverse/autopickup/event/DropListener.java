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

package net.smoofyuniverse.autopickup.event;

import net.smoofyuniverse.autopickup.AutoPickup;
import net.smoofyuniverse.autopickup.config.world.BlockPickupConfig;
import net.smoofyuniverse.autopickup.config.world.WorldConfig;
import net.smoofyuniverse.autopickup.handler.BlockDropHandler;
import net.smoofyuniverse.autopickup.handler.EntityDropHandler;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.world.server.ServerWorld;

public class DropListener {
	private final AutoPickup plugin;

	public DropListener(AutoPickup plugin) {
		this.plugin = plugin;
	}

	@Exclude(SpawnEntityEvent.Pre.class)
	@Listener(order = Order.EARLY)
	public void onSpawnEntity_ChangeBlock(SpawnEntityEvent e, @Root BlockSnapshot block, @First ChangeBlockEvent change, @First ServerPlayer player) {
		WorldConfig.Resolved config = this.plugin.getConfig(player.world());
		if (!config.enabled)
			return;

		BlockPickupConfig.Resolved blockConfig = config.block;
		if (blockConfig.blacklistBlocks.contains(block.state()))
			return;

		BlockDropHandler.INSTANCE.handle(blockConfig, e, player);
	}

	@Exclude(SpawnEntityEvent.Pre.class)
	@Listener(order = Order.EARLY)
	public void onSpawnEntity_HarvestLiving(SpawnEntityEvent e, @Root HarvestEntityEvent harvest) {
		Entity entity = harvest.entity();
		if (!(entity instanceof Living) || entity instanceof Humanoid || entity instanceof ArmorStand)
			return;

		WorldConfig.Resolved config = this.plugin.getConfig((ServerWorld) entity.world());
		if (!config.enabled)
			return;

		net.smoofyuniverse.autopickup.config.world.EntityPickupConfig.Resolved entityConfig = config.entity;
		if (entityConfig.blacklistEntities.contains(entity.type()))
			return;

		EntityDropHandler.INSTANCE.handle(entityConfig, e, (ServerPlayer) entity.get(Keys.LAST_ATTACKER).filter(y -> y instanceof ServerPlayer).orElse(null));
	}
}
