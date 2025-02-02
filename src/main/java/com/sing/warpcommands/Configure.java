package com.sing.warpcommands;

import com.sing.simple_warp_commands.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Config(modid = Tags.MOD_ID)
@Mod.EventBusSubscriber
public class Configure {

    public static class CommandsToggle {
        @Config.RequiresWorldRestart
        @Config.Comment("Enable the warp command family(/warp,/setwarp,/delwarp and /warps)")
        public boolean enableWarpCommand = true;
        @Config.RequiresWorldRestart
        @Config.Comment("Enable the /home command family(/home and /sethome)")
        public boolean enableHomeCommand = true;

        @Config.RequiresWorldRestart
        @Config.Comment("Enable the /back command")
        public boolean enableBackCommand = true;
        @Config.RequiresWorldRestart
        @Config.Comment("Enable /spawn and /setspawn command")
        public boolean enableSpawnCommand = true;
        @Config.RequiresWorldRestart
        @Config.Comment("Enable /tpp command")
        public boolean enableTpPlayerCommand = true;
        @Config.RequiresWorldRestart
        @Config.Comment("Enable /pos command")
        public boolean enablePosCommand = true;
        @Config.RequiresWorldRestart
        @Config.Comment("Enable /bye command")
        public boolean enableByeCommand = true;
    }

    public static CommandsToggle commands = new CommandsToggle();

    @Config.RequiresWorldRestart
    @Config.Comment("Record death locations for /back command")
    public static boolean doBackRecordDeath = true;
    @Config.RequiresWorldRestart
    @Config.Comment({"Whether warps are dimension-locked",
            "true: Warps can ONLY be used within their original dimension",
            "false: Warps work across dimensions (when allowed by crossDimensionFilterMode)"})
    public static boolean warpsDimensionLocked = false;
    @Config.Comment("Display coordinates of waypoints when using /warps")
    public static boolean showWaypointCoords = true;

    @Config.Comment("Require OP permissions to use commands")
    public static boolean requireOp = false;

    public enum FilterMode {
        BLACKLIST,
        WHITELIST
    }

    @Config.Comment({"Control which dimension could teleport to and from.",
            "Syntax(may be empty for any dimension)",
            "from -> to",
            "e.g.",
            "\"-1 -> 1\", from nether(-1) to the end(1)"})
    @Config.Name("crossDimensionRules")
    public static String[] dimensionCrossAllowedList = new String[0];
    @Config.Comment("Filtering mode for crossDimensionRules")
    public static FilterMode crossDimensionFilterMode = FilterMode.BLACKLIST;

    public static class IntIntEntry implements Map.Entry<Integer, Integer> {
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

        public IntIntEntry(int key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IntIntEntry)) return false;
            IntIntEntry that = (IntIntEntry) o;
            return key == that.key && value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

    @Config.Ignore
    public static final Set<IntIntEntry> crossDimensionRules = new HashSet<>();

    private static boolean matchDimId(int from, int to) {
        return crossDimensionRules.contains(new IntIntEntry(from, to)) ||
                crossDimensionRules.contains(new IntIntEntry(from, Integer.MAX_VALUE)) ||
                crossDimensionRules.contains(new IntIntEntry(Integer.MAX_VALUE, to));
    }

    public static boolean couldTeleportTo(int from, int to) {
        return from == to || crossDimensionFilterMode.equals(FilterMode.WHITELIST) == matchDimId(from, to);
    }

    public static void update() {
        for (String s : dimensionCrossAllowedList) {
            final int index = s.indexOf("->");
            if (index == -1)
                throw new IllegalStateException("Configure invalid: requires a '->' between dimension source and target");
            String from = s.substring(0, index).trim();
            String to = s.substring(index + 2).trim();
            crossDimensionRules.add(
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
