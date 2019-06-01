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

import net.smoofyuniverse.autopickup.message.Message;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ExtendedTypeConfig extends TypeConfig {
	@Setting(value = "NoDrop-Item", comment = "Enable or disable removing items when it's not caused by a player")
	public boolean noDropItem = false;
	@Setting(value = "NoDrop-Experience", comment = "Enable or disable removing experience orbs when it's not caused by a player")
	public boolean noDropExperience = false;

	@Override
	public Immutable toImmutable() {
		return new Immutable(this.autoPickupItem, this.autoPickupExperience, Message.of(this.fullInventoryMessage), this.noDropItem, this.noDropExperience);
	}

	public static class Immutable extends TypeConfig.Immutable {
		public final boolean noDropItem, noDropExperience;

		public Immutable(boolean autoPickupItem, boolean autoPickupExperience, Message fullInventoryMessage, boolean noDropItem, boolean noDropExperience) {
			super(autoPickupItem, autoPickupExperience, fullInventoryMessage);
			this.noDropItem = noDropItem;
			this.noDropExperience = noDropExperience;
		}
	}
}
