package xiaocaoawa.minecraft.mod.cobbleupdraft.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import xiaocaoawa.minecraft.mod.cobbleupdraft.block.PokemonGrabberBlockEntity;

public final class GrabberPlatform {
    private GrabberPlatform() {
    }

    /**
     * 创建宝可梦抓手方块实体。NeoForge 侧在检测到 Sable（Create Aeronautics 物理引擎）时
     * 返回带升力物理集成的子类。
     */
    @ExpectPlatform
    public static PokemonGrabberBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    /**
     * 若坐标位于飞行器（sublevel/plot）空间，则投影为真实世界坐标；否则原样返回。
     */
    @ExpectPlatform
    public static Vec3 projectToWorld(Level level, Vec3 pos) {
        throw new AssertionError();
    }

    /**
     * 该坐标处飞行器的运动速度（世界坐标系，格/秒）；不在飞行器上时为零。
     * 用于锚定宝可梦时做一 tick 位置前瞻，消除高速移动时的拖影。
     */
    @ExpectPlatform
    public static Vec3 shipVelocity(Level level, Vec3 pos) {
        throw new AssertionError();
    }
}
