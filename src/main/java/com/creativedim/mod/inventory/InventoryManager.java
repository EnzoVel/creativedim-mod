package com.creativedim.mod.inventory;

import com.creativedim.mod.CreativeDimMod;
import com.creativedim.mod.dimension.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Core event handler for inventory swapping and game mode switching
 * whenever a player enters or leaves the Creative Dimension.
 *
 * Flow:
 *   Entering creative dim:
 *     1. EntityTravelToDimensionEvent  → save normal inventory
 *     2. PlayerChangedDimensionEvent   → load creative inventory, set CREATIVE mode
 *
 *   Leaving creative dim:
 *     1. EntityTravelToDimensionEvent  → save creative inventory
 *     2. PlayerChangedDimensionEvent   → load normal inventory, set SURVIVAL mode
 */
public class InventoryManager {

    // ── Pre-teleport: save current inventory ─────────────────────────────────

    @SubscribeEvent
    public void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ResourceKey<Level> from = player.level().dimension();
        ResourceKey<Level> to   = event.getDimension();

        boolean leavingCreative  = ModDimensions.CREATIVE_DIM_KEY.equals(from);
        boolean enteringCreative = ModDimensions.CREATIVE_DIM_KEY.equals(to);

        if (!leavingCreative && !enteringCreative) return;

        PlayerInventoryData data = player.getData(PlayerInventoryData.PLAYER_INVENTORY_DATA);

        if (enteringCreative) {
            // Save the normal inventory before we lose it
            data.getNormalInventory().captureFromPlayer(player);
            data.markNormalInventorySaved();
            CreativeDimMod.LOGGER.debug("[CreativeDim] Saved normal inventory for {}", player.getName().getString());
        } else {
            // Save the creative-dim inventory before leaving
            data.getCreativeDimInventory().captureFromPlayer(player);
            CreativeDimMod.LOGGER.debug("[CreativeDim] Saved creative inventory for {}", player.getName().getString());
        }
    }

    // ── Post-teleport: restore target inventory + set game mode ──────────────

    @SubscribeEvent
    public void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ResourceKey<Level> from = event.getFrom();
        ResourceKey<Level> to   = event.getTo();

        boolean enteredCreative = ModDimensions.CREATIVE_DIM_KEY.equals(to);
        boolean leftCreative    = ModDimensions.CREATIVE_DIM_KEY.equals(from);

        if (!enteredCreative && !leftCreative) return;

        PlayerInventoryData data = player.getData(PlayerInventoryData.PLAYER_INVENTORY_DATA);

        if (enteredCreative) {
            // Restore creative dim inventory (empty on first visit)
            data.getCreativeDimInventory().applyToPlayer(player);
            player.setGameMode(GameType.CREATIVE);

            player.sendSystemMessage(Component.literal(
                "§a[Dimension Créative] §fMode créatif activé — inventaire de survie sauvegardé."
            ));
            CreativeDimMod.LOGGER.info("[CreativeDim] {} entered creative dim → CREATIVE mode", player.getName().getString());

        } else {
            // Restore normal inventory
            data.getNormalInventory().applyToPlayer(player);
            player.setGameMode(GameType.SURVIVAL);

            player.sendSystemMessage(Component.literal(
                "§e[Dimension Créative] §fRetour en survie — inventaire restauré."
            ));
            CreativeDimMod.LOGGER.info("[CreativeDim] {} left creative dim → SURVIVAL mode", player.getName().getString());
        }
    }

    // ── Login: enforce correct game mode based on current dimension ───────────

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (ModDimensions.isCreativeDimension(player.serverLevel())) {
            if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
                player.setGameMode(GameType.CREATIVE);
                CreativeDimMod.LOGGER.info("[CreativeDim] {} logged in inside creative dim — forced CREATIVE", player.getName().getString());
            }
        }
    }

    // ── Respawn: ensure correct game mode after death ─────────────────────────

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // After respawn, check dimension and restore appropriate game mode
        if (ModDimensions.isCreativeDimension(player.serverLevel())) {
            player.setGameMode(GameType.CREATIVE);
        } else {
            // If they respawned outside creative dim but are in creative, fix that
            if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                player.setGameMode(GameType.SURVIVAL);
            }
        }
    }

    // ── Clone (respawn data copy): preserve our attachment data ───────────────

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;

        // NeoForge copyOnDeath() on the AttachmentType handles this automatically,
        // but we log it for debugging purposes.
        CreativeDimMod.LOGGER.debug("[CreativeDim] Player clone event for {}", newPlayer.getName().getString());
    }
}

