package com.sing.warpcommands.network;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import java.util.Map;

public class ClientCache {

    private static Map<String, RegistryKey<World>> CACHE = null;

    public static Map<String, RegistryKey<World>> get() {
        return CACHE;
    }

    public static void update(Map<String, RegistryKey<World>> value) {
        CACHE = value;
    }
}
