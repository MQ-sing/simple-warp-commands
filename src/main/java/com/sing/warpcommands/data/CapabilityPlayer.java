package com.sing.warpcommands.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CapabilityPlayer {

    public static class PlayerLocations implements INBTSerializable<CompoundNBT> {
        public static class Position {
            private static final SimpleCommandExceptionType NOT_FOUND = new SimpleCommandExceptionType(new TranslationTextComponent("locations.notfound"));
            @Nullable
            public EntityPos position = null;

            public void teleport(ServerPlayerEntity player) throws CommandSyntaxException {
                if (position == null) throw NOT_FOUND.create();
                position.teleport(player);
            }

            void readFromNBT(CompoundNBT tag) {
                position = EntityPos.fromNBT(tag);
            }

            void serializeAs(String name, CompoundNBT target) {
                if (position == null) return;
                target.put(name, position.serializeNBT());
            }

            public boolean exist() {
                return position != null;
            }

            public void relocate(@NotNull PlayerEntity e) {
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
        public CompoundNBT serializeNBT() {
            CompoundNBT compound = new CompoundNBT();
            this.homePosition.serializeAs("home", compound);
            this.recordedPosition.serializeAs("recorded", compound);
            this.backPosition.serializeAs("back", compound);
            return compound;
        }

        public void deserializeNBT(CompoundNBT nbt) {
            if (nbt.contains("home")) this.homePosition.readFromNBT(nbt.getCompound("home"));
            if (nbt.contains("back")) this.backPosition.readFromNBT(nbt.getCompound("back"));
            if (nbt.contains("recorded")) this.recordedPosition.readFromNBT(nbt.getCompound("recorded"));
        }
    }

    public static class ProvidePlayer implements ICapabilitySerializable<CompoundNBT>, ICapabilityProvider {
        private PlayerLocations locations = null;

        private PlayerLocations get() {
            if (locations == null) locations = new PlayerLocations();
            return locations;
        }
        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
            return capability == cap ? LazyOptional.of(this::get).cast() : LazyOptional.empty();
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT compound = new CompoundNBT();
            compound.put("PlayerLocations", get().serializeNBT());
            return compound;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            get().deserializeNBT(nbt.getCompound("PlayerLocations"));
        }
    }

    @CapabilityInject(PlayerLocations.class)
    public static Capability<PlayerLocations> cap;

    public static @NotNull LazyOptional<PlayerLocations> get(PlayerEntity player) {
        return player.getCapability(cap, null);
    }
}
