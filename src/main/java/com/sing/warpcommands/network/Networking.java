package com.sing.warpcommands.network;

import com.sing.warpcommands.WarpCommandsMod;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    private static final String VERSION = "0.1";
    private static int id = 0;

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(WarpCommandsMod.id("waypoints_sync"), () -> VERSION, version -> version.equals(VERSION), str -> true);
        INSTANCE.messageBuilder(UpdateWaypointPack.class, id++).encoder(UpdateWaypointPack::toBytes).decoder(UpdateWaypointPack::new).consumer(UpdateWaypointPack::handle).add();
    }
}
