package xiaocaoawa.minecraft.mod.cobbleupdraft;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import xiaocaoawa.minecraft.mod.cobbleupdraft.client.CobbleUpdraftClient;
import xiaocaoawa.minecraft.mod.cobbleupdraft.config.GrabberConfig;
import xiaocaoawa.minecraft.mod.cobbleupdraft.lock.GrabberLockManager;
import xiaocaoawa.minecraft.mod.cobbleupdraft.registry.ModRegistry;

public final class CobbleUpdraft {
    public static final String MOD_ID = "cobbleupdraft";

    public static void init() {
        GrabberConfig.load();
        ModRegistry.init();
        GrabberLockManager.init();
        EnvExecutor.runInEnv(Env.CLIENT, () -> CobbleUpdraftClient::init);
    }
}
