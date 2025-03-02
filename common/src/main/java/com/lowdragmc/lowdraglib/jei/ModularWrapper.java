package com.lowdragmc.lowdraglib.jei;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ModularWrapper<T extends Widget> extends ModularUIGuiContainer {
    @Getter
    protected T widget;
    @Getter @Setter
    protected boolean shouldRenderTooltips = false;

    public ModularWrapper(T widget) {
        super(new ModularUI(widget.getSize().width, widget.getSize().height, IUIHolder.EMPTY, Minecraft.getInstance().player).widget(widget), -1);
        modularUI.initWidgets();
        this.minecraft = Minecraft.getInstance();
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.font;
        this.widget = widget;
    }

    private int lastTick;
    @Getter
    private int left, top;

    public String getUid() {
        return null;
    }

    /**
     * For JEI to use
     */
    public void setRecipeLayout(int left, int top) {
        modularUI.initWidgets();
        this.left = left;
        this.top = top;
        this.width = minecraft.getWindow().getGuiScaledWidth();
        this.height = minecraft.getWindow().getGuiScaledHeight();
        modularUI.updateScreenSize(this.width, this.height);
        Position displayOffset = new Position(modularUI.getGuiLeft(), top);
        modularUI.mainGroup.setParentPosition(displayOffset);
    }

    /**
     * For REI to use
     */
    public void setRecipeWidget(int left, int top) {
        modularUI.initWidgets();
        this.left = 0;
        this.top = 0;
        this.width = minecraft.getWindow().getGuiScaledWidth();
        this.height = minecraft.getWindow().getGuiScaledHeight();
        modularUI.updateScreenSize(this.width, this.height);
        Position displayOffset = new Position(left, top);
        modularUI.mainGroup.setParentPosition(displayOffset);
    }

    public void setEmiRecipeWidget(int left, int top) {
        modularUI.initWidgets();
        this.left = left;
        this.top = top;
        this.width = minecraft.getWindow().getGuiScaledWidth();
        this.height = minecraft.getWindow().getGuiScaledHeight();
        modularUI.updateScreenSize(this.width, this.height);
        Position displayOffset = new Position(left, top);
        modularUI.mainGroup.setParentPosition(displayOffset);
    }

    public void draw(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (minecraft.player.tickCount != lastTick) {
            updateScreen();
            lastTick = minecraft.player.tickCount;
        }
        matrixStack.pushPose();
        matrixStack.translate(-left, -top, 0);
        render(matrixStack, mouseX + left, mouseY + top, partialTicks);
        matrixStack.popPose();
    }

    public void updateScreen() {
        modularUI.mainGroup.updateScreen();
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.hoveredSlot = null;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        tooltipTexts = null;
        tooltipFont = null;
        tooltipStack = ItemStack.EMPTY;
        tooltipComponent = null;

        modularUI.mainGroup.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
        modularUI.mainGroup.drawInForeground(matrixStack, mouseX, mouseY, partialTicks);

        if (shouldRenderTooltips && tooltipTexts != null && !tooltipTexts.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.translate(0, 0, 240);
            renderTooltip(matrixStack, tooltipTexts, Optional.ofNullable(tooltipComponent), mouseX, mouseY);
            matrixStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }

}
