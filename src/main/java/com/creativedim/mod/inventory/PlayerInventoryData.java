package com.creativedim.mod.inventory;

import com.creativedim.mod.CreativeDimMod;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Stores two saved inventories per player:
 *  - "normal" inventory  → Overworld / Nether / End
 *  - "creative_dim"      → our custom creative dimension
 *
 * Uses NeoForge AttachmentType for automatic per-player persistent storage.
 */
public class PlayerInventoryData {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CreativeDimMod.MOD_ID);

    @SuppressWarnings("unchecked")
    public static final Supplier<AttachmentType<PlayerInventoryData>> PLAYER_INVENTORY_DATA =
            ATTACHMENT_TYPES.register("inventory_data", () ->
                    AttachmentType.<PlayerInventoryData>builder(PlayerInventoryData::new)
                            .serialize(
                                    data -> data.serializeNBT(),
                                    (tag) -> {
                                        PlayerInventoryData d = new PlayerInventoryData();
                                        d.deserializeNBT(tag);
                                        return d;
                                    }
                            )
                            .copyOnDeath()   // keep data through death (game mode stays managed)
                            .build()
            );

    // ── Stored state ──────────────────────────────────────────────────────────

    private final SavedInventory normalInventory     = new SavedInventory();
    private final SavedInventory creativeDimInventory = new SavedInventory();

    /** Whether the player's normal inventory has been saved at least once. */
    private boolean normalInventorySaved = false;

    public PlayerInventoryData() {}

    // ── Accessors ─────────────────────────────────────────────────────────────

    public SavedInventory getNormalInventory()      { return normalInventory; }
    public SavedInventory getCreativeDimInventory() { return creativeDimInventory; }

    public boolean isNormalInventorySaved() { return normalInventorySaved; }
    public void markNormalInventorySaved()  { this.normalInventorySaved = true; }

    // ── Serialization ─────────────────────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("NormalInventory",     normalInventory.serializeNBT());
        tag.put("CreativeDimInventory", creativeDimInventory.serializeNBT());
        tag.putBoolean("NormalSaved",  normalInventorySaved);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("NormalInventory")) {
            normalInventory.deserializeNBT(tag.getCompound("NormalInventory"));
        }
        if (tag.contains("CreativeDimInventory")) {
            creativeDimInventory.deserializeNBT(tag.getCompound("CreativeDimInventory"));
        }
        normalInventorySaved = tag.getBoolean("NormalSaved");
    }

    // ── Registration ──────────────────────────────────────────────────────────

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

