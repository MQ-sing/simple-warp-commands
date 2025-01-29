package com.sing.warpcommands;

import com.sing.simple_warp_commands.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

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

    @SubscribeEvent
    public static void onConfigurationChanged(ConfigChangedEvent e) {
        if (!Objects.equals(e.getModID(), Tags.MOD_ID)) return;
        ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
    }
}
