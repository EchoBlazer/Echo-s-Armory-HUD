package me.echo.armordurability;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.List;

public class ArmorDurabilityMod implements ClientModInitializer {

    private static final int MAX_DISTANCE = 10;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(this::renderArmorHud);
    }

    private void renderArmorHud(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.world == null) return;

        HitResult hit = mc.crosshairTarget;
        if (!(hit instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof PlayerEntity target)) return;

        if (mc.player.squaredDistanceTo(target) > MAX_DISTANCE * MAX_DISTANCE) return;

        TextRenderer font = mc.textRenderer;

        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal(target.getName().getString() + "'s Armor"));

        lines.add(formatArmorLine("Helmet",   target.getEquippedStack(EquipmentSlot.HEAD)));
        lines.add(formatArmorLine("Chest",    target.getEquippedStack(EquipmentSlot.CHEST)));
        lines.add(formatArmorLine("Leggings", target.getEquippedStack(EquipmentSlot.LEGS)));
        lines.add(formatArmorLine("Boots",    target.getEquippedStack(EquipmentSlot.FEET)));

        int maxWidth = 0;
        for (Text t : lines) {
            int w = font.getWidth(t);
            if (w > maxWidth) maxWidth = w;
        }

        int lineHeight = font.fontHeight + 2;
        int padding = 4;
        int boxWidth = maxWidth + padding * 2;
        int boxHeight = lines.size() * lineHeight + padding * 2;

        int x = 10;
        int y = 10;

        RenderSystem.enableBlend();
        int bgColor = 0x90000000;
        context.fill(x, y, x + boxWidth, y + boxHeight, bgColor);

        int textX = x + padding;
        int textY = y + padding;
        for (Text line : lines) {
            context.drawText(font, line, textX, textY, 0xFFFFFF, false);
            textY += lineHeight;
        }
    }

    private Text formatArmorLine(String label, ItemStack stack) {
        if (stack.isEmpty()) {
            return Text.literal(label + ": None").styled(s -> s.withColor(0xAAAAAA));
        }

        if (!stack.isDamageable()) {
            return Text.literal(label + ": N/A").styled(s -> s.withColor(0xAAAAAA));
        }

        int max = stack.getMaxDamage();
        int damage = stack.getDamage();
        int remaining = max - damage;
        if (remaining < 0) remaining = 0;

        int percent = (int) ((remaining * 100.0) / max);

        int color;
        if (percent >= 60)      color = 0x55FF55;
        else if (percent >=30) color = 0xFFFF55;
        else                   color = 0xFF5555;

        String text = label + ": " + remaining + "/" + max + " (" + percent + "%)";
        return Text.literal(text).styled(s -> s.withColor(color));
    }
}
