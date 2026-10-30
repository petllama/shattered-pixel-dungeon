/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Practice;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/** Toggles for the practice-build features. */
public class WndPractice extends Window {

	private static final int WIDTH      = 130;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 2;
	private static final int INFO_SIZE  = 14;

	public WndPractice(){
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2f, GAP);
		PixelScene.align(title);
		add(title);

		float pos = title.bottom() + 2 * GAP;

		for (final String key : Practice.optionKeys()){
			CheckBox box = new CheckBox(Messages.get(this, key)){
				@Override
				protected void onClick() {
					super.onClick();
					Practice.set(key, checked());
				}
			};
			box.checked(Practice.enabled(key));
			box.setRect(0, pos, WIDTH - INFO_SIZE - GAP, BTN_HEIGHT);
			add(box);

			IconButton info = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront(new WndTitledMessage(
							Icons.get(Icons.INFO),
							Messages.get(WndPractice.class, key),
							Messages.get(WndPractice.class, key + "_desc")));
				}
			};
			info.setRect(WIDTH - INFO_SIZE, pos + (BTN_HEIGHT - INFO_SIZE) / 2f, INFO_SIZE, INFO_SIZE);
			add(info);

			pos = box.bottom() + GAP;
		}

		RenderedTextBlock note = PixelScene.renderTextBlock(Messages.get(this, "note"), 6);
		note.maxWidth(WIDTH);
		note.setPos(0, pos + GAP);
		add(note);

		resize(WIDTH, (int)Math.ceil(note.bottom()));
	}
}
