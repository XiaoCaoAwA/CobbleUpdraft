package xiaocaoawa.minecraft.mod.cobbleupdraft.block;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.callback.PartySelectCallbacks;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xiaocaoawa.minecraft.mod.cobbleupdraft.config.GrabberConfig;
import xiaocaoawa.minecraft.mod.cobbleupdraft.entity.GrabberAnchorEntity;
import xiaocaoawa.minecraft.mod.cobbleupdraft.platform.GrabberPlatform;
import xiaocaoawa.minecraft.mod.cobbleupdraft.registry.ModRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 宝可梦抓手：右键选择队伍中的宝可梦站上来。
 * 飞行系 / 会飞 / 漂浮特性的宝可梦会扇动翅膀，为其所在的飞行器提供升力，
 * 升力大小与宝可梦速度种族值和红石信号强度相关（见 {@link GrabberConfig}）。
 */
public class PokemonGrabberBlockEntity extends BlockEntity {
    /** 宝可梦实体连续多少刻找不到后自动清除绑定（区块加载等待宽限）。 */
    private static final int MISSING_TIMEOUT_TICKS = 200;
    /** 方块碰撞箱顶面高度。 */
    private static final double TOP_Y = 6.0 / 16.0;

    @Nullable
    private UUID ownerId;
    @Nullable
    private UUID pokemonId;
    @Nullable
    private UUID entityId;
    @Nullable
    private UUID anchorId;
    private int missingTicks;
    /** 上一刻的锚定点（世界坐标），用于计算移动方向让宝可梦转身。不持久化。 */
    @Nullable
    private Vec3 lastAnchorPos;

    /** 当前升力（单位：可吊起的方块重量数），由服务端 tick 写入、物理线程读取。 */
    private volatile double liftUnits;

    // ==== 护目镜显示数据（服务端计算，同步到客户端） ====
    private String displayPokemonName = "";
    private int displaySpeed;
    private boolean displayLifter;
    private int displayPower;
    private int displayThrottlePercent;

