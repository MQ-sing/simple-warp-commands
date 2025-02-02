package com.sing.warpcommands.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sing.warpcommands.Configure;
import com.sing.warpcommands.data.CapabilityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public class EntityPos {
    public double x;
    public double y;
    public double z;
    public float yRot;
    public float xRot;
    public RegistryKey<World> dim;


    public void relocate(@NotNull RegistryKey<World> dim, double x, double y, double z, float yRot, float xRot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.xRot = xRot;
        this.dim = dim;
    }

    public void relocate(@NotNull Entity entity) {
        relocate(entity.level.dimension(), entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
    }

    public EntityPos(double x, double y, double z, float yRot, float xRot, @NotNull RegistryKey<World> dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.xRot = xRot;
        this.dim = dim;
    }

    public EntityPos(double x, double y, double z, float yRot, float xRot, ResourceLocation dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.xRot = xRot;
        if (dim == null) {
            throw new IllegalStateException("Data corrupted!Unable to read the dimension info");
        }
        this.dim = RegistryKey.create(Registry.DIMENSION_REGISTRY, dim);
    }

    public EntityPos(BlockPos pos, RegistryKey<World> dim) {
        this(pos.getX(), pos.getY(), pos.getZ(), 90, 0, dim);
    }

    private EntityPos(CompoundNBT tag) {
        this(
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.getFloat("yaw"),
                tag.getFloat("pitch"),
                ResourceLocation.tryParse(tag.getString("dim"))
        );
    }
    public EntityPos(Entity entity) {
        relocate(entity);
    }

    public static void teleport(@NotNull ServerPlayerEntity teleported, ServerPlayerEntity target) throws CommandSyntaxException {
        new EntityPos(teleported).teleport(target);
    }

    public static final SimpleCommandExceptionType NO_DIM_CROSS = new SimpleCommandExceptionType(new TranslationTextComponent("teleport.no_dimension_cross"));
    public static final DynamicCommandExceptionType DIM_NOT_EXIST = new DynamicCommandExceptionType(name -> new TranslationTextComponent("teleport.no_dim", name));

    public void teleport(ServerPlayerEntity e) throws CommandSyntaxException {
        if (!e.server.getWorldData().worldGenSettings().dimensions().containsKey(dim.location()))
            throw DIM_NOT_EXIST.create(dim);
        LazyOptional<CapabilityPlayer.PlayerLocations> cap = CapabilityPlayer.get(e);
        cap.ifPresent(c -> c.backPosition.position = new EntityPos(e));
        if (!Configure.enableRiding.get()) e.stopRiding();
        if (e.level.dimension().equals(dim)) {
            e.connection.teleport(x, y, z, yRot, xRot);
        } else {
            if (!Configure.couldTeleportTo(e.level.dimension(), dim)) throw NO_DIM_CROSS.create();
            ServerWorld world = e.server.getLevel(dim);
            if (world == null) throw DIM_NOT_EXIST.create(dim.toString());
            e.teleportTo(world, x, y, z, yRot, xRot);
        }
    }


    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putDouble("x", x);
        nbt.putDouble("y", y);
        nbt.putDouble("z", z);

        nbt.putDouble("yaw", yRot);
        nbt.putDouble("pitch", xRot);
        nbt.putString("dim", dim.location().toString());
        return nbt;
    }
    @Contract(value = "null->null;!null->!null", pure = true)
    public static EntityPos fromNBT(CompoundNBT nbt) {
        if (nbt == null) return null;
        return new EntityPos(nbt);
    }

    public double distanceSquared(double x, double y, double z) {
        return (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) + (z - this.z) * (z - this.z);
    }

    @Override
    public String toString() {
        String dimName = dim.location().toString();
        return Configure.showCoordinates.get() ?
                String.format("[%.1f,%.1f,%.1f], %s", x, y, z, dimName) :
                String.format("in %s", dimName);
    }
}
