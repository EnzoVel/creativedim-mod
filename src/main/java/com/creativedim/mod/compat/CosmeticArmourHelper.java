package com.creativedim.mod.compat;

import com.creativedim.mod.CreativeDimMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public class CosmeticArmourHelper {

    private static final String COSMETIC_KEY = "cosmeticarmors";
    private static Boolean modPresent = null;

    public static boolean isModPresent() {
        if (modPresent == null) {
            modPresent = ModList.get().isLoaded("cosmeticarmors");
            CreativeDimMod.LOGGER.info("[CreativeDim] CosmeticArmours {} détecté.",
                modPresent ? "" : "non");
        }
        return modPresent;
    }

    public static CompoundTag capture(ServerPlayer player) {
        if (!isModPresent()) return new CompoundTag();
        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(COSMETIC_KEY)) {
            return persistent.getCompound(COSMETIC_KEY).copy();
        }
        return new CompoundTag();
    }

    public static void restore(ServerPlayer player, CompoundTag saved) {
        if (!isModPresent()) return;
        CompoundTag persistent = player.getPersistentData();
        if (saved.isEmpty()) {
            persistent.remove(COSMETIC_KEY);
        } else {
            persistent.put(COSMETIC_KEY, saved.copy());
        }
    }
}
