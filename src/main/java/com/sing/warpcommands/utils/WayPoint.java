package com.sing.warpcommands.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class WayPoint implements INBTSerializable<NBTTagCompound> {
    public String name;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public int dim;

    public WayPoint(String name, double x, double y, double z, float yaw, float pitch, int dim) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.dim = dim;
    }

    public WayPoint(NBTTagCompound nbt) {
        this.deserializeNBT(nbt);
    }

    public WayPoint(String name, EntityPlayerMP player) {
        this.name = name;
        this.x = player.posX;
        this.y = player.posY;
        this.z = player.posZ;
        this.yaw = player.rotationYaw;
        this.pitch = player.rotationPitch;
        this.dim = player.dimension;
    }

    public void applyTo(EntityPlayerMP e) {
        if (e.dimension != dim) {
            e = (EntityPlayerMP) e.changeDimension(dim);
        }
        if (e == null) return;
        e.connection.setPlayerLocation(x, y, z, yaw, pitch);
        e.playSound(new SoundEvent(new ResourceLocation("minecraft", "mob.endermen.portal")), 0.6f, 1.1f);

    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("name", new NBTTagString(name));
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
        name = nbt.getString("name");
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
        z = nbt.getDouble("z");
        yaw = nbt.getFloat("yaw");
        pitch = nbt.getFloat("pitch");
        dim = nbt.getInteger("dim");
    }
}
