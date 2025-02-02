package com.sing.warpcommands.network;

import com.sing.warpcommands.data.WorldDataWaypoints;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UpdateWaypointPack {
    private final Map<String, RegistryKey<World>> waypoints;

    public UpdateWaypointPack(Map<String, RegistryKey<World>> waypoints) {
        this.waypoints = waypoints;
    }

    public UpdateWaypointPack(PacketBuffer buf) {
        final HashMap<String, RegistryKey<World>> waypoints = new HashMap<>();
        int size = buf.readInt();
        while (size-- > 0) {
            final String str = buf.readUtf();
            waypoints.put(str, RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf())));
        }
        this.waypoints = Collections.unmodifiableMap(waypoints);
    }

    public static UpdateWaypointPack of(WorldDataWaypoints.IWaypointList data) {
        return new UpdateWaypointPack(data.entries().stream().collect(Collectors.toMap(Map.Entry<String, EntityPos>::getKey, i -> i.getValue().dim)));
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(this.waypoints.size());
        for (Map.Entry<String, RegistryKey<World>> name : waypoints.entrySet()) {
            buf.writeUtf(name.getKey());
            buf.writeUtf(name.getValue().location().toString());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientCache.update(Collections.unmodifiableMap(waypoints)));
        ctx.get().setPacketHandled(true);
    }
}