    public PokemonGrabberBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.POKEMON_GRABBER_BE.get(), pos, state);
    }

    public double getLiftUnits() {
        return liftUnits;
    }

    public boolean hasPokemon() {
        return pokemonId != null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PokemonGrabberBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (be.pokemonId == null) {
            be.liftUnits = 0.0;
            be.updateDisplay("", 0, false, 0, 0);
            return;
        }
        PokemonEntity entity = be.resolveEntity(serverLevel);
        if (entity == null) {
            be.liftUnits = 0.0;
            be.updateDisplay("", 0, false, 0, 0);
            if (++be.missingTicks > MISSING_TIMEOUT_TICKS) {
                be.clearAssignment();
            }
            return;
        }
        be.missingTicks = 0;

        GrabberConfig cfg = GrabberConfig.get();
        Pokemon pokemon = entity.getPokemon();
        boolean lifter = isLifter(pokemon);

        Vec3 anchor = be.worldAnchor(lifter ? cfg.hoverHeight : 0.0);
        entity.setNoAi(true);
        entity.setNoGravity(true);
        entity.setPos(anchor.x, anchor.y, anchor.z);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0f;
        be.faceMovementDirection(entity, anchor);
        be.ensureLeash(serverLevel, entity);
        // 扇翅膀：悬浮姿态（HOVER）动画
        if (entity.getBehaviourFlag(PokemonBehaviourFlag.FLYING) != lifter) {
            entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, lifter);
        }

        // 红石油门：信号强度 0~15 线性调节升力
        int power = level.getBestNeighborSignal(pos);
        double throttle = Math.max(cfg.minThrottle, power / 15.0);
        be.liftUnits = lifter
                ? Math.min(pokemon.getSpeed() * cfg.liftPerSpeedPoint, cfg.maxLift) * throttle
                : 0.0;
        be.updateDisplay(entity.getName().getString(), pokemon.getSpeed(), lifter,
                power, (int) Math.round(throttle * 100.0));
    }

    /** 更新护目镜显示数据，有变化时同步到客户端。 */
    private void updateDisplay(String name, int speed, boolean lifter, int power, int throttlePercent) {
        if (name.equals(displayPokemonName)
                && speed == displaySpeed
                && lifter == displayLifter
                && power == displayPower
                && throttlePercent == displayThrottlePercent) {
            return;
        }
        displayPokemonName = name;
        displaySpeed = speed;
        displayLifter = lifter;
        displayPower = power;
        displayThrottlePercent = throttlePercent;
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public String getDisplayPokemonName() {
        return displayPokemonName;
    }

    public int getDisplaySpeed() {
        return displaySpeed;
    }

    public boolean isDisplayLifter() {
        return displayLifter;
    }

    public int getDisplayPower() {
        return displayPower;
    }

    public int getDisplayThrottlePercent() {
        return displayThrottlePercent;
    }

    /** 右键交互入口：已有宝可梦则收回，否则打开队伍选择界面。 */
    public void onUse(ServerPlayer player) {
        if (hasPokemon()) {
            release(true);
            return;
        }
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        List<Pokemon> list = new ArrayList<>();
        for (Pokemon pokemon : party) {
            list.add(pokemon);
        }
        if (list.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.cobbleupdraft.pokemon_grabber.empty_party"), true);
            return;
        }
        PartySelectCallbacks.INSTANCE.createFromPokemon(
                player,
                Component.translatable("ui.cobbleupdraft.pokemon_grabber.select"),
                list,
                pokemon -> !pokemon.isFainted(),
                p -> Unit.INSTANCE,
                pokemon -> {
                    assignPokemon(player, pokemon);
                    return Unit.INSTANCE;
                }
        );
    }

    private void assignPokemon(ServerPlayer player, Pokemon pokemon) {
        if (isRemoved() || hasPokemon() || pokemon.isFainted()) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 anchor = worldAnchor(0.0);
        PokemonEntity existing = pokemon.getEntity();
        if (existing != null && existing.isAlive()) {
            existing.setPos(anchor.x, anchor.y, anchor.z);
            adopt(player, pokemon, existing);
        } else {
            pokemon.sendOutWithAnimation(player, serverLevel, anchor, null, true, null, e -> Unit.INSTANCE)
                    .thenAccept(entity -> {
                        if (!isRemoved() && !hasPokemon()) {
                            adopt(player, pokemon, entity);
                        }
                    });
        }
    }

    private void adopt(ServerPlayer player, Pokemon pokemon, PokemonEntity entity) {
        this.ownerId = player.getUUID();
        this.pokemonId = pokemon.getUuid();
        this.entityId = entity.getUUID();
        this.missingTicks = 0;
        setChanged();
    }

    /** 让宝可梦平滑转向移动方向（方块随飞行器移动时生效，静止时保持原朝向）。 */
    private void faceMovementDirection(PokemonEntity entity, Vec3 anchor) {
        Vec3 prev = lastAnchorPos;
        lastAnchorPos = anchor;
        if (prev == null) {
            return;
        }
        double dx = anchor.x - prev.x;
        double dz = anchor.z - prev.z;
        if (dx * dx + dz * dz < 1.0e-6) {
            return;
        }
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float yaw = Mth.approachDegrees(entity.getYRot(), targetYaw, 8.0f);
        entity.setYRot(yaw);
        entity.setYBodyRot(yaw);
        entity.setYHeadRot(yaw);
    }

    /**
     * 维持方块与宝可梦之间的绳子：锚点实体钉在方块顶面，客户端由锚点渲染器绘制绳子。
     * 不使用原版拴绳数据（Cobblemon 禁止有主宝可梦拴在非主人实体上，会掉落拴绳物品）。
     * 锚点位置每 tick 刷新（飞行器移动时跟随方块）。
     */
    private void ensureLeash(ServerLevel level, PokemonEntity entity) {
        GrabberAnchorEntity anchor = null;
        if (anchorId != null && level.getEntity(anchorId) instanceof GrabberAnchorEntity found && found.isAlive()) {
            anchor = found;
        }
        Vec3 pos = worldAnchor(0.0);
        if (anchor == null) {
            anchor = new GrabberAnchorEntity(ModRegistry.GRABBER_ANCHOR.get(), level);
            anchor.setPos(pos.x, pos.y, pos.z);
            level.addFreshEntity(anchor);
            anchorId = anchor.getUUID();
            setChanged();
        } else {
            anchor.setPos(pos.x, pos.y, pos.z);
        }
        anchor.keepAlive();
        anchor.setTargetId(entity.getId());
    }

    /** 释放宝可梦；recall 为 true 时收回精灵球。 */
    public void release(boolean recall) {
        if (level instanceof ServerLevel serverLevel) {
            PokemonEntity entity = resolveEntity(serverLevel);
            if (entity != null) {
                entity.setNoAi(false);
                entity.setNoGravity(false);
                entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false);
                if (recall) {
                    if (entity.getOwner() != null) {
                        entity.recallWithAnimation();
                    } else {
                        entity.getPokemon().recall();
                    }
                }
            }
        }
        clearAssignment();
    }

    private void clearAssignment() {
        if (anchorId != null && level instanceof ServerLevel serverLevel
                && serverLevel.getEntity(anchorId) instanceof GrabberAnchorEntity anchor) {
            anchor.discard();
        }
        ownerId = null;
        pokemonId = null;
        entityId = null;
        anchorId = null;
        missingTicks = 0;
        liftUnits = 0.0;
        setChanged();
    }

    @Nullable
    private PokemonEntity resolveEntity(ServerLevel level) {
        if (entityId == null || pokemonId == null) {
            return null;
        }
        Entity entity = level.getEntity(entityId);
        if (entity instanceof PokemonEntity pokemonEntity
                && pokemonEntity.isAlive()
                && pokemonId.equals(pokemonEntity.getPokemon().getUuid())) {
            return pokemonEntity;
        }
        return null;
    }

    /** 宝可梦锚定点：方块顶面中心（若在飞行器上则投影为世界坐标）。 */
    private Vec3 worldAnchor(double extraHeight) {
        Vec3 local = new Vec3(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + TOP_Y + extraHeight,
                worldPosition.getZ() + 0.5
        );
        return level == null ? local : GrabberPlatform.projectToWorld(level, local);
    }

    /** 判断该宝可梦能否提供升力：飞行系 / 漂浮特性 / 学会飞翔技能 / 行为标记会飞（各项可配置）。 */
    private static boolean isLifter(Pokemon pokemon) {
        GrabberConfig cfg = GrabberConfig.get();
        if (cfg.anyPokemonCanLift) {
            return true;
        }
        if (cfg.flyingTypeLifts) {
            for (ElementalType type : pokemon.getTypes()) {
                if (type == ElementalTypes.INSTANCE.getFLYING()) {
                    return true;
                }
            }
        }
        if (cfg.levitateAbilityLifts && "levitate".equalsIgnoreCase(pokemon.getAbility().getName())) {
            return true;
        }
        if (cfg.flyMoveLifts) {
            for (Move move : pokemon.getMoveSet()) {
                if ("fly".equalsIgnoreCase(move.getName())) {
                    return true;
                }
            }
        }
        return cfg.canFlyBehaviourLifts && pokemon.getForm().getBehaviour().getMoving().getFly().getCanFly();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
        }
        if (pokemonId != null) {
            tag.putUUID("PokemonId", pokemonId);
        }
        if (entityId != null) {
            tag.putUUID("EntityId", entityId);
        }
        if (anchorId != null) {
            tag.putUUID("AnchorId", anchorId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        pokemonId = tag.hasUUID("PokemonId") ? tag.getUUID("PokemonId") : null;
        entityId = tag.hasUUID("EntityId") ? tag.getUUID("EntityId") : null;
        anchorId = tag.hasUUID("AnchorId") ? tag.getUUID("AnchorId") : null;
        if (tag.contains("DisplayName")) {
            displayPokemonName = tag.getString("DisplayName");
            displaySpeed = tag.getInt("DisplaySpeed");
            displayLifter = tag.getBoolean("DisplayLifter");
            displayPower = tag.getInt("DisplayPower");
            displayThrottlePercent = tag.getInt("DisplayThrottle");
            liftUnits = tag.getDouble("DisplayLift");
        }
    }

    // ==== 客户端同步（护目镜显示） ====

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("DisplayName", displayPokemonName);
        tag.putInt("DisplaySpeed", displaySpeed);
        tag.putBoolean("DisplayLifter", displayLifter);
        tag.putInt("DisplayPower", displayPower);
        tag.putInt("DisplayThrottle", displayThrottlePercent);
        tag.putDouble("DisplayLift", liftUnits);
        return tag;
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
