package xiaocaoawa.minecraft.mod.cobbleupdraft.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import xiaocaoawa.minecraft.mod.cobbleupdraft.CobbleUpdraft;
import xiaocaoawa.minecraft.mod.cobbleupdraft.block.PokemonGrabberBlock;
import xiaocaoawa.minecraft.mod.cobbleupdraft.block.PokemonGrabberBlockEntity;
import xiaocaoawa.minecraft.mod.cobbleupdraft.entity.GrabberAnchorEntity;
import xiaocaoawa.minecraft.mod.cobbleupdraft.platform.GrabberPlatform;

public final class ModRegistry {
    private ModRegistry() {
    }

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(CobbleUpdraft.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(CobbleUpdraft.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(CobbleUpdraft.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(CobbleUpdraft.MOD_ID, Registries.CREATIVE_MODE_TAB);

    /** 模组独立的创造物品栏。 */
    public static final RegistrySupplier<CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            () -> CreativeTabRegistry.create(
                    Component.translatable("itemGroup.cobbleupdraft.main"),
                    () -> new ItemStack(ModRegistry.POKEMON_GRABBER_ITEM.get())));

    public static final RegistrySupplier<Block> POKEMON_GRABBER = BLOCKS.register("pokemon_grabber",
            () -> new PokemonGrabberBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.8f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistrySupplier<Item> POKEMON_GRABBER_ITEM = ITEMS.register("pokemon_grabber",
            () -> new BlockItem(POKEMON_GRABBER.get(), new Item.Properties().arch$tab(MAIN_TAB)));

    public static final RegistrySupplier<BlockEntityType<PokemonGrabberBlockEntity>> POKEMON_GRABBER_BE =
            BLOCK_ENTITIES.register("pokemon_grabber",
                    () -> BlockEntityType.Builder.of(GrabberPlatform::createBlockEntity, POKEMON_GRABBER.get()).build(null));

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(CobbleUpdraft.MOD_ID, Registries.ENTITY_TYPE);

    /** 隐形拴绳锚点实体（宝可梦拴在方块上的绳子另一端）。 */
    public static final RegistrySupplier<EntityType<GrabberAnchorEntity>> GRABBER_ANCHOR =
            ENTITY_TYPES.register("grabber_anchor",
                    () -> EntityType.Builder.<GrabberAnchorEntity>of(GrabberAnchorEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(10)
                            .build("grabber_anchor"));

    public static void init() {
        CREATIVE_TABS.register();
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITIES.register();
        ENTITY_TYPES.register();
    }
}
