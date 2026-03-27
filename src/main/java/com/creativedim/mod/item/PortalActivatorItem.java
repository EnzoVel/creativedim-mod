package com.creativedim.mod.item;

import com.creativedim.mod.dimension.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * The Portal Activator item.
 * Right-clicking with this item triggers teleportation (handled in DimensionEventHandler).
 * This class mainly provides tooltip information.
 */
public class PortalActivatorItem extends Item {

    public PortalActivatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§7Clic droit §f: Voyager vers la Dimension Créative"));
        tooltipComponents.add(Component.literal("§7(ou revenir si vous y êtes déjà)"));
        tooltipComponents.add(Component.literal("§bVos inventaires sont séparés !"));
    }
}
