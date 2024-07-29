package com.sing.warpcommands;

import com.sing.simple_warp_commands.Tags;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID)
public class Configure {
    @Config.RequiresWorldRestart
    @Config.Comment("Enable the /warp command set")
    public static boolean enableWarpCommand = true;
    @Config.RequiresWorldRestart
    @Config.Comment("Enable the /home command set")
    public static boolean enableHomeCommand = true;
}
