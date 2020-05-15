package test.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ForcedGUI extends GuiScreen {
    public GuiScreen original;
    public ForcedGUI(GuiScreen regularScreen) {
        this.original=regularScreen;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        try {
            original.drawScreen(mouseX,mouseY,partialTicks);
        } catch (Exception err) {}
    }

    @Override
    public List<String> getItemToolTip(ItemStack p_191927_1_) {
        try {
//            return original.getItemToolTip(p_191927_1_);
        } catch (Exception err) {}
        ArrayList<String> uhoh=new ArrayList<>();
        uhoh.add("Uh oh.");
        uhoh.add("GUI Failed");
        return uhoh;
    }

    @Override
    public void drawHoveringText(String text, int x, int y) {
        try {
            original.drawHoveringText(text,x,y);
        } catch (Exception err) {}
    }

    @Override
    public void setFocused(boolean hasFocusedControlIn) {
        try {
            original.setFocused(hasFocusedControlIn);
        } catch (Exception err) {}
    }

    @Override
    public boolean isFocused() {
        try {
            return original.isFocused();
        } catch (Exception err) {}
        return false;
    }

    @Override
    public void drawHoveringText(List<String> textLines, int x, int y) {
        try {
            original.drawHoveringText(textLines,x,y);
        } catch (Exception err) {}
    }

    @Override
    public boolean handleComponentClick(ITextComponent component) {
        try {
            return original.handleComponentClick(component);
        } catch (Exception err) {}
        return false;
    }

    @Override
    public void sendChatMessage(String msg) {
        try {
            original.sendChatMessage(msg);
        } catch (Exception err) {}
    }

    @Override
    public void sendChatMessage(String msg, boolean addToChat) {
        try {
            original.sendChatMessage(msg,addToChat);
        } catch (Exception err) {}
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        try {
            original.setWorldAndResolution(mc,width,height);
        } catch (Exception err) {}
    }

    @Override
    public void setGuiSize(int w, int h) {
        try {
            original.setGuiSize(w,h);
        } catch (Exception err) {}
    }

    @Override
    public void initGui() {
        try {
            original.initGui();
        } catch (Exception err) {}
    }

    @Override
    public void handleInput() throws IOException {
        try {
            original.handleInput();
        } catch (Exception err) {}
    }

    @Override
    public void handleMouseInput() throws IOException {
        try {
            original.handleMouseInput();
        } catch (Exception err) {}
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        try {
            original.handleKeyboardInput();
        } catch (Exception err) {}
    }

    @Override
    public void updateScreen() {
        try {
            original.updateScreen();
        } catch (Exception err) {}
    }

    int countClosed=0;

    @Override
    public void onGuiClosed() {
        try {
            original.onGuiClosed();
        } catch (Exception err) {}
    }

    @Override
    public void drawDefaultBackground() {
        try {
            original.drawDefaultBackground();
        } catch (Exception err) {}
    }

    @Override
    public void drawWorldBackground(int tint) {
        try {
            original.drawWorldBackground(tint);
        } catch (Exception err) {}
    }

    @Override
    public void drawBackground(int tint) {
        try {
            original.drawBackground(tint);
        } catch (Exception err) {}
    }

    @Override
    public boolean doesGuiPauseGame() {
        try {
            return original.doesGuiPauseGame();
        } catch (Exception err) {}
        return false;
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        try {
            original.confirmClicked(result,id);
        } catch (Exception err) {}
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        try {
            original.onResize(mcIn, w, h);
        } catch (Exception err) {}
    }
}
