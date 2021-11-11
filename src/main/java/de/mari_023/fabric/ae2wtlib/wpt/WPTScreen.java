package de.mari_023.fabric.ae2wtlib.wpt;

import appeng.client.gui.me.items.PatternTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import de.mari_023.fabric.ae2wtlib.wut.CycleTerminalButton;
import de.mari_023.fabric.ae2wtlib.wut.IUniversalTerminalCapable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.io.IOException;

public class WPTScreen extends PatternTermScreen<WPTContainer> implements IUniversalTerminalCapable {

    private static final ScreenStyle STYLE;

    static {
        ScreenStyle STYLE1;
        try {
            STYLE1 = StyleManager.loadStyleDoc("/screens/wtlib/wireless_pattern_terminal.json");
        } catch(IOException ignored) {
            STYLE1 = null;
        }
        STYLE = STYLE1;
    }

    public WPTScreen(WPTContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title, STYLE);

        if(getScreenHandler().isWUT()) widgets.add("cycleTerminal", new CycleTerminalButton(btn -> cycleTerminal()));
    }

    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {//FIXME slot offsets, gui spacing
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        //FIXME infinity booster card background
    }
}