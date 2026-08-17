package xiaocaoawa.minecraft.mod.cobbleupdraft.lock;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xiaocaoawa.minecraft.mod.cobbleupdraft.config.GrabberConfig;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 宝可梦抓手的锁定管理：被放到抓手上的宝可梦仍留在玩家队伍中，
 * 但被锁定——不能放出、不能收回展示实体、不能放生、不能交易、不能参战（对战中也就无法切换上场）。
 * 锁表在内存中，由抓手方块实体每 tick 维护（存档重载后随方块加载自动恢复）。
 */
public final class GrabberLockManager {
    /** 被抓手锁定的宝可梦 UUID（禁止放出/收回/放生/交易/参战）。 */
    private static final Set<UUID> LOCKED = ConcurrentHashMap.newKeySet();
    /**
     * 当前正被抓手固定的展示实体 UUID（常驻世界：随区块保存、免疫 Cobblemon 的主动丢弃）。
     * 按实体而非宝可梦记录，这样飞船飞走后旧区块里遗留的重复实体不会被保护，会被正常清理。
     */
    private static final Set<UUID> GRABBED_ENTITIES = ConcurrentHashMap.newKeySet();

    private GrabberLockManager() {
    }

    public static void lock(UUID pokemonId) {
        LOCKED.add(pokemonId);
    }

    public static void unlock(UUID pokemonId) {
        LOCKED.remove(pokemonId);
    }

    public static boolean isLocked(UUID pokemonId) {
        return LOCKED.contains(pokemonId);
    }

    public static void markGrabbedEntity(UUID entityId) {
        GRABBED_ENTITIES.add(entityId);
    }

    public static void clearGrabbedEntity(UUID entityId) {
        GRABBED_ENTITIES.remove(entityId);
    }

    /** 该实体是否为抓手当前固定的展示实体（供 mixin 判断是否常驻世界）。 */
    public static boolean isGrabbedEntity(UUID entityId) {
        return GRABBED_ENTITIES.contains(entityId);
    }

    /** 该实体是否应取消碰撞箱（被抓手固定且配置开启）。 */
    public static boolean isCollisionDisabled(UUID entityId) {
        return GrabberConfig.get().grabbedPokemonNoCollision && GRABBED_ENTITIES.contains(entityId);
    }

    public static void init() {
        // 禁止放出（快捷键/精灵球）
        CobblemonEvents.POKEMON_SENT_PRE.subscribe(Priority.HIGH, event -> {
            if (isLocked(event.getPokemon().getUuid())) {
                event.cancel();
                notifyOwner(event.getPokemon());
            }
            return Unit.INSTANCE;
        });
        // 禁止收回抓手上的展示实体
        CobblemonEvents.POKEMON_RECALL_PRE.subscribe(Priority.HIGH, event -> {
            if (isLocked(event.getPokemon().getUuid())) {
                event.cancel();
                notifyOwner(event.getPokemon());
            }
            return Unit.INSTANCE;
        });
        // 禁止放生
        CobblemonEvents.POKEMON_RELEASED_EVENT_PRE.subscribe(Priority.HIGH, event -> {
            if (isLocked(event.getPokemon().getUuid())) {
                event.cancel();
                event.getPlayer().displayClientMessage(
                        Component.translatable("message.cobbleupdraft.pokemon_grabber.locked"), true);
            }
            return Unit.INSTANCE;
        });
        // 禁止交易
        CobblemonEvents.TRADE_EVENT_PRE.subscribe(Priority.HIGH, event -> {
            if (isLocked(event.getTradeParticipant1Pokemon().getUuid())
                    || isLocked(event.getTradeParticipant2Pokemon().getUuid())) {
                event.cancel();
            }
            return Unit.INSTANCE;
        });
        // 对战：锁定的宝可梦从参战队伍移除（对战照常，但它无法上场/切换）；
        // 若某一方因此没有可战宝可梦，则取消这场对战
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.HIGH, event -> {
            for (BattleActor actor : event.getBattle().getActors()) {
                List<BattlePokemon> list = actor.getPokemonList();
                boolean removed = list.removeIf(bp -> isLocked(bp.getOriginalPokemon().getUuid()));
                if (removed && list.isEmpty()) {
                    event.cancel();
                    event.setReason(Component.translatable("message.cobbleupdraft.pokemon_grabber.locked_battle"));
                }
            }
            return Unit.INSTANCE;
        });
    }

    /** 向宝可梦主人提示"被抓手固定"。 */
    public static void notifyLocked(Pokemon pokemon) {
        notifyOwner(pokemon);
    }

    private static void notifyOwner(Pokemon pokemon) {
        ServerPlayer owner = pokemon.getOwnerPlayer();
        if (owner != null) {
            owner.displayClientMessage(
                    Component.translatable("message.cobbleupdraft.pokemon_grabber.locked"), true);
        }
    }
}
