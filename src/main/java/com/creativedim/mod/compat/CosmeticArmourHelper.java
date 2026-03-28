package com.creativedim.mod.compat;

import com.creativedim.mod.CreativeDimMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public class CosmeticArmourHelper {

    private static Boolean modPresent = null;

    public static boolean isModPresent() {
        if (modPresent == null) {
            modPresent = ModList.get().isLoaded("cosmeticarmorreworked");
            CreativeDimMod.LOGGER.info("[CreativeDim] CosmeticArmorReworked {}détecté.",
                modPresent ? "" : "non ");
        }
        return modPresent;
    }

    /**
     * Capture l'inventaire cosmétique du joueur via l'API publique du mod.
     */
    public static CompoundTag capture(ServerPlayer player) {
        if (!isModPresent()) return new CompoundTag();
        try {
            lain.mods.cos.api.inventory.CAStacksBase inv = lain.mods.cos.api.CosArmorAPI
                    .getManager()
                    .getCosArmorInventory(player.getUUID());

            return inv.serializeNBT(player.server.registryAccess());
        } catch (Exception e) {
            CreativeDimMod.LOGGER.warn("[CreativeDim] Erreur capture CosmeticArmor: {}", e.getMessage());
            return new CompoundTag();
        }
    }

    /**
     * Restaure l'inventaire cosmétique du joueur via l'API publique du mod.
     */
    public static void restore(ServerPlayer player, CompoundTag saved) {
        if (!isModPresent()) return;
        try {
            lain.mods.cos.api.inventory.CAStacksBase inv = lain.mods.cos.api.CosArmorAPI
                    .getManager()
                    .getCosArmorInventory(player.getUUID());

            if (saved.isEmpty()) {
                // Vider tous les slots cosmétiques
                for (int i = 0; i < inv.getSlots(); i++) {
                    inv.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
                }
            } else {
                inv.deserializeNBT(player.server.registryAccess(), saved);
            }
        } catch (Exception e) {
            CreativeDimMod.LOGGER.warn("[CreativeDim] Erreur restore CosmeticArmor: {}", e.getMessage());
        }
    }
}
