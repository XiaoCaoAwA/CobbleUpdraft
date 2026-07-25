package xiaocaoawa.minecraft.mod.cobbleupdraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import xiaocaoawa.minecraft.mod.cobbleupdraft.entity.GrabberAnchorEntity;

/**
 * 绘制锚点与目标宝可梦之间的绳子。顶点、光照与下垂曲线的算法照搬原版拴绳
 * （MobRenderer#renderLeash），但完全独立于原版拴绳数据，纯视觉效果。
 */
public class GrabberAnchorRenderer extends EntityRenderer<GrabberAnchorEntity> {

    public GrabberAnchorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GrabberAnchorEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(GrabberAnchorEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        // 绳子可能延伸到视锥外，只要有目标就渲染
        return entity.getTargetId() != 0;
    }

    @Override
    public void render(GrabberAnchorEntity anchor, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(anchor, entityYaw, partialTicks, poseStack, buffer, packedLight);
        Entity target = anchor.level().getEntity(anchor.getTargetId());
        if (!(target instanceof Mob mob) || !mob.isAlive()) {
            return;
        }

        // 锚点端起点（渲染坐标系原点即锚点插值位置）
        Vec3 start = anchor.getPosition(partialTicks);

        // 宝可梦端挂绳点：身体朝向旋转后的 leashOffset（与原版一致）
        double bodyRot = Mth.lerp(partialTicks, mob.yBodyRotO, mob.yBodyRot) * (Math.PI / 180.0) + (Math.PI / 2.0);
        Vec3 leashOffset = mob.getLeashOffset(partialTicks);
        double offX = Math.cos(bodyRot) * leashOffset.z + Math.sin(bodyRot) * leashOffset.x;
        double offZ = Math.sin(bodyRot) * leashOffset.z - Math.cos(bodyRot) * leashOffset.x;
        double endX = Mth.lerp(partialTicks, mob.xo, mob.getX()) + offX;
        double endY = Mth.lerp(partialTicks, mob.yo, mob.getY()) + leashOffset.y;
        double endZ = Mth.lerp(partialTicks, mob.zo, mob.getZ()) + offZ;

        float dx = (float) (endX - start.x);
        float dy = (float) (endY - start.y);
        float dz = (float) (endZ - start.z);

        poseStack.pushPose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.leash());
        Matrix4f pose = poseStack.last().pose();
        float widthScale = Mth.invSqrt(dx * dx + dz * dz) * 0.025f / 2.0f;
        float wz = dz * widthScale;
        float wx = dx * widthScale;

        BlockPos startPos = BlockPos.containing(anchor.getEyePosition(partialTicks));
        BlockPos endPos = BlockPos.containing(mob.getEyePosition(partialTicks));
        int startBlockLight = anchor.level().getBrightness(LightLayer.BLOCK, startPos);
        int endBlockLight = anchor.level().getBrightness(LightLayer.BLOCK, endPos);
        int startSkyLight = anchor.level().getBrightness(LightLayer.SKY, startPos);
        int endSkyLight = anchor.level().getBrightness(LightLayer.SKY, endPos);

        for (int i = 0; i <= 24; i++) {
            addVertexPair(consumer, pose, dx, dy, dz, startBlockLight, endBlockLight,
                    startSkyLight, endSkyLight, 0.025f, 0.025f, wz, wx, i, false);
        }
        for (int i = 24; i >= 0; i--) {
            addVertexPair(consumer, pose, dx, dy, dz, startBlockLight, endBlockLight,
                    startSkyLight, endSkyLight, 0.025f, 0.0f, wz, wx, i, true);
        }
        poseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer consumer, Matrix4f pose,
                                      float dx, float dy, float dz,
                                      int startBlockLight, int endBlockLight,
                                      int startSkyLight, int endSkyLight,
                                      float thickness, float yOffset,
                                      float wz, float wx, int index, boolean reverse) {
        float t = (float) index / 24.0f;
        int blockLight = (int) Mth.lerp(t, (float) startBlockLight, (float) endBlockLight);
        int skyLight = (int) Mth.lerp(t, (float) startSkyLight, (float) endSkyLight);
        int packedLight = LightTexture.pack(blockLight, skyLight);
        float shade = index % 2 == (reverse ? 1 : 0) ? 0.7f : 1.0f;
        float r = 0.5f * shade;
        float g = 0.4f * shade;
        float b = 0.3f * shade;
        float x = dx * t;
        // 下垂曲线（与原版拴绳一致）
        float y = dy > 0.0f ? dy * t * t : dy - dy * (1.0f - t) * (1.0f - t);
        float z = dz * t;
        consumer.addVertex(pose, x - wz, y + yOffset, z + wx).setColor(r, g, b, 1.0f).setLight(packedLight);
        consumer.addVertex(pose, x + wz, y + thickness - yOffset, z - wx).setColor(r, g, b, 1.0f).setLight(packedLight);
    }
}
