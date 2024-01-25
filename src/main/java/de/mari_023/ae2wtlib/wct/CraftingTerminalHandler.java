package de.mari_023.ae2wtlib.wct;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.menu.locator.MenuLocator;

import de.mari_023.ae2wtlib.curio.CurioHelper;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetHost;
import de.mari_023.ae2wtlib.wut.WUTHandler;

public class CraftingTerminalHandler {
    private static final WeakHashMap<Player, CraftingTerminalHandler> SERVER_PLAYERS = new WeakHashMap<>();
    private static final WeakHashMap<Player, CraftingTerminalHandler> CLIENT_PLAYERS = new WeakHashMap<>();
    private final Player player;
    private ItemStack craftingTerminal = ItemStack.EMPTY;
    @Nullable
    private WTMenuHost menuHost;
    @Nullable
    private MenuLocator locator;
    private HashMap<Item, Long> restockAbleItems = new HashMap<>();
    @Nullable
    private MagnetHost magnetHost;

    private CraftingTerminalHandler(Player player) {
        this.player = player;
    }

    public static CraftingTerminalHandler getCraftingTerminalHandler(Player player) {
        Map<Player, CraftingTerminalHandler> players = player instanceof ServerPlayer ? SERVER_PLAYERS : CLIENT_PLAYERS;

        if (players.containsKey(player)) {
            if (player == players.get(player).player)
                return players.get(player);
            removePlayer(player);
        }
        CraftingTerminalHandler handler = new CraftingTerminalHandler(player);
        players.put(player, handler);
        return handler;
    }

    public static void removePlayer(Player player) {
        if (player instanceof ServerPlayer)
            SERVER_PLAYERS.remove(player);
        else
            CLIENT_PLAYERS.remove(player);
    }

    public void invalidateCache() {
        craftingTerminal = ItemStack.EMPTY;
        menuHost = null;
        locator = null;
        restockAbleItems.clear();
        magnetHost = null;
    }

    public ItemStack getCraftingTerminal() {// TODO use Inventory#findSlotMatchingItem(), which ensures the stack is
        // actually identical (unlike #contains() which only cares about the item)
        Inventory inv = player.getInventory();
        if (!craftingTerminal.isEmpty() && (inv.contains(craftingTerminal)
                || CurioHelper.isStillPresent(player, craftingTerminal)))
            return craftingTerminal;

        if (getMenuHost() == null)
            craftingTerminal = ItemStack.EMPTY;
        else {
            assert menuHost != null;
            craftingTerminal = menuHost.getItemStack();
        }

        return craftingTerminal;
    }

    @Nullable
    private WTMenuHost getMenuHost() {
        if (menuHost != null && menuHost.rangeCheck() && menuHost.stillValid()) {
            return menuHost;
        }

        locator = WUTHandler.findTerminal(player, "crafting");

        if (locator == null)
            menuHost = null;
        else
            menuHost = locator.locate(player, WTMenuHost.class);

        if (menuHost == null) {
            invalidateCache();
        }

        return menuHost;
    }

    @Nullable
    public MenuLocator getLocator() {
        if (getMenuHost() == null)
            return null;
        return locator;
    }

    @Nullable
    public IGrid getTargetGrid() {
        if(menuHost == null) return null;
        final IGridNode n = menuHost.getActionableNode();

        if (n == null)
            return null;
        return menuHost.getActionableNode().getGrid();
    }

    public boolean inRange() {
        if (getMenuHost() == null)
            return false;
        return getMenuHost().rangeCheck();
    }

    public long getAccessibleAmount(ItemStack stack) {
        return stack.getCount()
                + (restockAbleItems.get(stack.getItem()) == null ? 0 : restockAbleItems.get(stack.getItem()));
    }

    public boolean isRestockable(ItemStack stack) {
        return restockAbleItems.containsKey(stack.getItem());
    }

    public void setRestockAbleItems(HashMap<Item, Long> items) {
        restockAbleItems = items;
    }

    @Nullable
    public MagnetHost getMagnetHost() {
        if (magnetHost == null) {
            if (getCraftingTerminal().isEmpty())
                return null;
            magnetHost = new MagnetHost(this);
        }
        return magnetHost;
    }
}
