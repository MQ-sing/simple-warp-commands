package com.sing.warpcommands.data;

import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CapabilityPlayer {
    public static class Data implements Capability.IStorage<PlayerLocations> {

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<PlayerLocations> capability, PlayerLocations instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            if (instance.homePosition != null)
                compound.setTag("home", instance.homePosition.serializeNBT());
            if (instance.backPosition != null)
                compound.setTag("back", instance.backPosition.serializeNBT());
            return compound;
        }

        @Override
        public void readNBT(Capability<PlayerLocations> capability, PlayerLocations instance, EnumFacing side, NBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            instance.homePosition = EntityPos.fromNBT((NBTTagCompound) compound.getTag("home"));
            instance.backPosition = EntityPos.fromNBT((NBTTagCompound) compound.getTag("back"));
        }
    }

    public static class PlayerLocations {
        @Nullable
        public EntityPos homePosition;
        @Nullable
        public EntityPos backPosition;
    }

    public static class ProvidePlayer implements ICapabilitySerializable<NBTTagCompound>, ICapabilityProvider {
        private final PlayerLocations locations = new PlayerLocations();
        public static final Capability.IStorage<PlayerLocations> storage = cap.getStorage();

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            return cap.equals(capability);
        }

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            if (!hasCapability(capability, facing)) return null;
            return (T) locations;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("PlayerLocations", Objects.requireNonNull(storage.writeNBT(cap, locations, null)));
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            NBTTagCompound compound = nbt.getCompoundTag("PlayerLocations");
            storage.readNBT(cap, locations, null, compound);
        }
    }

    @CapabilityInject(PlayerLocations.class)
    public static Capability<PlayerLocations> cap;

    CapabilityPlayer.PlayerLocations get(EntityPlayerMP player) {
        return player.getCapability(cap, null);
    }
}
