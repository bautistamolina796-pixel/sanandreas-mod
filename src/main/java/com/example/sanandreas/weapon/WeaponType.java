package com.example.sanandreas.weapon;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/** Definicion de cada arma. El orden es el de la hotbar del video (slots 2 a 9). */
public enum WeaponType {
    //        id             kind          clip reserve dmg    delay pel  spread range reload auto   recoil sound                                 vol   pitch
    MINIGUN  ("minigun",     Kind.HITSCAN, 100, 2000,   2.5f,  1,    1,   4.5f,  60,   100,   true,  0.4f,  SoundEvents.DISPENSER_LAUNCH,        0.4f, 1.9f),
    UZI      ("uzi",         Kind.HITSCAN, 50,  500,    2.5f,  2,    1,   4.0f,  50,   55,    true,  0.5f,  SoundEvents.DISPENSER_LAUNCH,        0.6f, 1.7f),
    PISTOL   ("pistol",      Kind.HITSCAN, 17,  500,    6.0f,  7,    1,   0.8f,  70,   40,    false, 1.5f,  SoundEvents.FIREWORK_ROCKET_BLAST,   0.5f, 1.9f),
    SHOTGUN  ("shotgun",     Kind.HITSCAN, 6,   200,    2.5f,  22,   8,   7.0f,  30,   70,    false, 3.0f,  SoundEvents.GENERIC_EXPLODE,        0.4f, 1.8f),
    RPG      ("rpg",         Kind.ROCKET,  1,   30,     0.0f,  40,   1,   0.0f,  0,    55,    false, 4.0f,  SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0f, 0.8f),
    FLAMETHROWER("flamethrower", Kind.FLAME, 100, 1500, 1.5f,  2,    1,   6.0f,  9,    70,    true,  0.0f,  SoundEvents.BLAZE_SHOOT,            0.4f, 1.5f),
    AK47     ("ak47",        Kind.HITSCAN, 30,  500,    4.0f,  3,    1,   2.5f,  80,   55,    true,  0.8f,  SoundEvents.CROSSBOW_SHOOT,         0.7f, 1.6f),
    M4       ("m4",          Kind.HITSCAN, 50,  500,    3.5f,  3,    1,   1.5f,  90,   55,    true,  0.6f,  SoundEvents.CROSSBOW_SHOOT,         0.6f, 1.9f);

    public enum Kind { HITSCAN, ROCKET, FLAME }

    public final String id;
    public final Kind kind;
    public final int clipSize;
    public final int startReserve;
    public final float damage;
    /** Ticks minimos entre disparos. */
    public final int fireDelay;
    public final int pellets;
    /** Dispersion en grados. */
    public final float spreadDeg;
    public final int range;
    public final int reloadTicks;
    public final boolean automatic;
    /** Patada de camara hacia arriba, en grados, por disparo. */
    public final float recoil;
    public final SoundEvent sound;
    public final float volume;
    public final float pitch;

    WeaponType(String id, Kind kind, int clipSize, int startReserve, float damage, int fireDelay, int pellets,
               float spreadDeg, int range, int reloadTicks, boolean automatic, float recoil,
               SoundEvent sound, float volume, float pitch) {
        this.id = id;
        this.kind = kind;
        this.clipSize = clipSize;
        this.startReserve = startReserve;
        this.damage = damage;
        this.fireDelay = fireDelay;
        this.pellets = pellets;
        this.spreadDeg = spreadDeg;
        this.range = range;
        this.reloadTicks = reloadTicks;
        this.automatic = automatic;
        this.recoil = recoil;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }
}
