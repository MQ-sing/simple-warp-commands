package com.sing.warpcommands;

import com.mojang.brigadier.CommandDispatcher;
import com.sing.warpcommands.commands.*;
import com.sing.warpcommands.commands.utils.IMatchProvider;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.data.WorldDataWaypoints;
import com.sing.warpcommands.jeic.JEIChIntegration;
import com.sing.warpcommands.network.Networking;
import com.sing.warpcommands.network.UpdateWaypointPack;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

@Mod("simple_warp_commands")
@Mod.EventBusSubscriber
public class WarpCommandsMod {
    private static IMatchProvider matchProvider;

    @SubscribeEvent
    static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CapabilityManager.INSTANCE.register(CapabilityPlayer.PlayerLocations.class, new Capability.IStorage<CapabilityPlayer.PlayerLocations>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<CapabilityPlayer.PlayerLocations> capability, CapabilityPlayer.PlayerLocations playerLocations, Direction direction) {
                return null;
            }

            @Override
            public void readNBT(Capability<CapabilityPlayer.PlayerLocations> capability, CapabilityPlayer.PlayerLocations playerLocations, Direction direction, INBT inbt) {

            }
        }, () -> null));

    }

    public WarpCommandsMod() {
        Networking.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configure.SPEC);
        if (FMLEnvironment.dist.isClient() && ModList.get().isLoaded("jecharacters")) {
            matchProvider = JEIChIntegration.provider();
        } else {
            matchProvider = (match, str) -> Utils.matchesSubStr(match, str, (a, starts, b) -> a.startsWith(b));
        }
    }

    @SubscribeEvent
    static void registerServerCommand(RegisterCommandsEvent e) {
        final CommandDispatcher<CommandSource> dispatcher = e.getDispatcher();
        if (Configure.enableWarpCommand.get()) CommandWarp.register(dispatcher);
        if (Configure.enableHomeCommand.get()) CommandHome.register(dispatcher);
        if (Configure.enableBackCommand.get()) CommandBack.register(dispatcher);
        if (Configure.enableSpawnCommand.get()) CommandSpawn.register(dispatcher);
        if (Configure.enablePosCommand.get()) CommandPos.register(dispatcher);
        if (Configure.enableTpPlayerCommand.get()) CommandTeleportPlayer.register(dispatcher);
        if (Configure.enableByeCommand.get()) CommandBye.register(dispatcher);
    }

    @SubscribeEvent
    static void attachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> e) {
        if (!(e.getObject() instanceof ServerPlayerEntity)) return;
        ICapabilitySerializable<CompoundNBT> p = new CapabilityPlayer.ProvidePlayer();
        e.addCapability(id("player_locations"), p);
    }

    @SubscribeEvent
    static void onPlayerClone(PlayerEvent.Clone e) {
        LazyOptional<CapabilityPlayer.PlayerLocations> from = CapabilityPlayer.get(e.getOriginal());
        LazyOptional<CapabilityPlayer.PlayerLocations> to = CapabilityPlayer.get(e.getPlayer());
        from.ifPresent(original -> to.ifPresent(created -> {
            created.homePosition.position = original.homePosition.position;
            created.backPosition.position = original.backPosition.position;
            created.recordedPosition.position = original.recordedPosition.position;
        }));
    }

    @SubscribeEvent
    static void onEntityDeath(LivingDeathEvent e) {
        if (!Configure.doBackRecordDeath.get() || !(e.getEntity() instanceof PlayerEntity)) return;
        final PlayerEntity player = (PlayerEntity) e.getEntity();
        LazyOptional<CapabilityPlayer.PlayerLocations> l = CapabilityPlayer.get(player);
        l.ifPresent(cap -> cap.backPosition.relocate(player));
    }

    public static void syncWaypointsFor(ServerPlayerEntity player) {
        final WorldDataWaypoints.IWaypointList waypoints = WorldDataWaypoints.get(player.getLevel());
        Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), UpdateWaypointPack.of(waypoints));
    }

    public static void syncWaypoints(WorldDataWaypoints.IWaypointList data, MinecraftServer server) {
        final UpdateWaypointPack pack = UpdateWaypointPack.of(data);
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), pack);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        syncWaypointsFor((ServerPlayerEntity) e.getPlayer());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getPlayer() instanceof ServerPlayerEntity) syncWaypointsFor((ServerPlayerEntity) e.getPlayer());
    }
    public static ResourceLocation id(String id) {
        return new ResourceLocation("simple_warp_commands", id);
    }

    public static int matchSubStr(String str, String match) {
        return matchProvider.match(str, match);
    }
}
