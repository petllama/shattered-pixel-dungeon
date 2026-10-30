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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Practice-build helpers: option toggles, the death recap log and enemy stat estimates.
 * All options default to ON and are changed from WndPractice.
 */
public class Practice {

	// ---------- option toggles ----------

	public static final String KEY_IDENTIFY   = "practice_identify";
	public static final String KEY_CHECKPOINT = "practice_checkpoint";
	public static final String KEY_OPTIONS    = "practice_options";
	public static final String KEY_HEROES     = "practice_heroes";
	public static final String KEY_MOB_STATS  = "practice_mob_stats";
	public static final String KEY_RECAP      = "practice_recap";

	public static boolean enabled( String key ){
		return SPDSettings.getBoolean( key, true );
	}

	public static void set( String key, boolean value ){
		SPDSettings.put( key, value );
	}

	public static boolean identifyEnabled(){   return enabled(KEY_IDENTIFY); }
	public static boolean checkpointEnabled(){ return enabled(KEY_CHECKPOINT); }
	public static boolean mobStatsEnabled(){   return enabled(KEY_MOB_STATS); }
	public static boolean recapEnabled(){      return enabled(KEY_RECAP); }
	public static boolean allHeroesEnabled(){  return enabled(KEY_HEROES); }

	/** custom seeds, daily runs, challenges and full randomize normally need a win first */
	public static boolean runOptionsUnlocked(){
		return enabled(KEY_OPTIONS)
				|| Badges.isUnlocked(Badges.Badge.VICTORY)
				|| DeviceCompat.isDebug();
	}

	// ---------- death recap ----------

	public static final int RECAP_SIZE = 12;

	private static class DamageEvent {
		int turn;
		int depth;
		String source;
		int damage;
		int hpAfter;
		int maxHP;
	}

	private static final ArrayDeque<DamageEvent> recap = new ArrayDeque<>();

	public static synchronized void clearRecap(){
		recap.clear();
	}

	public static synchronized void recordDamage( Object src, int damage, Hero hero ){
		if (damage <= 0 || hero == null) return;
		DamageEvent e = new DamageEvent();
		e.turn = (int)Math.floor(Actor.now());
		e.depth = Dungeon.depth;
		e.source = describe(src);
		e.damage = damage;
		e.hpAfter = Math.max(0, hero.HP) + hero.shielding();
		e.maxHP = hero.HT;
		recap.addLast(e);
		while (recap.size() > RECAP_SIZE) recap.removeFirst();
	}

	public static synchronized String recapText(){
		if (recap.isEmpty()){
			return Messages.get(Practice.class, "recap_empty");
		}

		StringBuilder sb = new StringBuilder();
		int total = 0;
		DamageEvent biggest = null;
		int firstTurn = recap.peekFirst().turn;
		int lastTurn = recap.peekLast().turn;
		for (DamageEvent e : recap){
			total += e.damage;
			if (biggest == null || e.damage >= biggest.damage) biggest = e;
		}

		sb.append(Messages.get(Practice.class, "recap_summary",
				recap.size(), total, Math.max(1, lastTurn - firstTurn + 1), biggest.source, biggest.damage));
		sb.append("\n");

		for (DamageEvent e : recap){
			sb.append("\n");
			sb.append(Messages.get(Practice.class, "recap_line",
					e.turn, e.source, e.damage, e.hpAfter, e.maxHP));
		}

		DamageEvent last = recap.peekLast();
		if (last.hpAfter <= 0){
			sb.append("\n\n");
			sb.append(Messages.get(Practice.class, "recap_killer", last.source));
		}
		return sb.toString();
	}

	public static String describe( Object src ){
		if (src == null) return Messages.get(Practice.class, "unknown");
		try {
			if (src instanceof Char) return Messages.titleCase(((Char) src).name());
			if (src instanceof Item) return Messages.titleCase(((Item) src).name());
			if (src instanceof Buff) {
				String n = ((Buff) src).name();
				if (validName(n)) return Messages.titleCase(n);
			}
		} catch (Exception ignored){
			//fall through to class-based naming
		}

		Class<?> c = src instanceof Class ? (Class<?>) src : src.getClass();
		while (c != null && c.isAnonymousClass()) c = c.getEnclosingClass();
		if (c == null) return Messages.get(Practice.class, "unknown");

		String n = Messages.get(c, "name");
		if (validName(n)) return Messages.titleCase(n);

		return humanize(c.getSimpleName());
	}

	private static boolean validName( String n ){
		return n != null && !n.isEmpty() && !n.equals(Messages.NO_TEXT_FOUND);
	}

	private static String humanize( String s ){
		if (s == null || s.isEmpty()) return Messages.get(Practice.class, "unknown");
		return s.replaceAll("([a-z])([A-Z])", "$1 $2");
	}

	// ---------- enemy stats ----------

	private static final int SAMPLES = 400;

	/** P(Random.Float(acc) >= Random.Float(eva)), ignoring temporary buffs */
	private static float hitChance( float acc, float eva ){
		if (eva >= Char.INFINITE_EVASION) return 0f;
		if (acc >= Char.INFINITE_ACCURACY) return 1f;
		if (eva <= 0) return 1f;
		if (acc <= 0) return 0f;
		if (acc >= eva) return 1f - eva / (2f * acc);
		return acc / (2f * eva);
	}

	private static String pct( float f ){
		return Math.round(f * 100) + "%";
	}

	private static String fmt( float f ){
		return String.format(Locale.ENGLISH, "%.1f", f);
	}

