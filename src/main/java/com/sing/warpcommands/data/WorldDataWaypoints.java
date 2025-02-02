package com.sing.warpcommands.data;

import com.google.common.collect.Maps;
import com.sing.warpcommands.Configure;
import com.sing.warpcommands.utils.EntityPos;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldDataWaypoints extends WorldSavedData {
    private static final String NAME = "WayPoints";
    public WorldDataWaypoints(String name) {
        super(name);
        updateConfigure();
    }

    public WorldDataWaypoints() {
        this(NAME);
    }

    private IWaypointsStorage storage;

    public static IWaypointList get(World world) {
        if (!(world instanceof ServerWorld)) throw new InvalidParameterException("Expected a server world");
        DimensionSavedDataManager manager = ((ServerWorld) world).getServer().overworld().getDataStorage();
        WorldDataWaypoints data = manager.get(WorldDataWaypoints::new, NAME);
        if (data == null) {
            data = new WorldDataWaypoints("WayPoints");
            manager.set(data);
        }
        return data.storage.get(world);
    }

    public void updateConfigure() {
        storage = Configure.dimensionIndependentWaypoints.get() ? new IndependentWorldWayPoint() : new SharedWorldWayPointStorage();
    }

    @Override
    public void load(CompoundNBT nbt) {
        storage.deserializeNBT(nbt.getCompound("WayPoints"));
    }

    @Override
    public @NotNull CompoundNBT save(@NotNull CompoundNBT nbt) {
        nbt.put("WayPoints", storage.serializeNBT());
        return nbt;
    }

    public interface IWaypointsStorage extends INBTSerializable<CompoundNBT> {
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
            setDirty();
        }

        @Override
        public boolean has(String name) {
            return waypoints.containsKey(name);
        }

        @Override
        public @Nullable EntityPos remove(String name) {
            EntityPos removed = this.waypoints.remove(name);
            if (removed != null) setDirty();
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

    private static CompoundNBT storeEntityPos(EntityPos pos) {
        return pos.serializeNBT();
    }

    private class SharedWorldWayPointStorage implements IWaypointsStorage {
        SharedWaypointList waypointsMap = new SharedWaypointList();

        @Override
        public SharedWaypointList get(World _world) {
            return waypointsMap;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            for (Map.Entry<String, EntityPos> entry : this.waypointsMap.entries()) {
                nbt.put(entry.getKey(), storeEntityPos(entry.getValue()));
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            waypointsMap.waypoints.clear();
            for (String name : nbt.getAllKeys()) {
                EntityPos pos = EntityPos.fromNBT(nbt.getCompound(name));
                waypointsMap.waypoints.put(name, pos);
            }
        }
    }

    private static class IndependentEntityPos {
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;

        public EntityPos get(RegistryKey<World> dim) {
            return new EntityPos(x, y, z, yaw, pitch, dim);
        }

        public IndependentEntityPos(EntityPos pos) {
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
            this.yaw = pos.yRot;
            this.pitch = pos.xRot;
        }
    }

    private class IndependentWaypoints implements IWaypointList {
        private final RegistryKey<World> dim;
        private final Object2ObjectMap<String, IndependentEntityPos> waypoints;

        private IndependentWaypoints(RegistryKey<World> dim, Object2ObjectMap<String, IndependentEntityPos> waypoints) {
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
            setDirty();
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
                setDirty();
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

        private final Map<RegistryKey<World>, Object2ObjectMap<String, WorldDataWaypoints.IndependentEntityPos>> waypointsMap = new HashMap<>();

        private IndependentWaypoints getForDim(RegistryKey<World> dim) {
            return new IndependentWaypoints(dim, this.waypointsMap.computeIfAbsent(dim, (key) -> new Object2ObjectOpenHashMap<>()));
        }

        @Override
        public IndependentWaypoints get(World world) {
            return getForDim(world.dimension());
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            for (Map.Entry<RegistryKey<World>, Object2ObjectMap<String, IndependentEntityPos>> entries : this.waypointsMap.entrySet()) {
                for (Map.Entry<String, IndependentEntityPos> entry : entries.getValue().entrySet()) {
                    nbt.put(entry.getKey(), storeEntityPos(entry.getValue().get(entries.getKey())));
                }
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            waypointsMap.clear();
            for (String name : nbt.getAllKeys()) {
                final CompoundNBT compound = nbt.getCompound(name);
                IndependentWaypoints map = getForDim(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("dim"))));
                EntityPos pos = EntityPos.fromNBT(compound);
                map.waypoints.put(name, new IndependentEntityPos(pos));
            }
        }
    }
}
