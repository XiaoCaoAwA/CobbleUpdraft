package xiaocaoawa.minecraft.mod.cobbleupdraft.neoforge.compat;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import xiaocaoawa.minecraft.mod.cobbleupdraft.block.PokemonGrabberBlockEntity;

/**
 * 带 Sable 物理集成的宝可梦抓手：当方块被组装进飞行器（sublevel）后，
 * Sable 会在每个物理刻回调 {@link #sable$physicsTick}，此处以 BALLOON_LIFT 力组
 * 向船体施加世界坐标系竖直向上的浮力——机制与 Create Aeronautics 热气球气囊完全一致。
 */
public class SablePokemonGrabberBlockEntity extends PokemonGrabberBlockEntity implements BlockEntitySubLevelActor {

    /** 上一物理刻施加的升力，用于检测升力消失时唤醒休眠的物理体。仅物理线程访问。 */
    private double lastAppliedLift;

    public SablePokemonGrabberBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle body, double dt) {
        double units = getLiftUnits();
        double previous = lastAppliedLift;
        lastAppliedLift = units;
        if (units <= 0.0) {
            // 升力刚消失（收回宝可梦/红石关闭）：唤醒休眠的物理体，让飞行器恢复下落
            if (previous > 0.0) {
                wakeUp(subLevel);
            }
            return;
        }
        // 升力工作期间禁止物理体休眠，否则悬停时会被判定静止而冻结在空中
        wakeUp(subLevel);

        ServerLevel level = subLevel.getLevel();
        Pose3dc pose = subLevel.logicalPose();

        // 方块在飞行器（plot）坐标系中的中心点
        Vector3d plotPos = JOMLConversion.atCenterOf(getBlockPos());

        // 换算为世界坐标，用于采样重力与气压
        Vector3d worldPos = new Vector3d(plotPos).sub(pose.rotationPoint());
        pose.orientation().transform(worldPos).add(pose.position());

        Vector3d gravity = DimensionPhysicsData.getGravity(level, worldPos);
        if (gravity.lengthSquared() < 1.0e-8) {
            return;
        }
        double pressure = DimensionPhysicsData.getAirPressure(level, worldPos);
        if (pressure < 1.0e-5) {
            return;
        }

        // 浮力 = 重力反方向 × 升力 × 气压（与 ServerBalloon.applyForces 相同）
        Vector3d force = new Vector3d(gravity).mul(-units * pressure);
        // 垂直阻尼：抑制围绕悬停高度的无衰减升降振荡（气球式/扑翼式阻尼不同，由 BE 按模式给出）
        double damping = getActiveDamping();
        if (damping > 0.0) {
            force.y -= body.getLinearVelocity().y() * units * damping;
        }
        force.mul(dt);
        // 世界坐标系 → 船体局部坐标系
        pose.orientation().transformInverse(force);

        subLevel.getOrCreateQueuedForceGroup(ForceGroups.BALLOON_LIFT.get())
                .applyAndRecordPointForce(plotPos, force);
    }

    private static void wakeUp(ServerSubLevel subLevel) {
        SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(subLevel.getLevel());
        if (system != null) {
            system.getPipeline().wakeUp(subLevel);
        }
    }
}
