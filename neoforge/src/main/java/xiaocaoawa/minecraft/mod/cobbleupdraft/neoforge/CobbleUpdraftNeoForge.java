package xiaocaoawa.minecraft.mod.cobbleupdraft.neoforge;

import xiaocaoawa.minecraft.mod.cobbleupdraft.CobbleUpdraft;
import net.neoforged.fml.common.Mod;

@Mod(CobbleUpdraft.MOD_ID)
public final class CobbleUpdraftNeoForge {
    public CobbleUpdraftNeoForge() {
        // Run our common setup.
        CobbleUpdraft.init();
    }
}
