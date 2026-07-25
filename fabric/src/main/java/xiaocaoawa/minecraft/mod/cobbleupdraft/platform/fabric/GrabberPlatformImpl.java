package xiaocaoawa.minecraft.mod.cobbleupdraft.platform.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import xiaocaoawa.minecraft.mod.cobbleupdraft.block.PokemonGrabberBlockEntity;

/**
 * Fabric 侧没有 Create Aeronautics（1.21.1 仅 NeoForge），只提供基础功能，不产生船体升力。
 */
public final class GrabberPlatformImpl {
    private GrabberPlatformImpl() {
    }

    public static PokemonGrabberBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PokemonGrabberBlockEntity(pos, state);
    }

    public static Vec3 projectToWorld(Level level, Vec3 pos) {
        return pos;
    }
}
