package com.sing.warpcommands.data;

import com.sing.warpcommands.utils.EntityPos;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class WorldDataWaypoints extends WorldSavedData {
    public final Object2ObjectMap<String, EntityPos> wayPoints = new Object2ObjectAVLTreeMap<>();

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
            NBTTagCompound compound = (NBTTagCompound) list.get(i);
            EntityPos p = EntityPos.fromNBT(compound);
            wayPoints.put(compound.getString("name"), p);
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
        NBTTagList wayPoints = new NBTTagList();
        for (Object2ObjectMap.Entry<String, EntityPos> p : this.wayPoints.object2ObjectEntrySet()) {
            NBTTagCompound compound = p.getValue().serializeNBT();
            compound.setString("name", p.getKey());
            wayPoints.appendTag(compound);
        }
        nbt.setTag("WayPoints", wayPoints);
        return nbt;
    }

    @Nullable
    public EntityPos get(String name) {
        return this.wayPoints.get(name);
    }

    public void set(String name, EntityPos pos) {
        this.wayPoints.put(name, pos);
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
