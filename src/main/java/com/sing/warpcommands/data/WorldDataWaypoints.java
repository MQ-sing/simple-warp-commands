package com.sing.warpcommands.data;

import com.sing.warpcommands.utils.WayPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldDataWaypoints extends WorldSavedData {
    public final HashMap<String, WayPoint> wayPoints = new HashMap<>();

    public WorldDataWaypoints(String name) {
        super(name);
    }

    public static WorldDataWaypoints get(World world) {
        WorldSavedData data = world.loadData(WorldDataWaypoints.class, "WayPoints");
        if (data == null) {
            data = new WorldDataWaypoints("WayPoints");
            world.setData("WayPoints", data);
        }
        return (WorldDataWaypoints) data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        wayPoints.clear();
        NBTTagList list = (NBTTagList) nbt.getTag("WayPoints");
        for (int i = 0; i < list.tagCount(); ++i) {
            WayPoint p = WayPoint.fromNBT((NBTTagCompound) list.get(i));
            wayPoints.put(p.name, p);
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
        NBTTagList wayPoints = new NBTTagList();
        for (WayPoint p : this.wayPoints.values()) {
            wayPoints.appendTag(p.serializeNBT());
        }
        nbt.setTag("WayPoints", wayPoints);
        return nbt;
    }

    @Nullable
    public WayPoint get(String name) {
        return this.wayPoints.get(name);
    }

    public void set(WayPoint wayPoint) {
        this.wayPoints.put(wayPoint.name, wayPoint);
        this.markDirty();
    }

    public boolean has(String name) {
        return this.wayPoints.containsKey(name);
    }

    public void remove(String name) {
        this.wayPoints.remove(name);
        this.markDirty();
    }
}
