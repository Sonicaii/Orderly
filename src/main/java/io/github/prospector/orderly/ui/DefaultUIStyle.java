package io.github.prospector.orderly.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.orderly.Orderly;
import io.github.prospector.orderly.api.UIStyle;
import io.github.prospector.orderly.api.config.OrderlyConfig;
import io.github.prospector.orderly.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.registry.Registries;

public class DefaultUIStyle extends SimpleUIStyle {
    private static final UIStyle INSTANCE = new DefaultUIStyle();
    private static final float SCALE_MULTIPLIER = 0.026666672F;
    private static final Identifier TEXTURE = new Identifier(Orderly.MODID, "textures/ui/default_health_bar.png");

    public static UIStyle getInstance() {
        return INSTANCE;
    }

    @Override
    protected void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, OrderlyConfig config, LivingEntity entity, int light, ItemStack icon, boolean boss) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Quaternionf rotation = new Quaternionf(camera.getRotation());
        rotation.scale(-1.0F);
        matrices.multiply(rotation);
        float scale = SCALE_MULTIPLIER * config.getHealthBarScale();
        matrices.scale(-scale, -scale, scale);
        float health = MathHelper.clamp(entity.getHealth(), 0.0F, entity.getMaxHealth());
        float percent = (health / entity.getMaxHealth()) * 100.0F;
        float size = boss ? config.getPlateSizeBoss() : config.getPlateSize();
        float textScale = 0.5F;
        //noinspection ConstantConditions
        String name = (entity.hasCustomName() ? ((MutableText) entity.getCustomName()).formatted(Formatting.ITALIC) : entity.getDisplayName()).getString();
        float namel = mc.textRenderer.getWidth(name) * textScale;
        if (namel + 20 > size * 2) {
            size = namel / 2.0F + 10.0F;
        }
        float healthSize = size * (health / entity.getMaxHealth());
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f modelViewMatrix = entry.getPositionMatrix();
        Vector3f normal = new Vector3f(0.0F, 1.0F, 0.0F);
        normal.mulTranspose(entry.getNormalMatrix());
        VertexConsumer buffer = immediate.getBuffer(RenderLayer.getTextSeeThrough(TEXTURE)); // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        int barHeight = config.getBarHeight();
        final int overlay = OverlayTexture.DEFAULT_UV;

        RenderSystem.polygonOffset(0.0f, 0.0f);
        // Background
        if (config.drawsBackground()) {
            int bgHeight = config.getBackgroundHeight();
            float padding = config.getBackgroundPadding();
            buffer.vertex(modelViewMatrix, -size - padding, -bgHeight, 0.0F).color(255, 255, 255, 64).texture(0.0F, 0.0F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
            buffer.vertex(modelViewMatrix, -size - padding, barHeight + padding, 0.0F).color(255, 255, 255, 64).texture(0.0F, 0.5F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
            buffer.vertex(modelViewMatrix, size + padding, barHeight + padding, 0.0F).color(255, 255, 255, 64).texture(1.0F, 0.5F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
            buffer.vertex(modelViewMatrix, size + padding, -bgHeight, 0.0F).color(255, 255, 255, 64).texture(1.0F, 0.0F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        }
        immediate.draw();
        buffer = immediate.getBuffer(RenderLayer.getTextSeeThrough(TEXTURE));
        RenderSystem.polygonOffset(0.0f, -1.0f);
        // Health Bar Background
        buffer.vertex(modelViewMatrix, -size, 0, -0.000F).color(255, 255, 255, 127).texture(0.0F, 0.5F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        buffer.vertex(modelViewMatrix, -size, barHeight, -0.000F).color(255, 255, 255, 127).texture(0.0F, 0.75F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        buffer.vertex(modelViewMatrix, size, barHeight, -0.000F).color(255, 255, 255, 127).texture(1.0F, 0.75F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        buffer.vertex(modelViewMatrix, size, 0, -0.000F).color(255, 255, 255, 127).texture(1.0F, 0.5F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        immediate.draw();

        buffer = immediate.getBuffer(RenderLayer.getTextSeeThrough(TEXTURE));
        RenderSystem.polygonOffset(0.0f, -2.0f);
        // Health Bar
        int argb = RenderUtil.getColor(entity, config.colorByType(), boss);
        int r = RenderUtil.getRed(argb);
        int g = RenderUtil.getGreen(argb);
        int b = RenderUtil.getBlue(argb);
        buffer.vertex(modelViewMatrix, -size, 0, 0.000F).color(r, g, b, 127).texture(0.0F, 0.75F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        buffer.vertex(modelViewMatrix, -size, barHeight, -0.000F).color(r, g, b, 127).texture(0.0F, 1.0F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        buffer.vertex(modelViewMatrix, healthSize * 2 - size, barHeight, -0.000F).color(r, g, b, 127).texture(1.0F, 1.0F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        buffer.vertex(modelViewMatrix, healthSize * 2 - size, 0, -0.000F).color(r, g, b, 127).texture(1.0F, 0.75F).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        immediate.draw();

        RenderSystem.polygonOffset(0.0f, -3.0f);
        // Foreground
        {
            int white = 0xFFFFFF;
            int black = 0x000000;
            matrices.translate(-size, -4.5F, 0.0F);
            matrices.scale(textScale, textScale, textScale);
            modelViewMatrix = matrices.peek().getPositionMatrix();
            mc.textRenderer.draw(name, 0, 0, white, false, modelViewMatrix, immediate, true, black, light);
            float s1 = 0.75F;
            matrices.push();
            {
                matrices.scale(s1, s1, s1);
                modelViewMatrix = matrices.peek().getPositionMatrix();
                int h = config.getHpTextHeight();
                String maxHpStr = String.format("%s%.2f", Formatting.BOLD, entity.getMaxHealth()).replaceAll("\\.00$", "");
                String hpStr = String.format("%.2f", health).replaceAll("\\.00$", "");
                String percStr = String.format("%.2f%%", percent).replace(".00%", "%");
                if (maxHpStr.endsWith(".00")) {
                    maxHpStr = maxHpStr.substring(0, maxHpStr.length() - 3);
                }
                if (hpStr.endsWith(".00")) {
                    hpStr = hpStr.substring(0, hpStr.length() - 3);
                }
                if (config.showCurrentHP()) {
                    mc.textRenderer.draw(hpStr, 2, h, white, false, modelViewMatrix, immediate, true, black, light);
                }
                if (config.canShowMaxHP()) {
                    mc.textRenderer.draw(maxHpStr, (int) (size / (textScale * s1) * 2) - 2 - mc.textRenderer.getWidth(maxHpStr), h, white, false, modelViewMatrix, immediate, true, black, light);
                }
                if (config.canShowPercentage()) {
                    mc.textRenderer.draw(percStr, (int) (size / (textScale * s1)) - mc.textRenderer.getWidth(percStr) / 2.0F, h, white, false, modelViewMatrix, immediate, true, black, light);
                }
                if (config.isDebugInfoEnabled() && mc.options.debugEnabled) {
                    mc.textRenderer.draw(String.format("ID: \"%s\"", Registries.ENTITY_TYPE.getId(entity.getType())), 0, h + 16, white, false, modelViewMatrix, immediate, true, black, light);
                }
            }
            matrices.pop();
            matrices.push();
            int off = 0;
            s1 = 0.5F;
            matrices.scale(s1, s1, s1);
            matrices.translate(size / (textScale * s1) * 2 - 16, 0.0F, 0.0F);
            mc.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
            if (icon != null && config.canShowAttributes()) {
                renderIcon(off, 0, icon, matrices, immediate, OverlayTexture.DEFAULT_UV, light);
                off -= 16;
            }
            int armor = entity.getArmor();
            if (armor > 0 && config.canShowArmor()) {
                int ironArmor = armor % 5;
                int diamondArmor = armor / 5;
                if (!config.canShowGroupArmor()) {
                    ironArmor = armor;
                    diamondArmor = 0;
                }
                icon = new ItemStack(Items.IRON_CHESTPLATE);
                for (int i = 0; i < ironArmor; i++) {
                    renderIcon(off, 0, icon, matrices, immediate, OverlayTexture.DEFAULT_UV, light);
                    off -= 4;
                }
                icon = new ItemStack(Items.DIAMOND_CHESTPLATE);
                for (int i = 0; i < diamondArmor; i++) {
                    renderIcon(off, 0, icon, matrices, immediate, OverlayTexture.DEFAULT_UV, light);
                    off -= 4;
                }
            }
            matrices.pop();
        }
        RenderSystem.polygonOffset(0.0f, 0.0f);
    }

    private void renderIcon(double x, double y, ItemStack stack, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, int overlay, int light) {
        MinecraftClient mc = MinecraftClient.getInstance();
        matrices.push();
        matrices.translate(x, y, -0.002D);
        matrices.scale(16.0F, 16.0F, 1.0F);
        try {
            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getTextSeeThrough(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
            BakedModel bakedModel = mc.getItemRenderer().getModels().getModel(stack);
            Sprite textureAtlasSprite = bakedModel.getParticleSprite();
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f modelViewMatrix = entry.getPositionMatrix();
            Vector3f normal = new Vector3f(0.0F, 1.0F, 0.0F);
            normal.mul(entry.getNormalMatrix());
            buffer.vertex(modelViewMatrix, 0.0F, 0.0F, 0.0F).color(255, 255, 255, 255).texture(textureAtlasSprite.getMinU(), textureAtlasSprite.getMinV()).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
            buffer.vertex(modelViewMatrix, 0.0F, 1.0F, 0.0F).color(255, 255, 255, 255).texture(textureAtlasSprite.getMinU(), textureAtlasSprite.getMaxV()).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
            buffer.vertex(modelViewMatrix, 1.0F, 1.0F, 0.0F).color(255, 255, 255, 255).texture(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMaxV()).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
            buffer.vertex(modelViewMatrix, 1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).texture(textureAtlasSprite.getMaxU(), textureAtlasSprite.getMinV()).overlay(overlay).light(light).normal(normal.x(), normal.y(), normal.z()).next();
        } catch (Exception ignore) {
            //TODO exception handling?
        }
        matrices.pop();
        vertexConsumers.draw();
    }
}
