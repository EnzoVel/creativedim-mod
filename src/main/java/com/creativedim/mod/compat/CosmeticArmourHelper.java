package com.creativedim.mod.compat;

import com.creativedim.mod.CreativeDimMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Isole les données de CosmeticArmorReworked entre la dimension créative
 * et les dimensions normales.
 *
 * CosmeticArmorReworked stocke ses données dans des fichiers séparés :
 *   world/playerdata/<uuid>.cosarmor
 *
 * On sauvegarde/restaure ces fichiers lors des transitions de dimension,
 * de la même façon que notre propre système d'inventaire.
 */
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
     * Sauvegarde le fichier .cosarmor actuel vers un fichier de backup
     * (.cosarmor_normal ou .cosarmor_creative selon la destination).
     */
    public static void saveForNormal(ServerPlayer player) {
        if (!isModPresent()) return;
        copyFile(getCosArmorFile(player), getNormalBackup(player));
    }

    public static void saveForCreative(ServerPlayer player) {
        if (!isModPresent()) return;
        copyFile(getCosArmorFile(player), getCreativeBackup(player));
    }

    /**
     * Restaure le fichier .cosarmor depuis le backup normal ou créatif.
     * Force ensuite CosmeticArmorReworked à recharger les données
     * en invalidant son cache interne via son InventoryManager.
     */
    public static void restoreForNormal(ServerPlayer player) {
        if (!isModPresent()) return;
        File backup = getNormalBackup(player);
        if (backup.exists()) {
            copyFile(backup, getCosArmorFile(player));
        } else {
            // Première fois : pas de backup normal → vider le fichier actuel
            getCosArmorFile(player).delete();
        }
        reloadInventory(player);
    }

    public static void restoreForCreative(ServerPlayer player) {
        if (!isModPresent()) return;
        File backup = getCreativeBackup(player);
        if (backup.exists()) {
            copyFile(backup, getCosArmorFile(player));
        } else {
            // Première visite dans la dim créative → pas de cosmétiques
            getCosArmorFile(player).delete();
        }
        reloadInventory(player);
    }

    /**
     * Force CosmeticArmorReworked à recharger les données depuis le fichier
     * en invalidant son cache interne.
     */
private static void reloadInventory(ServerPlayer player) {
    try {
        lain.mods.cos.api.CosArmorAPI.getManager()
                .getCosArmorInventory(player.getUUID());
    } catch (Exception e) {
        CreativeDimMod.LOGGER.warn("[CreativeDim] Erreur reload CosmeticArmor: {}", e.getMessage());
    }
}

    // ── Chemins des fichiers ──────────────────────────────────────────────────

    private static File getCosArmorFile(ServerPlayer player) {
        return getPlayerDataFile(player, ".cosarmor");
    }

    private static File getNormalBackup(ServerPlayer player) {
        return getPlayerDataFile(player, ".cosarmor_normal");
    }

    private static File getCreativeBackup(ServerPlayer player) {
        return getPlayerDataFile(player, ".cosarmor_creative");
    }

    private static File getPlayerDataFile(ServerPlayer player, String extension) {
        UUID uuid = player.getUUID();
        return player.getServer()
                .getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR)
                .resolve(uuid + extension)
                .toFile();
    }

    private static void copyFile(File src, File dst) {
        try {
            if (src.exists()) {
                Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            CreativeDimMod.LOGGER.warn("[CreativeDim] Erreur copie fichier cosarmor {} → {}: {}",
                src.getName(), dst.getName(), e.getMessage());
        }
    }
}
