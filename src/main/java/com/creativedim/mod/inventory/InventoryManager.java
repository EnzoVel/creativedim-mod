package com.creativedim.mod.inventory;

import com.creativedim.mod.CreativeDimMod;
import com.creativedim.mod.compat.CosmeticArmourHelper;
import com.creativedim.mod.dimension.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class InventoryManager {

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
            data.getNormalInventory().captureFromPlayer(player);
            data.markNormalInventorySaved();
            data.setNormalCosmeticArmour(CosmeticArmourHelper.capture(player));
            CreativeDimMod.LOGGER.debug("[CreativeDim] Saved normal inventory + cosmetics for {}", player.getName().getString());
        } else {
            data.getCreativeDimInventory().captureFromPlayer(player);
            data.setCreativeCosmeticArmour(CosmeticArmourHelper.capture(player));
            CreativeDimMod.LOGGER.debug("[CreativeDim] Saved creative inventory + cosmetics for {}", player.getName().getString());
        }
    }

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
            data.getCreativeDimInventory().applyToPlayer(player);
            CosmeticArmourHelper.restore(player, data.getCreativeCosmeticArmour());
            player.setGameMode(GameType.CREATIVE);

            player.sendSystemMessage(Component.literal(
                "§a[Dimension Créative] §fMode créatif activé — inventaire de survie sauvegardé."
            ));
            CreativeDimMod.LOGGER.info("[CreativeDim] {} entered creative dim → CREATIVE", player.getName().getString());

        } else {
            data.getNormalInventory().applyToPlayer(player);
            CosmeticArmourHelper.restore(player, data.getNormalCosmeticArmour());
            player.setGameMode(GameType.SURVIVAL);

            player.sendSystemMessage(Component.literal(
                "§e[Dimension Créative] §fRetour en survie — inventaire restauré."
            ));
            CreativeDimMod.LOGGER.info("[CreativeDim] {} left creative dim → SURVIVAL", player.getName().getString());
        }
    }

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

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (ModDimensions.isCreativeDimension(player.serverLevel())) {
            player.setGameMode(GameType.CREATIVE);
        } else if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
            player.setGameMode(GameType.SURVIVAL);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        CreativeDimMod.LOGGER.debug("[CreativeDim] Player clone event for {}", event.getEntity().getName().getString());
    }
}
