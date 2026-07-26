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

    /**
     * 升力公式：capacity = min(速度 × liftPerSpeedPoint + 总能力值 × liftPerStatTotalPoint, maxLift)。
     * 总能力值 = HP+攻击+防御+特攻+特防+速度 六项之和。
     */
    /** 每点速度提供的升力，单位约等于"能吊起多少格普通方块的重量"。 */
    public double liftPerSpeedPoint = 0.3;
    /** 每点总能力值提供的升力。 */
    public double liftPerStatTotalPoint = 0.05;
    /** 单个抓手的升力上限（红石油门调节前）。 */
    public double maxLift = 100.0;
    /** 无红石信号时的最小油门（0~1）。0 表示没有红石信号就没有升力。 */
    public double minThrottle = 0.0;
    /**
     * 升力模式：漂浮特性的宝可梦 = 气球式（平稳缓升缓降）；
     * 飞行系/飞翔技能/会飞等扇翅膀的 = 扑翼式（响应快但有周期性颠簸）。
     */
    /** 气球式：升力充气时间（tick），复刻 CA 热气球 fillingTime=180。 */
    public double liftFillingTimeTicks = 180.0;
    /** 气球式：升力排气时间（tick），复刻 CA 热气球 emptyingTime=180。 */
    public double liftEmptyingTimeTicks = 180.0;
    /** 扑翼式：升力上升响应时间（tick），比气球快得多。 */
    public double wingedFillingTimeTicks = 40.0;
    /** 扑翼式：升力回落时间（tick）。 */
    public double wingedEmptyingTimeTicks = 60.0;
    /** 扑翼式：颠簸幅度（占当前升力的比例，0 关闭）。 */
    public double wingedTurbulence = 0.15;
    /** 扑翼式：颠簸周期（tick），对应扇翅膀的节奏。 */
    public double wingedTurbulencePeriodTicks = 40.0;
    /** 扑翼式：垂直阻尼（每秒），较小以保留颠簸感。 */
    public double wingedDamping = 0.05;
    /** 接近目标值时的收敛加速系数（CA responsivenessAdjustmentFactor=5.0），0 关闭。 */
    public double responsivenessFactor = 5.0;
    /** 收敛加速的生效区间，占容量的比例（CA responsivenessAdjustmentRange=0.05）。 */
    public double responsivenessRange = 0.05;
    /** 垂直阻尼系数（每秒）。CA 热气球本体无阻尼（设 0 即完全一致）；保留少量作为防振荡保险。 */
    public double liftDamping = 0.2;
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
              // 升力公式: min(速度 × liftPerSpeedPoint + 总能力值 × liftPerStatTotalPoint, maxLift)
              // 总能力值 = HP+攻击+防御+特攻+特防+速度
              // 每点速度提供的升力（大约等于能吊起多少格方块的重量）
              "liftPerSpeedPoint": 0.3,
              // 每点总能力值提供的升力
              "liftPerStatTotalPoint": 0.05,
              // 单个宝可梦抓手的升力上限（红石油门调节前）
              "maxLift": 100.0,
              // 无红石信号时的最小油门(0~1)。0 表示没有红石信号就没有升力
              "minThrottle": 0.0,
              // ===== 升力模式：漂浮特性=气球式（平稳）；飞行系/飞翔技能等=扑翼式（颠簸） =====
              // 气球式：升力充气时间（tick）（复刻机械动力热气球 180 tick = 9 秒）
              "liftFillingTimeTicks": 180.0,
              // 气球式：升力排气时间（tick）（复刻机械动力热气球 180 tick）
              "liftEmptyingTimeTicks": 180.0,
              // 扑翼式：升力上升响应时间（tick）
              "wingedFillingTimeTicks": 40.0,
              // 扑翼式：升力回落时间（tick）
              "wingedEmptyingTimeTicks": 60.0,
              // 扑翼式：颠簸幅度（占当前升力的比例，0 关闭）
              "wingedTurbulence": 0.15,
              // 扑翼式：颠簸周期（tick）
              "wingedTurbulencePeriodTicks": 40.0,
              // 扑翼式：垂直阻尼（每秒）
              "wingedDamping": 0.05,
              // 接近目标值时的收敛加速系数（机械动力为 5.0），0 关闭
              "responsivenessFactor": 5.0,
              // 收敛加速的生效区间，占容量的比例（机械动力为 0.05）
              "responsivenessRange": 0.05,
              // 垂直阻尼系数（每秒）。机械动力热气球本体无阻尼（设 0 即完全一致）；保留少量作为防振荡保险
              "liftDamping": 0.2,
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
