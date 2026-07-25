package xiaocaoawa.minecraft.mod.cobbleupdraft.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import xiaocaoawa.minecraft.mod.cobbleupdraft.registry.ModRegistry;

/**
 * 客户端初始化：锚点实体本身不可见，其渲染器只负责绘制到宝可梦的绳子。
 */
public final class CobbleUpdraftClient {
    private CobbleUpdraftClient() {
    }

    public static void init() {
        EntityRendererRegistry.register(ModRegistry.GRABBER_ANCHOR, GrabberAnchorRenderer::new);
    }
}
