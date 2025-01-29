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

    public void relocate(int dim, double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.dim = dim;
    }

    public void relocate(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.yaw = 0;
        this.pitch = 90;
    }

    public void relocate(@NotNull Entity entity) {
        relocate(entity.dimension, entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
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

    public EntityPos(int dim, @NotNull BlockPos pos) {
        relocate(pos);
    }

    public EntityPos(Entity entity) {
        relocate(entity);
    }

    public static void teleport(@NotNull EntityPlayerMP target, EntityPlayerMP e) throws CommandException {
        new EntityPos(target).teleport(e);
    }

    public static void teleport(@NotNull BlockPos pos, EntityPlayerMP e) throws CommandException {
        new EntityPos(e.dimension, pos).teleport(e);
    }

    public void teleport(EntityPlayerMP e) throws CommandException {
        if (!DimensionManager.isDimensionRegistered(dim)) throw new CommandException("teleport.no_dim");
        CapabilityPlayer.PlayerLocations cap = CapabilityPlayer.get(e);
        if (cap != null) cap.backPosition.position = new EntityPos(e);
        e.dismountRidingEntity();
        if (e.dimension == dim) {
            e.connection.setPlayerLocation(x, y, z, yaw, pitch);
        } else {
            if (!Configure.allowDimensionCross) throw new CommandException(I18n.format("teleport.no_dimension_cross"));
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

    @Override
    public String toString() {
        String dimName = DimensionManager.getProviderType(dim).getName();
        return Configure.showCoordinates ?
                String.format("[%.1f,%.1f,%.1f], %s", x, y, z, dimName) :
                String.format("in %s", dimName);
    }
}
