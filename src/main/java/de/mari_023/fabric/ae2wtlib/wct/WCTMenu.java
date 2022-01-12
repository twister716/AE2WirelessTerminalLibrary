package de.mari_023.fabric.ae2wtlib.wct;

import java.util.Objects;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.DisabledSlot;

import de.mari_023.fabric.ae2wtlib.AE2wtlibSlotSemantics;
import de.mari_023.fabric.ae2wtlib.wct.magnet_card.MagnetHandler;
import de.mari_023.fabric.ae2wtlib.wct.magnet_card.MagnetMode;
import de.mari_023.fabric.ae2wtlib.wct.magnet_card.MagnetSettings;
import de.mari_023.fabric.ae2wtlib.wut.ItemWUT;

public class WCTMenu extends CraftingTermMenu {

    public static final MenuType<WCTMenu> TYPE = MenuTypeBuilder.create(WCTMenu::new, WCTMenuHost.class)
            .requirePermission(SecurityPermissions.CRAFT).build("wireless_crafting_terminal");

    public static final String ACTION_DELETE = "delete";
    public static final String MAGNET_MODE = "magnetMode";

    private final WCTMenuHost wctGUIObject;

    public WCTMenu(int id, final Inventory ip, final WCTMenuHost gui) {
        super(TYPE, id, ip, gui, true);
        wctGUIObject = gui;

        boolean isInOffhand = Integer.valueOf(40).equals(wctGUIObject.getSlot());

        SlotsWithTrinket[5] = addSlot(new Slot(getPlayerInventory(), 39, 0, 0) {
            @Environment(EnvType.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
            }

            public boolean mayPlace(ItemStack stack) {
                return getPlayerInventory().canPlaceItem(39, stack) && ((stack.getItem() instanceof ArmorItem aItem
                        && aItem.getSlot().equals(EquipmentSlot.HEAD))
                        || (stack.getItem() instanceof BlockItem bItem && bItem.getBlock() instanceof Wearable));
            }
        }, AE2wtlibSlotSemantics.HELMET);
        SlotsWithTrinket[6] = addSlot(new Slot(getPlayerInventory(), 38, 0, 0) {
            @Environment(EnvType.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
            }

            public boolean mayPlace(ItemStack stack) {
                return getPlayerInventory().canPlaceItem(38, stack) && stack.getItem() instanceof ArmorItem aItem
                        && aItem.getSlot().equals(EquipmentSlot.CHEST);
            }
        }, AE2wtlibSlotSemantics.CHESTPLATE);
        SlotsWithTrinket[7] = addSlot(new Slot(getPlayerInventory(), 37, 0, 0) {
            @Environment(EnvType.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
            }

            public boolean mayPlace(ItemStack stack) {
                return getPlayerInventory().canPlaceItem(37, stack) && stack.getItem() instanceof ArmorItem aItem
                        && aItem.getSlot().equals(EquipmentSlot.LEGS);
            }
        }, AE2wtlibSlotSemantics.LEGGINGS);
        SlotsWithTrinket[8] = addSlot(new Slot(getPlayerInventory(), 36, 0, 0) {
            @Environment(EnvType.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
            }

            public boolean mayPlace(ItemStack stack) {
                return getPlayerInventory().canPlaceItem(36, stack) && stack.getItem() instanceof ArmorItem aItem
                        && aItem.getSlot().equals(EquipmentSlot.FEET);
            }
        }, AE2wtlibSlotSemantics.BOOTS);

        if (isInOffhand)
            SlotsWithTrinket[45] = addSlot(new DisabledSlot(getPlayerInventory(), Inventory.SLOT_OFFHAND) {
                @Environment(EnvType.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
                }
            }, AE2wtlibSlotSemantics.OFFHAND);
        else
            SlotsWithTrinket[45] = addSlot(new Slot(getPlayerInventory(), Inventory.SLOT_OFFHAND, 0, 0) {
                @Environment(EnvType.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
                }
            }, AE2wtlibSlotSemantics.OFFHAND);
        addSlot(new AppEngSlot(wctGUIObject.getSubInventory(WCTMenuHost.INV_TRASH), 0), AE2wtlibSlotSemantics.TRASH);

        registerClientAction(ACTION_DELETE, this::deleteTrashSlot);
        registerClientAction(MAGNET_MODE, MagnetMode.class, this::setMagnetMode);
    }

    @Override
    public IGridNode getNetworkNode() {
        return wctGUIObject.getActionableNode();
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    public void deleteTrashSlot() {
        if (isClient())
            sendClientAction(ACTION_DELETE);
        Objects.requireNonNull(wctGUIObject.getSubInventory(WCTMenuHost.INV_TRASH)).setItemDirect(0, ItemStack.EMPTY);
    }

    private MagnetSettings magnetSettings;

    public MagnetSettings getMagnetSettings() {
        return magnetSettings = MagnetHandler.getMagnetSettings(wctGUIObject.getItemStack());
    }

    public void saveMagnetSettings() {
        MagnetHandler.saveMagnetSettings(wctGUIObject.getItemStack(), magnetSettings);
    }

    public void setMagnetMode(MagnetMode mode) {
        if (isClient())
            sendClientAction(MAGNET_MODE, mode);
        getMagnetSettings().magnetMode = mode;
        saveMagnetSettings();
    }

    public boolean isWUT() {
        return wctGUIObject.getItemStack().getItem() instanceof ItemWUT;
    }

    public final Slot[] SlotsWithTrinket = new Slot[46];
}
