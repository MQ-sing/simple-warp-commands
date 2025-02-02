package com.sing.warpcommands.data;

import com.google.common.collect.Maps;
import com.sing.warpcommands.Configure;
import com.sing.warpcommands.utils.EntityPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldDataWaypoints extends WorldSavedData {

    public WorldDataWaypoints(String name) {
        super(name);
        updateConfigure();
    }

    private IWaypointsStorage storage;

    public static IWaypointList get(World world) {
        WorldDataWaypoints data = (WorldDataWaypoints) world.loadData(WorldDataWaypoints.class, "WayPoints");
        if (data == null) {
            data = new WorldDataWaypoints("WayPoints");
            world.setData("WayPoints", data);
        }
        return data.storage.get(world);
    }

    public void updateConfigure() {
        storage = Configure.areWarpsDimensionsIndependent ? new IndependentWorldWayPoint() : new SharedWorldWayPointStorage();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        storage.deserializeNBT(nbt.getTagList("WayPoints", 10));
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
        nbt.setTag("WayPoints", storage.serializeNBT());
        return nbt;
    }

    public interface IWaypointsStorage extends INBTSerializable<NBTTagList> {
        @Nullable
        IWaypointList get(World world);
    }

    public interface IWaypointList {
        @Nullable
        EntityPos get(String name);

        void set(String name, EntityPos pos);

        boolean has(String name);

        int size();
        @Nullable
        EntityPos remove(String name);

        Set<String> keySet();

        Collection<Map.Entry<String, EntityPos>> entries();

        Collection<EntityPos> values();
    }

    public class SharedWaypointList implements IWaypointList {
        private final HashMap<String, EntityPos> waypoints = new HashMap<>();

        @Override
        @Nullable
        public EntityPos get(String name) {
            return waypoints.get(name);
        }

        @Override
        public void set(String name, EntityPos pos) {
            waypoints.put(name, pos);
            markDirty();
        }

        @Override
        public boolean has(String name) {
            return waypoints.containsKey(name);
        }

        @Override
        public @Nullable EntityPos remove(String name) {
            EntityPos removed = this.waypoints.remove(name);
            if (removed != null) markDirty();
            return removed;
        }

        @Override
        public Set<String> keySet() {
            return this.waypoints.keySet();
        }

        @Override
        public Collection<Map.Entry<String, EntityPos>> entries() {
            return this.waypoints.entrySet();
        }

        @Override
        public Collection<EntityPos> values() {
            return this.waypoints.values();
        }

        @Override
        public int size() {
            return this.waypoints.size();
        }
    }

    private static NBTTagCompound storeEntityPos(String name, EntityPos pos) {
        NBTTagCompound nbt = pos.serializeNBT();
        nbt.setString("name", name);
        return nbt;
    }

    private class SharedWorldWayPointStorage implements IWaypointsStorage {
        SharedWaypointList waypointsMap = new SharedWaypointList();

        @Override
        public SharedWaypointList get(World _world) {
            return waypointsMap;
        }

        @Override
        public NBTTagList serializeNBT() {
            NBTTagList list = new NBTTagList();
            for (Map.Entry<String, EntityPos> entry : this.waypointsMap.entries()) {
                list.appendTag(storeEntityPos(entry.getKey(), entry.getValue()));
            }
            return list;
        }

        @Override
        public void deserializeNBT(NBTTagList nbt) {
            waypointsMap.waypoints.clear();
            for (NBTBase nbtBase : nbt) {
                NBTTagCompound compound = (NBTTagCompound) nbtBase;
                EntityPos pos = EntityPos.fromNBT(compound);
                waypointsMap.waypoints.put(compound.getString("name"), pos);
            }
        }
    }

    private static class IndependentEntityPos {
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;

        public EntityPos get(int dim) {
            return new EntityPos(x, y, z, yaw, pitch, dim);
        }

        public IndependentEntityPos(EntityPos pos) {
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
            this.yaw = pos.yaw;
            this.pitch = pos.pitch;
        }
    }

    private class IndependentWaypoints implements IWaypointList {
        private final int dim;
        private final Object2ObjectMap<String, IndependentEntityPos> waypoints;

        private IndependentWaypoints(int dim, Object2ObjectMap<String, IndependentEntityPos> waypoints) {
            this.dim = dim;
            this.waypoints = waypoints;
        }

        @Nullable
        @Override
        public EntityPos get(String name) {
            final IndependentEntityPos pos = waypoints.get(name);
            if (pos == null) return null;
            return pos.get(dim);
        }

        @Override
        public void set(String name, EntityPos pos) {
            waypoints.put(name, new IndependentEntityPos(pos));
            markDirty();
        }

        @Override
        public boolean has(String name) {
            return waypoints.containsKey(name);
        }

        @Nullable
        @Override
        public EntityPos remove(String name) {
            IndependentEntityPos pos = waypoints.remove(name);
            if (pos != null) {
                markDirty();
                return pos.get(dim);
            }
            return null;
        }

        @Override
        public ObjectSet<String> keySet() {
            return waypoints.keySet();
        }

        @Override
        public Collection<Map.Entry<String, EntityPos>> entries() {
            Stream<Map.Entry<String, EntityPos>> stream = waypoints.entrySet().stream().map(x -> Maps.immutableEntry(x.getKey(), x.getValue().get(dim)));
            return stream.collect(Collectors.toList());
        }

        @Override
        public Collection<EntityPos> values() {
            return this.waypoints.values().stream().map((pos) -> pos.get(dim)).collect(Collectors.toList());
        }

        @Override
        public int size() {
            return this.waypoints.size();
        }
    }

    private class IndependentWorldWayPoint implements IWaypointsStorage {

        private final Int2ObjectMap<Object2ObjectMap<String, WorldDataWaypoints.IndependentEntityPos>> waypointsMap = new Int2ObjectOpenHashMap<>();

        private IndependentWaypoints getForDim(int dim) {
            return new IndependentWaypoints(dim, this.waypointsMap.computeIfAbsent(dim, (key) -> new Object2ObjectOpenHashMap<>()));
        }

        @Override
        public IndependentWaypoints get(World world) {
            return getForDim(world.provider.getDimension());
        }

        @Override
        public NBTTagList serializeNBT() {
            NBTTagList list = new NBTTagList();
            for (Int2ObjectMap.Entry<Object2ObjectMap<String, IndependentEntityPos>> entries : this.waypointsMap.int2ObjectEntrySet()) {
                int dim = entries.getIntKey();
                for (Map.Entry<String, IndependentEntityPos> entry : entries.getValue().entrySet()) {
                    list.appendTag(storeEntityPos(entry.getKey(), entry.getValue().get(dim)));
                }
            }
            return list;
        }

        @Override
        public void deserializeNBT(NBTTagList nbt) {
            waypointsMap.clear();
            for (NBTBase tag : nbt) {
                NBTTagCompound compound = (NBTTagCompound) tag;
                IndependentWaypoints map = getForDim(compound.getInteger("dim"));
                EntityPos pos = EntityPos.fromNBT(compound);
                map.waypoints.put(compound.getString("name"), new IndependentEntityPos(pos));
            }
        }
    }
}
