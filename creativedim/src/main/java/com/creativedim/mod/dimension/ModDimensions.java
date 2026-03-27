package com.creativedim.mod.dimension;

import com.creativedim.mod.CreativeDimMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDimensions {

    /**
     * The ResourceKey identifying our custom dimension.
     * The actual dimension/level data is registered via JSON data files.
     */
    public static final ResourceKey<Level> CREATIVE_DIM_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(CreativeDimMod.MOD_ID, "creative_world")
    );

    public static void register(IEventBus modEventBus) {
        // Dimensions in NeoForge 1.21.1 are data-driven (JSON).
        // This class holds the ResourceKey reference used throughout the code.
        CreativeDimMod.LOGGER.info("Registering Creative Dimension key: {}", CREATIVE_DIM_KEY.location());
    }

    /**
     * Checks if a given level is our custom creative dimension.
     */
    public static boolean isCreativeDimension(Level level) {
        return level.dimension().equals(CREATIVE_DIM_KEY);
    }
}
