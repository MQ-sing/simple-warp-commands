package com.sing.warpcommands;

import com.sing.simple_warp_commands.Tags;
import com.sing.warpcommands.commands.CommandHome;
import com.sing.warpcommands.commands.CommandWarp;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class WarpCommandsMod {
    @Mod.EventHandler
    void serverInit(FMLServerStartingEvent e) {
        if (Configure.enableWarpCommand) CommandWarp.init(e);
        if (Configure.enableHomeCommand) CommandHome.init(e);
    }
}
