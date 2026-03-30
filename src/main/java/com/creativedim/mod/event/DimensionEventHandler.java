package com.creativedim.mod.event;

import com.creativedim.mod.CreativeDimMod;
import com.creativedim.mod.dimension.ModDimensions;
import com.creativedim.mod.inventory.PlayerInventoryData;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class DimensionEventHandler {

    // ══════════════════════════════════════════════════════════════════════════
    // COMMANDES
    // ══════════════════════════════════════════════════════════════════════════

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("creativedim")
                .requires(source -> source.hasPermission(0))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ResourceKey<Level> current = player.level().dimension();

                    if (ModDimensions.CREATIVE_DIM_KEY.equals(current)) {
                        returnToOrigin(player);
                    } else {
                        PlayerInventoryData data = player.getData(PlayerInventoryData.PLAYER_INVENTORY_DATA);
                        data.saveReturnPosition(player);
                        teleportToCreative(player);
                    }
                    return 1;
                })
                .then(Commands.literal("back")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();

                        if (!ModDimensions.isCreativeDimension(player.serverLevel())) {
                            player.sendSystemMessage(Component.literal(
                                "§c[Dimension Créative] Vous n'êtes pas dans la dimension créative."
                            ));
                            return 0;
                        }

                        returnToOrigin(player);
                        return 1;
                    })
                )
        );

        CreativeDimMod.LOGGER.info("[CreativeDim] Commandes /creativedim et /creativedim back enregistrées.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TÉLÉPORTATION
    // ══════════════════════════════════════════════════════════════════════════

    private void teleportToCreative(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return;

        ServerLevel target = server.getLevel(ModDimensions.CREATIVE_DIM_KEY);
        if (target == null) {
            player.sendSystemMessage(Component.literal(
                "§c[Dimension Créative] Erreur : dimension introuvable !"
            ));
            return;
        }

        PlayerInventoryData data = player.getData(PlayerInventoryData.PLAYER_INVENTORY_DATA);
        CompoundTag lastPos = data.getCreativeReturnPosition();

        if (!lastPos.isEmpty()) {
            double x     = lastPos.getDouble("X");
            double y     = lastPos.getDouble("Y");
            double z     = lastPos.getDouble("Z");
            float  yaw   = lastPos.getFloat("Yaw");
            float  pitch = lastPos.getFloat("Pitch");
            player.teleportTo(target, x, y, z, yaw, pitch);
        } else {
            BlockPos dest = getSafeDestination(target);
            player.teleportTo(target,
                dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5,
                player.getYRot(), player.getXRot()
            );
        }
    }

    private void returnToOrigin(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return;

        PlayerInventoryData data = player.getData(PlayerInventoryData.PLAYER_INVENTORY_DATA);

        data.saveCreativeReturnPosition(player);

        if (!data.hasReturnPosition()) {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld == null) return;
            BlockPos spawn = getSafeDestination(overworld);
            player.teleportTo(overworld,
                spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                player.getYRot(), player.getXRot()
            );
            player.sendSystemMessage(Component.literal(
                "§e[Dimension Créative] §fAucune position sauvegardée — retour au spawn."
            ));
            return;
        }

        CompoundTag pos = data.getReturnPosition();
        String dimStr = pos.getString("Dimension");
        double x      = pos.getDouble("X");
        double y      = pos.getDouble("Y");
        double z      = pos.getDouble("Z");
        float  yaw    = pos.getFloat("Yaw");
        float  pitch  = pos.getFloat("Pitch");

        ResourceKey<Level> returnDim = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.parse(dimStr)
        );
        ServerLevel returnLevel = server.getLevel(returnDim);

        if (returnLevel == null) {
            returnLevel = server.getLevel(Level.OVERWORLD);
            if (returnLevel == null) return;
            player.sendSystemMessage(Component.literal(
                "§e[Dimension Créative] §fDimension d'origine introuvable, retour à l'Overworld."
            ));
        }

        player.teleportTo(returnLevel, x, y, z, yaw, pitch);
        data.clearReturnPosition();
    }

    private BlockPos getSafeDestination(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        int x = spawn.getX();
        int z = spawn.getZ();

        if (ModDimensions.isCreativeDimension(level)) {
            return new BlockPos(x, 4, z);
        } else {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            return new BlockPos(x, y, z);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MÉTÉO ET TEMPS
    // ══════════════════════════════════════════════════════════════════════════

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!ModDimensions.isCreativeDimension(serverLevel)) return;

        if (serverLevel.isRaining() || serverLevel.isThundering()) {
            serverLevel.setWeatherParameters(
                Integer.MAX_VALUE,
                0,
                false,
                false
            );
        }

        if (serverLevel.getDayTime() != 6000) {
            serverLevel.setDayTime(6000);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MOBS HOSTILES
    // ══════════════════════════════════════════════════════════════════════════

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!ModDimensions.isCreativeDimension(serverLevel)) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        if (mob instanceof Monster) {
            event.setCanceled(true);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ENDERCHESTS BLOQUÉES
    // ══════════════════════════════════════════════════════════════════════════

    @SubscribeEvent
    public void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ModDimensions.isCreativeDimension(player.serverLevel())) return;

        var blockState = event.getLevel().getBlockState(event.getPos());
        if (!blockState.is(Blocks.ENDER_CHEST)) return;

        event.setCanceled(true);
        event.setUseBlock(net.neoforged.neoforge.common.util.TriState.FALSE);

        player.sendSystemMessage(Component.literal(
            "§c[Dimension Créative] §fLes Ender Chests sont désactivés dans cette dimension."
        ));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ÉVÉNEMENTS NIVEAU
    // ══════════════════════════════════════════════════════════════════════════

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && ModDimensions.isCreativeDimension(serverLevel)) {
            CreativeDimMod.LOGGER.info("[CreativeDim] Creative Dimension loaded successfully!");
        }
    }
}
