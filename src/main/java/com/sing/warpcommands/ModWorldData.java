package com.sing.warpcommands;

import com.sing.warpcommands.utils.WayPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModWorldData extends WorldSavedData {
    public final HashMap<String, WayPoint> wayPoints = new HashMap<>();
    public final HashMap<UUID, WayPoint> homePoints = new HashMap<>();

    public ModWorldData(String name) {
        super(name);
    }

    public static ModWorldData get(World world) {
        WorldSavedData data = world.loadData(ModWorldData.class, "WayPoints");
        if (data == null) {
            data = new ModWorldData("WayPoints");
            world.setData("WayPoints", data);
        }
        return (ModWorldData) data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        wayPoints.clear();
        NBTTagList list = (NBTTagList) nbt.getTag("WayPoints");
        for (int i = 0; i < list.tagCount(); ++i) {
            WayPoint p = new WayPoint((NBTTagCompound) list.get(i));
            wayPoints.put(p.name, p);
        }
        NBTTagList homes = (NBTTagList) nbt.getTag("Homes");
        for (int i = 0; i < homes.tagCount(); ++i) {
            NBTTagCompound x = (NBTTagCompound) homes.get(i);
            homePoints.put(x.getUniqueId("uuid"), new WayPoint((NBTTagCompound) x.getTag("point")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList wayPoints = new NBTTagList();
        for (WayPoint p : this.wayPoints.values()) {
            wayPoints.appendTag(p.serializeNBT());
        }
        nbt.setTag("WayPoints", wayPoints);
        NBTTagList homes = new NBTTagList();
        for (Map.Entry<UUID, WayPoint> p : homePoints.entrySet()) {
            NBTTagCompound x = new NBTTagCompound();
            x.setUniqueId("uuid", p.getKey());
            x.setTag("point", p.getValue().serializeNBT());
            homes.appendTag(x);
        }

        nbt.setTag("Homes", homes);
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

    @Nullable
    public WayPoint getHome(EntityPlayerMP player) {
        WayPoint home = this.homePoints.get(player.getUniqueID());
        if (home == null) {

            BlockPos pos = player.getBedLocation(0);

            if (pos == null) return null;
            home = new WayPoint("", pos.getX(), pos.getY(), pos.getZ(), 0, 90, 0);
            this.homePoints.put(player.getUniqueID(), home);
        }
        return home;
    }

    public void setHome(EntityPlayerMP player) {
        this.homePoints.put(player.getUniqueID(), new WayPoint("", player));
        this.markDirty();
    }

    public void remove(String name) {
        this.wayPoints.remove(name);
        this.markDirty();
    }
}
