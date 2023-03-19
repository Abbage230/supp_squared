package net.mehvahdjukaar.suppsquared.client;


import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.HangingSignBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetTextHolderPacket;
import net.mehvahdjukaar.suppsquared.common.PlaqueBlock;
import net.mehvahdjukaar.suppsquared.common.PlaqueBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.stream.IntStream;

public class PlaqueEditScreen extends Screen {
    private TextFieldHelper textInputUtil;
    // The index of the line that is being edited.
    private int editLine = 0;
    //for ticking cursor
    private int updateCounter;
    private final PlaqueBlockTile tileSign;
    private final String[] cachedLines;

    private PlaqueEditScreen(PlaqueBlockTile teSign) {
        super(Component.translatable("sign.edit"));
        this.tileSign = teSign;
        this.cachedLines = IntStream.range(0, PlaqueBlockTile.MAX_LINES).mapToObj(teSign.getTextHolder()::getLine).map(Component::getString).toArray(String[]::new);

    }

    public static void open(PlaqueBlockTile teSign) {
        Minecraft.getInstance().setScreen(new PlaqueEditScreen(teSign));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.textInputUtil.charTyped(codePoint);
        return true;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scrollText((int) delta);
        return true;
    }

    public void scrollText(int amount) {
        this.editLine = Math.floorMod(this.editLine - amount, PlaqueBlockTile.MAX_LINES);
        this.textInputUtil.setCursorToEnd();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // up arrow
        if (keyCode == 265) {
            this.scrollText(1);
            return true;
        }
        // !down arrow, !enter, !enter, handles special keys
        else if (keyCode != 264 && keyCode != 257 && keyCode != 335) {
            return this.textInputUtil.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
        }
        // down arrow, enter
        else {
            this.scrollText(-1);
            return true;
        }
    }

    @Override
    public void tick() {
        ++this.updateCounter;
        if (!this.tileSign.getType().isValid(this.tileSign.getBlockState())) {
            this.close();
        }
    }


    @Override
    public void onClose() {
        this.close();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        // send new text to the server
        NetworkHandler.CHANNEL.sendToServer(new ServerBoundSetTextHolderPacket(this.tileSign.getBlockPos(), this.tileSign.getTextHolder()));
    }

    private void close() {
        this.tileSign.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {

        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, (p_169820_) -> {
            this.close();
        }));
        //this.tileSign.setEditable(false);
        this.textInputUtil = new TextFieldHelper(() -> this.cachedLines[this.editLine], (t) -> {
            this.cachedLines[this.editLine] = t;
            this.tileSign.getTextHolder().setLine(this.editLine, Component.literal(t));
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft),
                (s) -> this.minecraft.font.width(s) <= tileSign.getTextHolder().getMaxLineVisualWidth());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {

        Lighting.setupForFlatItems();
        this.renderBackground(poseStack);

        super.render(poseStack, mouseX, mouseY, partialTicks);

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 40, 16777215);

        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        poseStack.pushPose();

        poseStack.translate((this.width / 2d), 0.0D, 50.0D);
        poseStack.scale(93.75F, -93.75F, 93.75F);
        poseStack.translate(0.0D, -1.3125D, 0.0D);
        // renders sign
        poseStack.pushPose();

        poseStack.mulPose(RotHlpr.Y90);
        poseStack.translate(0, -0.5, -0.5);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        blockRenderer.renderSingleBlock(tileSign.getBlockState().setValue(PlaqueBlock.FACING, Direction.EAST),
                poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        //renders text
        boolean blink = this.updateCounter / 6 % 2 == 0;

        poseStack.translate(0, 0, 0.0625 + 0.005);
        poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
        poseStack.translate(0,-1,0);
        TextUtil.renderGuiText(this.tileSign.getTextHolder().getGUIRenderTextProperties(),
                this.cachedLines, this.font, poseStack, bufferSource, this.textInputUtil.getCursorPos(),
                this.textInputUtil.getSelectionPos(), this.editLine, blink, PlaqueBlockTile.LINE_SEPARATION);

        poseStack.popPose();
        Lighting.setupFor3DItems();

    }
}

