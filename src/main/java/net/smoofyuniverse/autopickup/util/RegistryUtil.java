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

package net.smoofyuniverse.autopickup.util;

import net.smoofyuniverse.autopickup.AutoPickup;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

public class RegistryUtil {

	public static Set<BlockState> resolveBlockStates(Iterable<String> keys) {
		Registry<BlockType> blockTypeRegistry = RegistryTypes.BLOCK_TYPE.get();
		BlockState.Builder blockStateBuilder = BlockState.builder();

		Set<BlockState> states = new HashSet<>();
		Set<String> unknownKeys = new HashSet<>(), invalidPatterns = new HashSet<>();

		for (String key : keys) {
			boolean negate = key.startsWith("-");
			if (negate)
				key = key.substring(1);

			if (key.startsWith("regex!")) {
				key = key.substring(6);

				Pattern pattern;
				try {
					pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException e) {
					invalidPatterns.add(key);
					continue;
				}

				Stream<BlockState> matchedStates = blockTypeRegistry.stream()
						.flatMap(type -> type.validStates().stream())
						.filter(state -> pattern.matcher(state.asString()).matches());

				if (negate)
					matchedStates.forEach(states::remove);
				else
					matchedStates.forEach(states::add);
				continue;
			}

			try {
				Optional<BlockType> type = blockTypeRegistry.findValue(ResourceKey.resolve(key));
				if (type.isPresent()) {
					List<BlockState> typeStates = type.get().validStates();
					if (negate)
						typeStates.forEach(states::remove);
					else
						states.addAll(typeStates);
					continue;
				}
			} catch (Exception ignored) {
			}

			try {
				BlockState state = blockStateBuilder.reset().fromString(key).build();
				if (negate)
					states.remove(state);
				else
					states.add(state);
				continue;
			} catch (Exception ignored) {
			}

			unknownKeys.add(key);
		}

		if (!unknownKeys.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Unknown block states:");
			for (String key : unknownKeys)
				sb.append(' ').append(key);
			AutoPickup.LOGGER.warn(sb.toString());
		}

		if (!invalidPatterns.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Invalid block states patterns:");
			for (String pattern : invalidPatterns)
				sb.append(' ').append(pattern);
			AutoPickup.LOGGER.warn(sb.toString());
		}

		return states;
	}

	public static Set<EntityType<?>> resolveEntityTypes(Iterable<ResourceKey> keys) {
		return resolve(RegistryTypes.ENTITY_TYPE.get(), keys, "entity types");
	}

	public static <T> Set<T> resolve(Registry<T> registry, Iterable<ResourceKey> keys, String types) {
		Set<T> values = new HashSet<>();
		Set<ResourceKey> unknownKeys = new HashSet<>();

		for (ResourceKey key : keys) {
			Optional<T> value = registry.findValue(key);
			if (value.isPresent())
				values.add(value.get());
			else
				unknownKeys.add(key);
		}

		if (!unknownKeys.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Unknown ").append(types).append(':');
			for (ResourceKey key : unknownKeys)
				sb.append(' ').append(key.formatted());
			AutoPickup.LOGGER.warn(sb.toString());
		}

		return values;
	}

	public static Set<ItemType> resolveItemTypes(Iterable<ResourceKey> keys) {
		return resolve(RegistryTypes.ITEM_TYPE.get(), keys, "item types");
	}
}
