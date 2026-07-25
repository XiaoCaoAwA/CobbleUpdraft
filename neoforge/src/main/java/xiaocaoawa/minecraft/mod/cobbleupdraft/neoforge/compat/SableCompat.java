package xiaocaoawa.minecraft.mod.cobbleupdraft.neoforge.compat;

import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import xiaocaoawa.minecraft.mod.cobbleupdraft.block.PokemonGrabberBlockEntity;

/**
 * Sable 入口封装。只有在 sable 模组存在时才会加载本类。
 */
public final class SableCompat {
    private SableCompat() {
    }

    public static PokemonGrabberBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SablePokemonGrabberBlockEntity(pos, state);
    }

    /** create + sable 同时存在：附加护目镜升力信息。 */
    public static PokemonGrabberBlockEntity createGoggleBlockEntity(BlockPos pos, BlockState state) {
        return new GoggleSablePokemonGrabberBlockEntity(pos, state);
    }

    public static Vec3 projectToWorld(Level level, Vec3 pos) {
        return Sable.HELPER.projectOutOfSubLevel(level, pos);
    }
}
