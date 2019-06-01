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

package net.smoofyuniverse.autopickup.message;

import net.smoofyuniverse.autopickup.util.TextUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;

public interface Message {

	Text getText();

	void sendTo(Player p);

	public static Message of(String value) {
		if (value == null || value.isEmpty())
			return EmptyMessage.INSTANCE;

		Text text = TextUtil.deserialize(value);
		if (text.isEmpty())
			return EmptyMessage.INSTANCE;

		String type = null;
		if (value.charAt(0) == '(') {
			int i = value.indexOf(')');
			if (i != -1)
				type = value.substring(1, i);
		}

		if (type == null)
			return new ChatMessage(text);

		Text text2 = TextUtil.deserialize(value.substring(type.length() + 2));
		if (text2.isEmpty())
			return EmptyMessage.INSTANCE;

		type = type.toLowerCase();

		if (type.equals("chat"))
			return new ChatMessage(text2);

		if (type.equals("action_bar"))
			return new ChatMessage(ChatTypes.ACTION_BAR, text2);

		if (type.startsWith("title")) {
			int ticks = 20;

			if (type.length() != 5) {
				try {
					ticks = Integer.parseInt(type.substring(5));
				} catch (NumberFormatException e) {
					ticks = -1;
				}
			}

			return ticks >= 0 ? new TitleMessage(text2, ticks) : new ChatMessage(text);
		}

		if (type.startsWith("subtitle")) {
			int ticks = 20;

			if (type.length() != 8) {
				try {
					ticks = Integer.parseInt(type.substring(8));
				} catch (NumberFormatException e) {
					ticks = -1;
				}
			}

			return ticks >= 0 ? new SubtitleMessage(text2, ticks) : new ChatMessage(text);
		}

		return new ChatMessage(text);
	}
}
