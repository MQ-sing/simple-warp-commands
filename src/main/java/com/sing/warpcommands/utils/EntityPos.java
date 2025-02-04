package com.sing.warpcommands.utils;

import com.sing.warpcommands.Configure;
import com.sing.warpcommands.data.CapabilityPlayer;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public class EntityPos {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public int dim;

    public void relocate(EntityPos pos) {
        x = pos.x;
        y = pos.y;
        z = pos.z;
        yaw = pos.yaw;
        pitch = pos.pitch;
        dim = pos.dim;
    }

    public EntityPos(double x, double y, double z, float yaw, float pitch, int dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.dim = dim;
    }

    private EntityPos(NBTTagCompound tag) {
        this(
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.getFloat("yaw"),
                tag.getFloat("pitch"),
                tag.getInteger("dim")
        );
    }

    public EntityPos(@NotNull BlockPos pos, int dim) {
        this(pos.getX(), pos.getY(), pos.getZ(), 0, 0, dim);
    }

    public EntityPos(Entity entity) {
        this(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch, entity.dimension);
    }

    public static void teleport(@NotNull EntityPlayerMP target, EntityPlayerMP e) throws CommandException {
        new EntityPos(target).teleport(e);
    }
    public void teleport(EntityPlayerMP e) throws CommandException {
        if (!DimensionManager.isDimensionRegistered(dim)) throw new CommandException("teleport.no_dim");
        CapabilityPlayer.PlayerLocations cap = CapabilityPlayer.get(e);
        if (cap != null) cap.backPosition.position = new EntityPos(e);
        e.dismountRidingEntity();
        if (e.dimension == dim) {
            e.connection.setPlayerLocation(x, y, z, yaw, pitch);
        } else {
            if (!Configure.couldTeleportTo(e.dimension, dim))
                throw new CommandException(I18n.format("teleport.no_dimension_cross", DimensionManager.getProviderType(e.dimension).getName(), DimensionManager.getProviderType(dim).getName()));
            e.server.getPlayerList().transferPlayerToDimension(e, dim, (world, entity, __) -> entity.setLocationAndAngles(x, y, z, yaw, pitch));
        }
    }


    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("x", new NBTTagDouble(x));
        nbt.setTag("y", new NBTTagDouble(y));
        nbt.setTag("z", new NBTTagDouble(z));

        nbt.setTag("yaw", new NBTTagFloat(yaw));
        nbt.setTag("pitch", new NBTTagFloat(pitch));
        nbt.setTag("dim", new NBTTagInt(dim));
        return nbt;
    }
    @Contract(value = "null->null;!null->!null", pure = true)
    public static EntityPos fromNBT(NBTTagCompound nbt) {
        if (nbt == null) return null;
        return new EntityPos(nbt);
    }

    public double distanceSquared(double x, double y, double z) {
        return (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) + (z - this.z) * (z - this.z);
    }

    @Override
    public String toString() {
        String dimName = DimensionManager.getProviderType(dim).getName();
        return Configure.showWaypointCoords ?
                String.format("[%.1f,%.1f,%.1f], %s", x, y, z, dimName) :
                String.format("in %s", dimName);
    }
}
