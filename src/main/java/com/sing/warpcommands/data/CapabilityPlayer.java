package com.sing.warpcommands.data;

import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
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
            instance.homePosition.serializeAs("home", compound);
            instance.recordedPosition.serializeAs("recorded", compound);
            instance.backPosition.serializeAs("back", compound);
            return compound;
        }

        @Override
        public void readNBT(Capability<PlayerLocations> capability, PlayerLocations instance, EnumFacing side, NBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            try {
                instance.homePosition.readFromNBT((NBTTagCompound) compound.getTag("home"));
                instance.backPosition.readFromNBT((NBTTagCompound) compound.getTag("back"));
                instance.recordedPosition.readFromNBT((NBTTagCompound) compound.getTag("recorded"));
            } catch (ClassCastException ignored) {
            }
        }
    }

    public static class PlayerLocations {
        public static class Position {
            @Nullable
            public EntityPos position = null;

            public void teleport(EntityPlayerMP player) throws CommandException {
                if (position == null) throw new CommandException(I18n.format("locations.notfound"));
                position.teleport(player);
            }

            void readFromNBT(NBTTagCompound tag) {
                position = EntityPos.fromNBT(tag);
            }

            void serializeAs(String name, NBTTagCompound target) {
                if (position == null) return;
                target.setTag(name, position.serializeNBT());
            }

            public boolean has() {
                return position != null;
            }

            public void relocate(@NotNull EntityPlayer e) {
                if (position == null) position = new EntityPos(e);
                else position.relocate(e);
            }

            public void clear() {
                position = null;
            }
        }

        public final Position homePosition = new Position();
        public final Position backPosition = new Position();
        public final Position recordedPosition = new Position();
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

    @Nullable
    public static CapabilityPlayer.PlayerLocations get(EntityPlayer player) {
        return player.getCapability(cap, null);
    }
}
