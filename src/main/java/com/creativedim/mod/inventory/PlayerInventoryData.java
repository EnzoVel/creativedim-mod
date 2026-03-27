package com.creativedim.mod.inventory;

import com.creativedim.mod.CreativeDimMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
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
                            .forGetter(d -> d.normalInventorySaved)
            ).apply(instance, PlayerInventoryData::fromCodec)
    );

    private static PlayerInventoryData fromCodec(CompoundTag normalTag, CompoundTag creativeTag, boolean normalSaved) {
        PlayerInventoryData data = new PlayerInventoryData();
        data.normalInventory.deserializeNBT(normalTag);
        data.creativeDimInventory.deserializeNBT(creativeTag);
        data.normalInventorySaved = normalSaved;
        return data;
    }

    public static final Supplier<AttachmentType<PlayerInventoryData>> PLAYER_INVENTORY_DATA =
            ATTACHMENT_TYPES.register("inventory_data", () ->
                    AttachmentType.builder(PlayerInventoryData::new)
                            .serialize(CODEC)
                            .copyOnDeath()
                            .build()
            );

    private final SavedInventory normalInventory      = new SavedInventory();
    private final SavedInventory creativeDimInventory = new SavedInventory();
    private boolean normalInventorySaved = false;

    public PlayerInventoryData() {}

    public SavedInventory getNormalInventory()      { return normalInventory; }
    public SavedInventory getCreativeDimInventory() { return creativeDimInventory; }
    public boolean isNormalInventorySaved()         { return normalInventorySaved; }
    public void markNormalInventorySaved()          { this.normalInventorySaved = true; }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
