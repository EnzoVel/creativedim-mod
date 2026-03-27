package com.creativedim.mod.item;

import com.creativedim.mod.CreativeDimMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CreativeDimMod.MOD_ID);

    /**
     * The Portal Activator: right-click to teleport to/from the creative dimension.
     * Survival players must have this item in hand to travel.
     */
    public static final DeferredItem<Item> PORTAL_ACTIVATOR =
            ITEMS.register("portal_activator", () ->
                    new PortalActivatorItem(new Item.Properties()
                            .stacksTo(1)
                            .fireResistant()
                    )
            );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
