package xiaocaoawa.minecraft.mod.cobbleupdraft.neoforge.compat;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Locale;

/**
 * 在 Sable 升力集成之上附加机械动力护目镜信息：
 * 戴护目镜看方块时显示当前宝可梦、速度、红石油门与实时升力。
 * 仅当 create 与 sable 同时存在时才会实例化本类。
 */
public class GoggleSablePokemonGrabberBlockEntity extends SablePokemonGrabberBlockEntity implements IHaveGoggleInformation {

    public GoggleSablePokemonGrabberBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.header").withStyle(ChatFormatting.WHITE));

        String name = getDisplayPokemonName();
        if (name.isEmpty()) {
            tooltip.add(indent(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.no_pokemon")
                    .withStyle(ChatFormatting.GRAY)));
            return true;
        }

        tooltip.add(indent(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.pokemon",
                        Component.literal(name).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY)));
        tooltip.add(indent(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.speed",
                        Component.literal(String.valueOf(getDisplaySpeed())).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY)));
        tooltip.add(indent(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.stat_total",
                        Component.literal(String.valueOf(getDisplayStatTotal())).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY)));

        if (!isDisplayLifter()) {
            tooltip.add(indent(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.not_lifter")
                    .withStyle(ChatFormatting.RED)));
            return true;
        }

        // 油门条：15 格对应红石信号 0~15
        int power = Math.max(0, Math.min(15, getDisplayPower()));
        tooltip.add(indent(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.throttle",
                        bar(power, 15),
                        Component.literal(getDisplayThrottlePercent() + "%").withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY)));

        // 填充条：气囊充气量（当前升力 / 容量），复刻热气球面板
        double capacity = getDisplayCapacity();
        if (capacity > 0.0) {
            int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, getLiftUnits() / capacity)) * 20.0);
            tooltip.add(indent(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.fill",
                            bar(filled, 20))
                    .withStyle(ChatFormatting.GRAY)));
        }

        tooltip.add(indent(Component.translatable("tooltip.cobbleupdraft.pokemon_grabber.lift",
                        Component.literal(String.format(Locale.ROOT, "%.2f kpg", getLiftUnits()))
                                .withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY)));
        return true;
    }

    private static MutableComponent bar(int filled, int total) {
        MutableComponent bar = Component.empty();
        bar.append(Component.literal("|".repeat(filled)).withStyle(ChatFormatting.AQUA));
        bar.append(Component.literal("|".repeat(total - filled)).withStyle(ChatFormatting.DARK_GRAY));
        return bar;
    }

    private static MutableComponent indent(Component component) {
        return Component.literal("    ").append(component);
    }
}
