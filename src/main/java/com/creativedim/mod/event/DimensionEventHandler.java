package com.creativedim.mod.event;

import com.creativedim.mod.CreativeDimMod;
import com.creativedim.mod.dimension.ModDimensions;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public class DimensionEventHandler {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("creativedim")
                .requires(source -> source.hasPermission(0)) // accessible à tous les joueurs
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();

                    ResourceKey<Level> current = player.level().dimension();

                    if (ModDimensions.CREATIVE_DIM_KEY.equals(current)) {
                        teleportPlayer(player, Level.OVERWORLD);
                    } else {
                        teleportPlayer(player, ModDimensions.CREATIVE_DIM_KEY);
                    }
                    return 1;
                })
        );

        CreativeDimMod.LOGGER.info("[CreativeDim] Commande /creativedim enregistrée.");
    }

    private void teleportPlayer(ServerPlayer player, ResourceKey<Level> targetDim) {
        var server = player.getServer();
        if (server == null) return;

        ServerLevel targetLevel = server.getLevel(targetDim);
        if (targetLevel == null) {
            player.sendSystemMessage(Component.literal(
                "§c[Dimension Créative] Erreur : dimension introuvable !"
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

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && ModDimensions.isCreativeDimension(serverLevel)) {
            CreativeDimMod.LOGGER.info("[CreativeDim] Creative Dimension loaded successfully!");
        }
    }
}
