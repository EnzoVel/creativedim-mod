package com.creativedim.mod.event;

import com.creativedim.mod.CreativeDimMod;
import com.creativedim.mod.dimension.ModDimensions;
import com.creativedim.mod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Handles right-clicking with the Portal Activator to travel between dimensions,
 * and logs dimension load events.
 */
public class DimensionEventHandler {

    /**
     * Right-click with Portal Activator in either hand → teleport to/from creative dim.
     */
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        // Accept either main hand or off-hand
        var held = event.getHand() == InteractionHand.MAIN_HAND
                ? player.getMainHandItem()
                : player.getOffhandItem();

        if (!held.is(ModItems.PORTAL_ACTIVATOR.get())) return;

        event.setCanceled(true);

        ResourceKey<Level> current = player.level().dimension();

        if (ModDimensions.CREATIVE_DIM_KEY.equals(current)) {
            // Return to Overworld
            teleportPlayer(player, Level.OVERWORLD);
        } else {
            // Go to creative dimension
            teleportPlayer(player, ModDimensions.CREATIVE_DIM_KEY);
        }
    }

    /** Teleport a player to the given dimension at a safe surface position near spawn. */
    private void teleportPlayer(ServerPlayer player, ResourceKey<Level> targetDim) {
        var server = player.getServer();
        if (server == null) return;

        ServerLevel targetLevel = server.getLevel(targetDim);
        if (targetLevel == null) {
            player.sendSystemMessage(Component.literal(
                "§c[Dimension Créative] Erreur : dimension '" + targetDim.location() + "' introuvable !"
            ));
            CreativeDimMod.LOGGER.error("[CreativeDim] Dimension not found: {}", targetDim.location());
            return;
        }

        BlockPos dest = getSafeDestination(targetLevel);

        player.teleportTo(
            targetLevel,
            dest.getX() + 0.5,
            dest.getY(),
            dest.getZ() + 0.5,
            player.getYRot(),
            player.getXRot()
        );

        CreativeDimMod.LOGGER.debug("[CreativeDim] Teleported {} to {} at {}",
            player.getName().getString(), targetDim.location(), dest);
    }

    /**
     * Find a safe landing position in the target level.
     *  - Creative dim (superflat): ground = Y3 (bedrock+dirt+dirt+grass), stand at Y4.
     *  - Overworld: use MOTION_BLOCKING heightmap at world spawn.
     */
    private BlockPos getSafeDestination(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        int x = spawn.getX();
        int z = spawn.getZ();

        if (ModDimensions.isCreativeDimension(level)) {
            // Superflat: ground at Y=3, stand at Y=4
            return new BlockPos(x, 4, z);
        } else {
            // Surface in the overworld
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            return new BlockPos(x, y, z);
        }
    }

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && ModDimensions.isCreativeDimension(serverLevel)) {
            CreativeDimMod.LOGGER.info("[CreativeDim] Creative Dimension loaded successfully!");
        }
    }
}

