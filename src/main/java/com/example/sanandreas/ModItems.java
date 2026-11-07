package com.example.sanandreas;

import com.example.sanandreas.item.SaWeaponItem;
import com.example.sanandreas.weapon.WeaponType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SanAndreasMod.MOD_ID);

    /** Un item por cada WeaponType, en el orden de la hotbar. */
    public static final Map<WeaponType, RegistryObject<SaWeaponItem>> WEAPONS = new EnumMap<>(WeaponType.class);

    /** Solo se usa como "modelo" del proyectil del lanzacohetes. */
    public static final RegistryObject<Item> ROCKET =
            ITEMS.register("rocket", () -> new Item(new Item.Properties()));

    static {
        for (WeaponType t : WeaponType.values()) {
            WEAPONS.put(t, ITEMS.register(t.id, () -> new SaWeaponItem(t, new Item.Properties().stacksTo(1))));
        }
    }
}
