package com.creativedim.mod.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/**
 * Represents a saved snapshot of a player's inventory (items, armor, offhand),
 * health, hunger, and XP.
 */
public class SavedInventory {

    private CompoundTag inventoryData = new CompoundTag();
    private float health = 20.0f;
    private int foodLevel = 20;
    private float saturationLevel = 5.0f;
    private int totalExperience = 0;
    private float experienceProgress = 0f;
    private int experienceLevel = 0;

    public SavedInventory() {}

    /**
     * Capture the player's current state into this SavedInventory.
     */
    public void captureFromPlayer(Player player) {
        inventoryData = new CompoundTag();
        // save() returns a ListTag of all slots
        inventoryData.put("Inventory", player.getInventory().save(new ListTag()));

        this.health = player.getHealth();
        this.foodLevel = player.getFoodData().getFoodLevel();
        this.saturationLevel = player.getFoodData().getSaturationLevel();
        this.totalExperience = player.totalExperience;
        this.experienceProgress = player.experienceProgress;
        this.experienceLevel = player.experienceLevel;
    }

    /**
     * Apply this SavedInventory to the player, restoring their state.
     * The caller is responsible for clearing the inventory beforehand if needed.
     */
    public void applyToPlayer(Player player) {
        player.getInventory().clearContent();

        if (inventoryData.contains("Inventory", Tag.TAG_LIST)) {
            player.getInventory().load(inventoryData.getList("Inventory", Tag.TAG_COMPOUND));
        }

        player.setHealth(Math.max(1.0f, this.health));
        player.getFoodData().setFoodLevel(this.foodLevel);
        player.getFoodData().setSaturation(this.saturationLevel);
        player.totalExperience = this.totalExperience;
        player.experienceProgress = this.experienceProgress;
        player.experienceLevel = this.experienceLevel;
    }

    /** Serialize to NBT for persistent storage via AttachmentType. */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("InventoryData", inventoryData.copy());
        tag.putFloat("Health", health);
        tag.putInt("FoodLevel", foodLevel);
        tag.putFloat("Saturation", saturationLevel);
        tag.putInt("TotalXP", totalExperience);
        tag.putFloat("XPProgress", experienceProgress);
        tag.putInt("XPLevel", experienceLevel);
        return tag;
    }

    /** Deserialize from NBT. */
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("InventoryData", Tag.TAG_COMPOUND)) {
            inventoryData = tag.getCompound("InventoryData");
        }
        health          = tag.contains("Health")     ? tag.getFloat("Health")     : 20.0f;
        foodLevel       = tag.contains("FoodLevel")  ? tag.getInt("FoodLevel")    : 20;
        saturationLevel = tag.contains("Saturation") ? tag.getFloat("Saturation") : 5.0f;
        totalExperience = tag.contains("TotalXP")    ? tag.getInt("TotalXP")      : 0;
        experienceProgress = tag.contains("XPProgress") ? tag.getFloat("XPProgress") : 0f;
        experienceLevel = tag.contains("XPLevel")    ? tag.getInt("XPLevel")      : 0;
    }
}
