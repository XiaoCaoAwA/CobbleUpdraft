package xiaocaoawa.minecraft.mod.cobbleupdraft.platform.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import xiaocaoawa.minecraft.mod.cobbleupdraft.block.PokemonGrabberBlockEntity;
import xiaocaoawa.minecraft.mod.cobbleupdraft.neoforge.compat.SableCompat;

/**
 * NeoForge 侧：安装了 Sable（Create Aeronautics 物理引擎）时启用升力集成，
 * 未安装时回退到基础方块实体。Sable 相关类只在 {@link SableCompat} 中引用，避免类加载失败。
 */
public final class GrabberPlatformImpl {
    private GrabberPlatformImpl() {
    }

    private static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");

    public static PokemonGrabberBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (SABLE_LOADED && CREATE_LOADED) {
            return SableCompat.createGoggleBlockEntity(pos, state);
        }
        if (SABLE_LOADED) {
            return SableCompat.createBlockEntity(pos, state);
        }
        return new PokemonGrabberBlockEntity(pos, state);
    }

    public static Vec3 projectToWorld(Level level, Vec3 pos) {
        return SABLE_LOADED ? SableCompat.projectToWorld(level, pos) : pos;
    }
}
