# Shattered Pixel Dungeon

[Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/) is an open-source traditional roguelike dungeon crawler with randomized levels and enemies, and hundreds of items to collect and use. It's based on the [source code of Pixel Dungeon](https://github.com/00-Evan/pixel-dungeon-gradle), by [Watabou](https://watabou.itch.io/).

This fork ("SPD Practice") adds practice/learning features that are not part of the official upstream releases linked below. Every feature is a toggle in the **Practice** window (in-game menu, or the hero select screen's game options), and all default to on:
- **Free identify**: inventory items can always be manually identified through a persistent `Identify` action with no resource cost and no turn cost.
- **Floor retry**: on death, `Continue` restores the exact beginning state of the current floor and discards the failed attempt.
- **Damage log**: the last 12 hits you took (turn, source, damage, HP left), from the game menu or the death window.
- **Enemy stats**: enemy info windows show HP, damage/armor ranges, hit chances both ways and rough attacks-to-kill estimates (sampled on an isolated RNG, so it never changes the run).
- **Seeds & challenges**: custom seeds, daily runs, challenges and full randomize without needing a win first.
- **All heroes**: every hero class is playable without unlocking it.

The Android build uses its own app id (`com.petllama.spdpractice`, "SPD Practice") so it installs alongside official Shattered PD, and it never prompts to update to upstream releases. Release builds are signed when `spdKeystore`, `spdKeystorePassword`, `spdKeyAlias` and `spdKeyPassword` are set in `~/.gradle/gradle.properties`.

Shattered Pixel Dungeon currently compiles for Android, iOS, and Desktop platforms. You can find official releases of the game on:

[![Get it on Google Play](https://shatteredpixel.com/assets/images/badges/gplay.png)](https://play.google.com/store/apps/details?id=com.shatteredpixel.shatteredpixeldungeon)
[![Download on the App Store](https://shatteredpixel.com/assets/images/badges/appstore.png)](https://apps.apple.com/app/shattered-pixel-dungeon/id1563121109)
[![Steam](https://shatteredpixel.com/assets/images/badges/steam.png)](https://store.steampowered.com/app/1769170/Shattered_Pixel_Dungeon/)<br>
[![GOG.com](https://shatteredpixel.com/assets/images/badges/gog.png)](https://www.gog.com/game/shattered_pixel_dungeon)
[![Itch.io](https://shatteredpixel.com/assets/images/badges/itch.png)](https://shattered-pixel.itch.io/shattered-pixel-dungeon)
[![Github Releases](https://shatteredpixel.com/assets/images/badges/github.png)](https://github.com/00-Evan/shattered-pixel-dungeon/releases)

If you like this game, please consider [supporting the creator on Patreon](https://www.patreon.com/ShatteredPixel)!

There is an official blog for the original project at [ShatteredPixel.com](https://www.shatteredpixel.com/blog/).

The game also has a translation project hosted on [Transifex](https://explore.transifex.com/shattered-pixel/shattered-pixel-dungeon/).


If you'd like to work with the code, you can find the following guides in `/docs`:
- [Compiling for Android.](docs/getting-started-android.md)
    - **[If you plan to distribute on Google Play please read the end of this guide.](docs/getting-started-android.md#distributing-your-app)**
- [Compiling for desktop platforms.](docs/getting-started-desktop.md)
- [Compiling for iOS.](docs/getting-started-ios.md)
- [Recommended changes for making your own version.](docs/recommended-changes.md)
