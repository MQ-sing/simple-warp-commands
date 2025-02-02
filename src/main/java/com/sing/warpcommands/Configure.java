package com.sing.warpcommands;

import com.sing.simple_warp_commands.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Config(modid = Tags.MOD_ID)
@Mod.EventBusSubscriber
public class Configure {
    @Config.RequiresWorldRestart
    @Config.Comment("Enable the /warp command set")
    public static boolean enableWarpCommand = true;
    @Config.RequiresWorldRestart
    @Config.Comment("Enable the /home command set")
    public static boolean enableHomeCommand = true;

    @Config.RequiresWorldRestart
    @Config.Comment("Enable the /back command")
    public static boolean enableBackCommand = true;
    @Config.RequiresWorldRestart
    @Config.Comment("Do /back command records the last death point")
    public static boolean enableBackRecordDeath = true;
    @Config.RequiresWorldRestart
    @Config.Comment("Enable /spawn and /setspawn command")
    public static boolean enableSpawnCommand = true;
    @Config.RequiresWorldRestart
    @Config.Comment("Enable /tpp command")
    public static boolean enableTpPlayerCommand = true;
    @Config.RequiresWorldRestart
    @Config.Comment("Enable /pos command")
    public static boolean enablePosCommand = true;
    @Config.RequiresWorldRestart
    @Config.Comment("Enable /bye command")
    public static boolean enableByeCommand = true;

    @Config.Comment("Allow cross-dimension teleporting")
    public static boolean allowDimensionCross = true;

    @Config.RequiresWorldRestart
    @Config.Comment("Ensures that warp points are non-interconnected across different dimensions, meaning the /warp command cannot teleport between dimensions")
    public static boolean areWarpsDimensionsIndependent = false;
    @Config.Comment("Shows coordinates of waypoints when using /warpopt")
    public static boolean showCoordinates = true;

    @Config.Comment("Set if the commands require op permissions to execute")
    public static boolean requireOp = false;

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

    @Config.Comment({"Control which dimension could teleport to and from.",
            "Syntax(may be empty for any dimension)",
            "from -> to",
            "e.g.",
            "-1 -> 1 #will allow teleporting from nether to the end dimension"})
    private static String[] dimensionCrossAllowedList = new String[0];

    private static class IntIntEntry implements Map.Entry<Integer, Integer> {
        int key;
        int value;

        @Override
        public Integer getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(Integer value) {
            return this.value = value;
        }

        public IntIntEntry(int value, int key) {
            this.value = value;
            this.key = key;
        }
    }

    @Config.Ignore
    private static final Set<IntIntEntry> dimensionCrossAllowed = new HashSet<>();
    @Config.Ignore
    private static final Set<IntIntEntry> dimensionCrossDisallowedCache = new HashSet<>();
    @Config.Comment({"Set whether dimensionFilterMode is a blacklist or not",
            "Allowed values: BLACKLIST,WHITELIST"})
    @Config.Name("dimensionFilterMode")
    private static String dimensionFilterModeString = "BLACKLIST";
    @Config.Ignore
    private static FilterMode dimensionFilterMode;

    private static boolean matchDimId(int from, int to) {
        //TODO
        return !dimensionCrossDisallowedCache.contains(new IntIntEntry(from, to)) && (
                dimensionCrossAllowed.contains(new IntIntEntry(from, to)) ||
                        dimensionCrossAllowed.contains(new IntIntEntry(from, Integer.MAX_VALUE)) ||
                        dimensionCrossAllowed.contains(new IntIntEntry(Integer.MAX_VALUE, to)));
    }

    public static boolean couldTeleportTo(int from, int to) {
        boolean matched = matchDimId(from, to);
        return from == to || dimensionFilterMode.equals(FilterMode.WHITELIST) == matched;
    }

    public static void update() {
        dimensionFilterMode = dimensionFilterModeString.equals("BLACKLIST") ? FilterMode.BLACKLIST :
                dimensionFilterModeString.equals("WHITELIST") ? FilterMode.WHITELIST : null;
        if (dimensionFilterMode == null)
            throw new IllegalStateException("Expected 'BLACKLIST' or 'WHITELIST'");
        for (String s : dimensionCrossAllowedList) {
            final int index = s.indexOf("->");
            if (index == -1)
                throw new IllegalStateException("Configure invalid: requires a '->' between dimension source and target");
            String from = s.substring(0, index).trim();
            String to = s.substring(index + 2).trim();
            dimensionCrossAllowed.add(
                    new IntIntEntry(
                            from.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(from),
                            to.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(to)
                    )
            );

        }
    }

    @SubscribeEvent
    public static void onConfigurationChanged(ConfigChangedEvent e) {
        if (!Objects.equals(e.getModID(), Tags.MOD_ID)) return;
        ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
        update();
    }
}
