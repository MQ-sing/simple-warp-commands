package com.sing.warpcommands.data;

import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CapabilityPlayer {

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

            public boolean exist() {
                return position != null;
            }

            public void relocate(@NotNull EntityPlayer e) {
                if (position == null) position = new EntityPos(e);
                else position.relocate(e);
            }

            public void clear() {
                position = null;
            }

            public String toString() {
                return position == null ? "null" : position.toString();
            }
        }

        public final Position homePosition = new Position();
        public final Position backPosition = new Position();
        public final Position recordedPosition = new Position();

        @NotNull
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            this.homePosition.serializeAs("home", compound);
            this.recordedPosition.serializeAs("recorded", compound);
            this.backPosition.serializeAs("back", compound);
            return compound;
        }

        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt.hasKey("home")) this.homePosition.readFromNBT(nbt.getCompoundTag("home"));
            if (nbt.hasKey("back")) this.backPosition.readFromNBT(nbt.getCompoundTag("back"));
            if (nbt.hasKey("recorded")) this.recordedPosition.readFromNBT(nbt.getCompoundTag("recorded"));
        }
    }

    public static class ProvidePlayer implements ICapabilitySerializable<NBTTagCompound>, ICapabilityProvider {
        private PlayerLocations locations = null;

        private PlayerLocations get() {
            if (locations == null) locations = new PlayerLocations();
            return locations;
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @javax.annotation.Nullable EnumFacing facing) {
            return capability == cap;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> @Nullable T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == cap ? (T) get() : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("PlayerLocations", get().serializeNBT());
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            get().deserializeNBT(nbt.getCompoundTag("PlayerLocations"));
        }
    }

    @CapabilityInject(PlayerLocations.class)
    public static Capability<PlayerLocations> cap;

    @Nullable
    public static CapabilityPlayer.PlayerLocations get(EntityPlayer player) {
        return player.getCapability(cap, null);
    }
}
