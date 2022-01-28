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

package net.smoofyuniverse.autopickup.handler;

import net.smoofyuniverse.autopickup.config.world.EntityPickupConfig;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.Item;

import java.util.List;

public class EntityDropHandler extends DropHandler<EntityPickupConfig.Resolved> {
	public static final EntityDropHandler INSTANCE = new EntityDropHandler();

	public EntityDropHandler() {
		super("entity");
	}

	@Override
	protected void handleOther(EntityPickupConfig.Resolved config, List<ExperienceOrb> orbs, List<Item> items) {
		if (config.noDropExperience)
			orbs.clear();

		if (config.noDropItem)
			items.clear();
	}
}