	public static String mobStats( Mob mob ){
		Hero hero = Dungeon.hero;
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n_").append(Messages.get(Practice.class, "stats_title")).append("_");

		//all random sampling happens on an isolated generator so it can't affect the run
		Random.pushGenerator( 0x5eed5eedL );
		try {
			int dmgMin = Integer.MAX_VALUE, dmgMax = 0; long dmgSum = 0;
			int drMin = Integer.MAX_VALUE, drMax = 0; long drSum = 0;
			int[] mobDmg = new int[SAMPLES];
			int[] mobDr = new int[SAMPLES];
			for (int i = 0; i < SAMPLES; i++){
				int d = mob.damageRoll();
				int r = mob.drRoll();
				mobDmg[i] = d; mobDr[i] = r;
				dmgMin = Math.min(dmgMin, d); dmgMax = Math.max(dmgMax, d); dmgSum += d;
				drMin = Math.min(drMin, r); drMax = Math.max(drMax, r); drSum += r;
			}

			sb.append("\n").append(Messages.get(Practice.class, "stats_hp", mob.HP, mob.HT));
			if (mob.shielding() > 0) sb.append(" ").append(Messages.get(Practice.class, "stats_shield", mob.shielding()));
			sb.append("\n").append(Messages.get(Practice.class, "stats_damage", dmgMin, dmgMax, fmt(dmgSum / (float)SAMPLES)));
			sb.append("\n").append(Messages.get(Practice.class, "stats_armor", drMin, drMax));

			if (hero == null || !hero.isAlive()) return sb.toString();

			int mobAcc = mob.attackSkill(hero);
			int mobEva = mob.defenseSkill(hero);
			//sleeping/unaware/paralysed enemies currently have 0 evasion, also show the alert value
			int alertEva = Math.max(mobEva, mob.defenseSkill);
			boolean surprised = mobEva < alertEva;
			sb.append("\n").append(Messages.get(Practice.class, "stats_skill", mobAcc, alertEva));
			if (surprised) sb.append(" ").append(Messages.get(Practice.class, "stats_surprised"));

			//hero accuracy/evasion can have one-shot side effects from a few talents and stances,
			// so only query them when those trackers aren't active
			boolean safeHero = hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo.ParryTracker.class) == null
					&& hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.LiquidAgilACCTracker.class) == null;
			if (!safeHero) return sb.toString();

			int heroAcc = hero.attackSkill(mob);
			int heroEva = hero.defenseSkill(mob);
			float mobHits = hitChance(mobAcc, heroEva);
			float heroHits = hitChance(heroAcc, mobEva);
			sb.append("\n").append(Messages.get(Practice.class, "stats_hit_you", pct(mobHits)));
			if (surprised){
				float alertHits = hitChance(heroAcc, alertEva);
				sb.append("\n").append(Messages.get(Practice.class, "stats_you_hit_surprised", pct(heroHits), pct(alertHits)));
				heroHits = alertHits; //kill estimates assume a normal fight
			} else {
				sb.append("\n").append(Messages.get(Practice.class, "stats_you_hit", pct(heroHits)));
			}

			//its damage after your armor
			long mobNet = 0;
			for (int i = 0; i < SAMPLES; i++){
				mobNet += Math.max(0, mobDmg[i] - hero.drRoll());
			}
			float mobPerHit = mobNet / (float)SAMPLES;

			//your damage after its armor (base weapon/fist roll, no enchants or abilities)
			int heroMin, heroMax;
			KindOfWeapon wep = hero.belongings.weapon();
			if (wep == null || RingOfForce.fightingUnarmed(hero)){
				heroMin = heroMax = -1;
			} else {
				int bonus = RingOfForce.armedDamageBonus(hero);
				heroMin = wep.min() + bonus;
				heroMax = wep.max() + bonus;
			}
			long heroNet = 0;
			for (int i = 0; i < SAMPLES; i++){
				int d = heroMin >= 0 ? Random.NormalIntRange(heroMin, heroMax) : RingOfForce.damageRoll(hero);
				heroNet += Math.max(0, d - mobDr[i]);
			}
			float heroPerHit = heroNet / (float)SAMPLES;

			sb.append("\n").append(Messages.get(Practice.class, "stats_per_hit", fmt(mobPerHit), fmt(heroPerHit)));

			float heroPerAttack = heroPerHit * heroHits;
			float mobPerAttack = mobPerHit * mobHits;
			String toKillIt = heroPerAttack > 0 ? String.valueOf((int)Math.ceil((mob.HP + mob.shielding()) / heroPerAttack)) : "-";
			String toKillYou = mobPerAttack > 0 ? String.valueOf((int)Math.ceil((hero.HP + hero.shielding()) / mobPerAttack)) : "-";
			sb.append("\n").append(Messages.get(Practice.class, "stats_turns", toKillIt, toKillYou));

		} catch (Exception e){
			//some unusual enemies can't be sampled outside of combat, just skip them
		} finally {
			Random.popGenerator();
		}
		return sb.toString();
	}

	/** labels for the practice option checkboxes, in display order */
	public static ArrayList<String> optionKeys(){
		ArrayList<String> keys = new ArrayList<>();
		keys.add(KEY_IDENTIFY);
		keys.add(KEY_CHECKPOINT);
		keys.add(KEY_RECAP);
		keys.add(KEY_MOB_STATS);
		keys.add(KEY_OPTIONS);
		keys.add(KEY_HEROES);
		return keys;
	}
}
