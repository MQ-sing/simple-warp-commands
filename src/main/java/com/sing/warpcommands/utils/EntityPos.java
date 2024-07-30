package com.sing.warpcommands.utils;

import com.sing.warpcommands.data.CapabilityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityPos implements INBTSerializable<NBTTagCompound> {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public int dim;

    EntityPos(double x, double y, double z, float yaw, float pitch, int dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.dim = dim;
    }

    public EntityPos() {
    }

    public EntityPos(@NotNull BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.yaw = 0;
        this.pitch = 90;
    }

    public EntityPos(EntityPlayer player) {
        this(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, player.dimension);
    }

    public void setTo(EntityPlayerMP e, @Nullable CapabilityPlayer.PlayerLocations cap) {
        if (e.dimension != dim) {
            e = (EntityPlayerMP) e.changeDimension(dim);
            if (e == null) return;
        }
        if (cap != null) cap.backPosition = new EntityPos(e);
        e.connection.setPlayerLocation(x, y, z, yaw, pitch);
        e.playSound(new SoundEvent(new ResourceLocation("minecraft", "mob.endermen.portal")), 0.6f, 1.1f);
    }

    @Override
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

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
        z = nbt.getDouble("z");
        yaw = nbt.getFloat("yaw");
        pitch = nbt.getFloat("pitch");
        dim = nbt.getInteger("dim");
    }

    @Contract(value = "!null->!null;null->null", pure = true)
    public static EntityPos fromNBT(NBTTagCompound nbt) {
        if (nbt == null) return null;
        EntityPos pos = new EntityPos();
        pos.deserializeNBT(nbt);
        return pos;
    }
}