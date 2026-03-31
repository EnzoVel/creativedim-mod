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
        data.normalInventorySaved   = normalSaved;
        data.returnPosition         = returnPos         != null ? returnPos         : new CompoundTag();
        data.creativeReturnPosition = creativeReturnPos != null ? creativeReturnPos : new CompoundTag();
        data.normalCosmeticArmour   = normalCosmetic    != null ? normalCosmetic    : new CompoundTag();
        data.creativeCosmeticArmour = creativeCosmetic  != null ? creativeCosmetic  : new CompoundTag();
        return data;
    }

    public static final Supplier<AttachmentType<PlayerInventoryData>> PLAYER_INVENTORY_DATA =
            ATTACHMENT_TYPES.register("inventory_data", () ->
                    AttachmentType.builder(PlayerInventoryData::new)
                            .serialize(CODEC)
                            .copyOnDeath()
                            .build()
            );

    // ── State ─────────────────────────────────────────────────────────────────

    private final SavedInventory normalInventory      = new SavedInventory();
    private final SavedInventory creativeDimInventory = new SavedInventory();
    private boolean normalInventorySaved              = false;
    private CompoundTag returnPosition                = new CompoundTag();
    private CompoundTag creativeReturnPosition        = new CompoundTag();
    private CompoundTag normalCosmeticArmour          = new CompoundTag();
    private CompoundTag creativeCosmeticArmour        = new CompoundTag();

    public PlayerInventoryData() {}

    // ── Accessors inventaires ─────────────────────────────────────────────────

    public SavedInventory getNormalInventory()      { return normalInventory; }
    public SavedInventory getCreativeDimInventory() { return creativeDimInventory; }
    public boolean isNormalInventorySaved()         { return normalInventorySaved; }
    public void markNormalInventorySaved()          { this.normalInventorySaved = true; }

    // ── Position de retour (overworld → creative) ─────────────────────────────

    public boolean hasReturnPosition()     { return !returnPosition.isEmpty(); }
    public CompoundTag getReturnPosition() { return returnPosition; }
    public void clearReturnPosition()      { this.returnPosition = new CompoundTag(); }

    public void saveReturnPosition(ServerPlayer player) {
        returnPosition = new CompoundTag();
        returnPosition.putString("Dimension", player.level().dimension().location().toString());
        returnPosition.putDouble("X", player.getX());
        returnPosition.putDouble("Y", player.getY());
        returnPosition.putDouble("Z", player.getZ());
        returnPosition.putFloat("Yaw",   player.getYRot());
        returnPosition.putFloat("Pitch", player.getXRot());
    }

    // ── Position dans la dim créative ─────────────────────────────────────────

    public CompoundTag getCreativeReturnPosition() { return creativeReturnPosition; }

    public void saveCreativeReturnPosition(ServerPlayer player) {
        creativeReturnPosition = new CompoundTag();
        creativeReturnPosition.putDouble("X", player.getX());
        creativeReturnPosition.putDouble("Y", player.getY());
        creativeReturnPosition.putDouble("Z", player.getZ());
        creativeReturnPosition.putFloat("Yaw",   player.getYRot());
        creativeReturnPosition.putFloat("Pitch", player.getXRot());
    }

    // ── Cosmétiques ───────────────────────────────────────────────────────────

    public CompoundTag getNormalCosmeticArmour()           { return normalCosmeticArmour; }
    public void setNormalCosmeticArmour(CompoundTag tag)   { this.normalCosmeticArmour = tag.copy(); }
    public CompoundTag getCreativeCosmeticArmour()         { return creativeCosmeticArmour; }
    public void setCreativeCosmeticArmour(CompoundTag tag) { this.creativeCosmeticArmour = tag.copy(); }

    // ── Registration ──────────────────────────────────────────────────────────

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
