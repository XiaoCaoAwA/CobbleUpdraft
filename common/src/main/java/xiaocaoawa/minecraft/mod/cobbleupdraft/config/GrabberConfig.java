package xiaocaoawa.minecraft.mod.cobbleupdraft.config;

import com.google.gson.Gson;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 宝可梦抓手配置，位于 config/cobbleupdraft.json。
 */
public final class GrabberConfig {
    private static final Gson GSON = new Gson();
    private static GrabberConfig instance = new GrabberConfig();

    /** 每点速度种族值提供的升力，单位约等于"能吊起多少格普通方块的重量"。 */
    public double liftPerSpeedPoint = 0.5;
    /** 单个抓手的升力上限（红石油门调节前）。 */
    public double maxLift = 100.0;
    /** 无红石信号时的最小油门（0~1）。0 表示没有红石信号就没有升力。 */
    public double minThrottle = 0.0;
    /** 为 true 时任何宝可梦都能提供升力（忽略下面的判定条件）。 */
    public boolean anyPokemonCanLift = false;
    /** 飞行系宝可梦可提供升力。 */
    public boolean flyingTypeLifts = true;
    /** 漂浮特性的宝可梦可提供升力。 */
    public boolean levitateAbilityLifts = true;
    /** 学会了"飞翔"技能的宝可梦可提供升力。 */
    public boolean flyMoveLifts = true;
    /** 行为数据标记会飞（canFly）的宝可梦可提供升力。 */
    public boolean canFlyBehaviourLifts = true;
    /** 宝可梦悬浮在方块上方的高度（格），用于播放扇翅膀的悬浮动画。 */
    public double hoverHeight = 0.3;

    public static GrabberConfig get() {
        return instance;
    }

    public static void load() {
        Path path = Platform.getConfigFolder().resolve("cobbleupdraft.json");
        try {
            if (Files.exists(path)) {
                try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    GrabberConfig loaded = GSON.fromJson(reader, GrabberConfig.class);
                    if (loaded != null) {
                        instance = loaded;
                    }
                }
            } else {
                Files.createDirectories(path.getParent());
                Files.writeString(path, DEFAULT_FILE, StandardCharsets.UTF_8);
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("[CobbleUpdraft] Failed to load config, using defaults: " + e);
        }
    }

    private static final String DEFAULT_FILE = """
            {
              // 每点速度种族值提供的升力（大约等于能吊起多少格方块的重量）
              "liftPerSpeedPoint": 0.5,
              // 单个宝可梦抓手的升力上限（红石油门调节前）
              "maxLift": 100.0,
              // 无红石信号时的最小油门(0~1)。0 表示没有红石信号就没有升力
              "minThrottle": 0.0,
              // 为 true 时任何宝可梦都能提供升力（忽略下面的判定条件）
              "anyPokemonCanLift": false,
              // 飞行系宝可梦可提供升力
              "flyingTypeLifts": true,
              // 漂浮特性的宝可梦可提供升力
              "levitateAbilityLifts": true,
              // 学会了"飞翔"技能的宝可梦可提供升力
              "flyMoveLifts": true,
              // 行为数据标记会飞(canFly)的宝可梦可提供升力
              "canFlyBehaviourLifts": true,
              // 宝可梦悬浮在方块上方的高度（格）
              "hoverHeight": 0.3
            }
            """;
}
