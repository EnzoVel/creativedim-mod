package com.creativedim.mod.compat;

import com.creativedim.mod.CreativeDimMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
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
 * On sauvegarde/restaure ces fichiers lors des transitions de dimension.
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

    /** Sauvegarde le .cosarmor actuel comme backup "normal" (avant d'entrer en dim créative). */
    public static void saveForNormal(ServerPlayer player) {
        if (!isModPresent()) return;
        copyFile(getCosArmorFile(player), getNormalBackup(player));
        CreativeDimMod.LOGGER.debug("[CreativeDim] CosArmor sauvegardé (normal) pour {}", player.getName().getString());
    }

    /** Sauvegarde le .cosarmor actuel comme backup "créatif" (avant de quitter la dim créative). */
    public static void saveForCreative(ServerPlayer player) {
        if (!isModPresent()) return;
        copyFile(getCosArmorFile(player), getCreativeBackup(player));
        CreativeDimMod.LOGGER.debug("[CreativeDim] CosArmor sauvegardé (créatif) pour {}", player.getName().getString());
    }

    /** Restaure le backup "normal" dans le .cosarmor actif (en quittant la dim créative). */
    public static void restoreForNormal(ServerPlayer player) {
        if (!isModPresent()) return;
        File backup = getNormalBackup(player);
        if (backup.exists()) {
            copyFile(backup, getCosArmorFile(player));
        } else {
            getCosArmorFile(player).delete();
        }
        reloadInventory(player);
        CreativeDimMod.LOGGER.debug("[CreativeDim] CosArmor restauré (normal) pour {}", player.getName().getString());
    }

    /** Restaure le backup "créatif" dans le .cosarmor actif (en entrant dans la dim créative). */
    public static void restoreForCreative(ServerPlayer player) {
        if (!isModPresent()) return;
        File backup = getCreativeBackup(player);
        if (backup.exists()) {
            copyFile(backup, getCosArmorFile(player));
        } else {
            getCosArmorFile(player).delete();
        }
        reloadInventory(player);
        CreativeDimMod.LOGGER.debug("[CreativeDim] CosArmor restauré (créatif) pour {}", player.getName().getString());
    }

    /**
     * Force CosmeticArmorReworked à recharger les données depuis le fichier
     * en invalidant son cache interne.
     */
    private static void reloadInventory(ServerPlayer player) {
        try {
            lain.mods.cos.impl.ModObjects.manager.CommonCache.invalidate(player.getUUID());
            lain.mods.cos.impl.ModObjects.manager.getCosArmorInventory(player.getUUID());
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
        return player.getServer()
                .getWorldPath(LevelResource.PLAYER_DATA_DIR)
                .resolve(player.getUUID() + extension)
                .toFile();
    }

    private static void copyFile(File src, File dst) {
        try {
            if (src.exists()) {
                Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            CreativeDimMod.LOGGER.warn("[CreativeDim] Erreur copie cosarmor {} → {}: {}",
                src.getName(), dst.getName(), e.getMessage());
        }
    }
}
