package com.sing.warpcommands.utils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.INBTSerializable;

public class WayPoint extends EntityPos implements INBTSerializable<NBTTagCompound> {
    public String name;

    public WayPoint(String name, EntityPlayerMP player) {
        super(player);
        this.name = name;
    }

    public WayPoint() {
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setTag("name", new NBTTagString(name));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        name = nbt.getString("name");
        super.deserializeNBT(nbt);
    }

    public static WayPoint fromNBT(NBTTagCompound nbt) {
        WayPoint pos = new WayPoint();
        pos.deserializeNBT(nbt);
        return pos;
    }
}
