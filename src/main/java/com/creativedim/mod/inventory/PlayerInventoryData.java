package com.creativedim.mod.inventory;

import com.creativedim.mod.CreativeDimMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class PlayerInventoryData {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CreativeDimMod.MOD_ID);

    public static final Codec<PlayerInventoryData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CompoundTag.CODEC.optionalFieldOf("NormalInventory", new CompoundTag())
                            .forGetter(d -> d.normalInventory.serializeNBT()),
                    CompoundTag.CODEC.optionalFieldOf("CreativeDimInventory", new CompoundTag())
                            .forGetter(d -> d.creativeDimInventory.serializeNBT()),
                    Codec.BOOL.optionalFieldOf("NormalSaved", false)
                            .forGetter(d -> d.normalInventorySaved),
                    CompoundTag.CODEC.optionalFieldOf("ReturnPosition", new CompoundTag())
                            .forGetter(d -> d.returnPosition),
                    CompoundTag.CODEC.optionalFieldOf("CreativeReturnPosition", new CompoundTag())
                            .forGetter(d -> d.creativeReturnPosition),
                    CompoundTag.CODEC.optionalFieldOf("NormalCosmeticArmour", new CompoundTag())
                            .forGetter(d -> d.normalCosmeticArmour),
                    CompoundTag.CODEC.optionalFieldOf("CreativeCosmeticArmour", new CompoundTag())
                            .forGetter(d -> d.creativeCosmeticArmour)
            ).apply(instance, PlayerInventoryData::fromCodec)
    );

    private static PlayerInventoryData fromCodec(
            CompoundTag normalTag, CompoundTag creativeTag, boolean normalSaved,
            CompoundTag returnPos, CompoundTag creativeReturnPos,
            CompoundTag normalCosmetic, CompoundTag creativeCosmetic) {
        PlayerInventoryData data = new PlayerInventoryData();
        try {
            if (normalTag != null && !normalTag.isEmpty())
                data.normalInventory.deserializeNBT(normalTag);
        } catch (Exception e) {
            CreativeDimMod.LOGGER.warn("[CreativeDim] Erreur lecture inventaire normal: {}", e.getMessage());
        }
        try {
            if (creativeTag != null && !creativeTag.isEmpty())
                data.creativeDimInventory.deserializeNBT(creativeTag);
        } catch (Exception e) {
            CreativeDimMod.LOGGER.warn("[CreativeDim] Erreur lecture inventaire créatif: {}", e.getMessage());
        }
        data.normalInventorySaved   = nor
