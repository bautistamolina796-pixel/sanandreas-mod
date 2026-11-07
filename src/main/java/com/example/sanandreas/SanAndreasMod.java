package com.example.sanandreas;

import com.example.sanandreas.network.ModNetwork;
import com.example.sanandreas.weapon.WeaponType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SanAndreasMod.MOD_ID)
public class SanAndreasMod {
    public static final String MOD_ID = "sanandreas";

    public SanAndreasMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);
        bus.addListener(this::commonSetup);
        bus.addListener(this::addCreative);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            for (WeaponType type : WeaponType.values()) {
                event.accept(ModItems.WEAPONS.get(type).get().getDefaultInstance());
            }
        }
    }
}
