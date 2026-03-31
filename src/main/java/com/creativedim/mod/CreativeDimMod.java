package com.creativedim.mod;

import com.creativedim.mod.dimension.ModDimensions;
import com.creativedim.mod.event.DimensionEventHandler;
import com.creativedim.mod.inventory.InventoryManager;
import com.creativedim.mod.inventory.PlayerInventoryData;
import com.creativedim.mod.network.ModNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CreativeDimMod.MOD_ID)
public class CreativeDimMod {
    public static final String MOD_ID = "creativedim";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CreativeDimMod(IEventBus modEventBus) {
        LOGGER.info("Creative Dimension mod initializing...");

        ModDimensions.register(modEventBus);
        PlayerInventoryData.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(new DimensionEventHandler());
        NeoForge.EVENT_BUS.register(new InventoryManager());

        LOGGER.info("Creative Dimension event handlers registered.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            LOGGER.info("Creative Dimension mod setup complete.");
        });
    }
}
