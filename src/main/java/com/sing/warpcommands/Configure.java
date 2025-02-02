package com.sing.warpcommands;

import com.google.common.collect.Maps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;


@Mod.EventBusSubscriber(modid = "simple_warp_commands", bus = Mod.EventBusSubscriber.Bus.MOD)
public class Configure {
    public static ForgeConfigSpec.BooleanValue enableWarpCommand;
    public static ForgeConfigSpec.BooleanValue enableHomeCommand;
    public static ForgeConfigSpec.BooleanValue enableBackCommand;
    public static ForgeConfigSpec.BooleanValue enableSpawnCommand;
    public static ForgeConfigSpec.BooleanValue enableTpPlayerCommand;
    public static ForgeConfigSpec.BooleanValue enablePosCommand;
    public static ForgeConfigSpec.BooleanValue enableByeCommand;

    public static ForgeConfigSpec.BooleanValue doBackRecordDeath;

    public static ForgeConfigSpec.BooleanValue dimensionIndependentWaypoints;

    public static ForgeConfigSpec.BooleanValue showCoordinates;

    public static ForgeConfigSpec.IntValue permissionRequired;

    public static ForgeConfigSpec.BooleanValue enableRiding;

    public static ForgeConfigSpec.BooleanValue noiseCommandUse;

    public static final ForgeConfigSpec SPEC;

    private static ForgeConfigSpec.BooleanValue doEnableCommand(ForgeConfigSpec.Builder builder, String name) {
        return builder.comment("Enable the /" + name + " command").define("enable" + Character.toUpperCase(name.charAt(0)) + name.substring(1) + "Command", true);
    }

    public enum FilterMode {
        BLACKLIST,
        WHITELIST
    }

    private static class TeleportMatch {
        private final Pattern from;
        private final Pattern to;

        public TeleportMatch(Pattern from, Pattern to) {
            this.from = from;
            this.to = to;
        }

        public boolean matches(ResourceLocation from, ResourceLocation to) {
            return this.from.matcher(from.toString()).matches() && this.to.matcher(to.toString()).matches();
        }
    }

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> dimensionCrossAllowedList;
    private static final List<TeleportMatch> conditions = new ArrayList<>();
    private static final Set<Map.Entry<@Nullable RegistryKey<World>, @Nullable RegistryKey<World>>> dimensionCrossAllowed = new HashSet<>();
    private static final Set<Map.Entry<@Nullable RegistryKey<World>, @Nullable RegistryKey<World>>> dimensionCrossDisallowedCache = new HashSet<>();
    private static final ForgeConfigSpec.EnumValue<FilterMode> dimensionFilterMode;

    private static boolean matchDimId(RegistryKey<World> from, RegistryKey<World> to) {
        return from.equals(to) ||
                !dimensionCrossDisallowedCache.contains(Maps.immutableEntry(from, to)) && (
                        dimensionCrossAllowed.contains(Maps.immutableEntry(from, to)) ||
                                dimensionCrossAllowed.contains(Maps.immutableEntry(from, null)) ||
                                dimensionCrossAllowed.contains(Maps.immutableEntry(null, to)));
    }

    public static boolean couldTeleportTo(RegistryKey<World> from, RegistryKey<World> to) {
        boolean matched = matchDimId(from, to);
        if (!matched) {
            if (conditions.stream().anyMatch(match ->
                    match.matches(from.location(), to.location())
            )) {
                matched = true;
                dimensionCrossAllowed.add(Maps.immutableEntry(from, to));
            } else dimensionCrossDisallowedCache.add(Maps.immutableEntry(from, to));
        }

        return dimensionFilterMode.get().equals(FilterMode.WHITELIST) == matched;
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("commands");
        enableWarpCommand = builder.comment("Enable the /warp command,and also /warp. /warp+ etc.").define("enableWarpCommand", true);
        enableHomeCommand = doEnableCommand(builder, "home");
        enableBackCommand = doEnableCommand(builder, "back");
        enableTpPlayerCommand = builder.comment("Enable the /tpp(tp player) command").define("enableTpPlayerCommand", true);

        enableSpawnCommand = doEnableCommand(builder, "spawn");
        enableByeCommand = doEnableCommand(builder, "bye");
        enablePosCommand = doEnableCommand(builder, "pos");
        builder.pop();
        builder.push("features");


        dimensionIndependentWaypoints = builder.comment("Ensures that waypoints are non-interconnected across different dimensions, meaning the /warp command cannot teleport between dimensions").define("areWarpsDimensionsIndependent", false);
        showCoordinates = builder.comment("Show the coordinates of waypoinys when using /warp.").define("showCoord", true);
        permissionRequired = builder.comment("Set the minimum permission required to execute the commands").defineInRange("permissionRequired", 0, 0, 4);
        enableRiding = builder.comment("Teleport the player and entities the players riding together").define("enableRiding", false);
        doBackRecordDeath = builder.comment("Should the /back command records last death location of the player").define("doBackRecordDeath", true);
        noiseCommandUse = builder.comment("Boradcast command messages to all players").define("noiseCommandUse", false);

        dimensionCrossAllowedList = builder.comment("Control which dimension could teleport to and from.")
                .comment("Syntax(may be empty for any dimension, or regex patterns when starts with !)")
                .comment("from -> to")
                .comment("e.g.")
                .comment("!minecraft:.* -> minecraft:the_end #will allow all vanilla dimensions teleport to the end dimension")
                .defineList("dimensionCrossAllowed", new ArrayList<>(), o -> o instanceof String);
        dimensionFilterMode = builder.comment("Set whether dimensionCrossAllowed is a blacklist or not").defineEnum("dimensionFilterMode", FilterMode.BLACKLIST);

        SPEC = builder.build();
    }

    private static void update() {
        for (String s : dimensionCrossAllowedList.get()) {
            final int index = s.indexOf("->");
            if (index == -1)
                throw new IllegalStateException("Configure invalid: requires a '->' between dimension source and target");
            String from = s.substring(0, index).trim();
            String to = s.substring(index + 2).trim();
            if (from.startsWith("!") || to.startsWith("!")) {
                if (from.startsWith("!")) from = from.substring(1);
                if (to.startsWith("!")) to = to.substring(1);
                conditions.add(new TeleportMatch(
                        Pattern.compile(from),
                        Pattern.compile(to)
                ));
            } else {
                dimensionCrossAllowed.add(
                        Maps.immutableEntry(
                                from.isEmpty() ? null : RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(from)),
                                to.isEmpty() ? null : RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(to))
                        )
                );
            }

        }
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfig.Loading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        update();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfig.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        update();
    }
}
