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

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class WorldConfig {
	public static final int CURRENT_VERSION = 1, MINIMUM__VERSION = 1;
	public static final TypeToken<WorldConfig> TOKEN = TypeToken.of(WorldConfig.class);

	@Setting(value = "Enabled", comment = "Enable or disable AutoPickup in this world")
	public boolean enabled = true;
	@Setting(value = "Entity", comment = "Section related to entities death")
	public ExtendedTypeConfig entity = new ExtendedTypeConfig();
	@Setting(value = "Block", comment = "Section related to blocks break")
	public TypeConfig block = new TypeConfig();

	public Immutable toImmutable() {
		return new Immutable(this.enabled, this.entity.toImmutable(), this.block.toImmutable());
	}

	public static class Immutable {
		public final boolean enabled;
		public final ExtendedTypeConfig.Immutable entity;
		public final TypeConfig.Immutable block;

		public Immutable(boolean enabled, ExtendedTypeConfig.Immutable entity, TypeConfig.Immutable block) {
			this.enabled = enabled;
			this.entity = entity;
			this.block = block;
		}
	}
}
