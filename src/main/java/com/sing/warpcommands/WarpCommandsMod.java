package com.sing.warpcommands;

import com.sing.simple_warp_commands.Tags;
import com.sing.warpcommands.commands.*;
import com.sing.warpcommands.commands.utils.IMatchProvider;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.jeic.JEIChIntegration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "after:jecharacters@[1.12.0-3.6.1,);")
@Mod.EventBusSubscriber
public class WarpCommandsMod {
    private static IMatchProvider matchProvider;


    @Mod.EventHandler
    void preInit(FMLPreInitializationEvent e) {
        CapabilityManager.INSTANCE.register(CapabilityPlayer.PlayerLocations.class, new Capability.IStorage<CapabilityPlayer.PlayerLocations>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<CapabilityPlayer.PlayerLocations> capability, CapabilityPlayer.PlayerLocations playerLocations, EnumFacing direction) {
                return null;
            }

            @Override
            public void readNBT(Capability<CapabilityPlayer.PlayerLocations> capability, CapabilityPlayer.PlayerLocations playerLocations, EnumFacing direction, NBTBase inbt) {

            }
        }, () -> null);
    }

    public WarpCommandsMod() {
        if (Loader.isModLoaded("jecharacters")) {
            matchProvider = JEIChIntegration.provider();
        } else {
            matchProvider = (match, str) -> Utils.matchesSubStr(match, str, (a, starts, b) -> a.startsWith(b));
        }
        Configure.update();
    }
    @Mod.EventHandler
    void serverInit(FMLServerStartingEvent e) {
        if (Configure.enableWarpCommand) CommandWarp.init(e);
        if (Configure.enableHomeCommand) CommandHome.init(e);
        if (Configure.enableBackCommand) CommandBack.init(e);
        if (Configure.enableSpawnCommand) CommandSpawn.init(e);
        if (Configure.enablePosCommand) CommandPos.init(e);
        if (Configure.enableTpPlayerCommand) {
            e.registerServerCommand(new CommandTeleportPlayer());
        }
        if (Configure.enableByeCommand) e.registerServerCommand(new CommandBye());
    }

    @SubscribeEvent
    static void attachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> e) {
        if (!(e.getObject() instanceof EntityPlayer)) return;
        ICapabilitySerializable<NBTTagCompound> p = new CapabilityPlayer.ProvidePlayer();
        e.addCapability(id("PlayerLocations"), p);
    }

    @SubscribeEvent
    static void onPlayerClone(PlayerEvent.Clone e) {
        CapabilityPlayer.PlayerLocations original = CapabilityPlayer.get(e.getOriginal());
        CapabilityPlayer.PlayerLocations p = CapabilityPlayer.get(e.getEntityPlayer());
        if (p == null || original == null) return;
        p.homePosition.position = original.homePosition.position;
        p.backPosition.position = original.backPosition.position;
        p.recordedPosition.position = original.recordedPosition.position;
    }

    @SubscribeEvent
    static void onEntityDeath(LivingDeathEvent e) {
        if (!Configure.enableBackRecordDeath || !(e.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) e.getEntity();
        CapabilityPlayer.PlayerLocations l = CapabilityPlayer.get(player);
        if (l != null) l.backPosition.relocate(player);
    }

    public static ResourceLocation id(String id) {
        return new ResourceLocation(Tags.MOD_ID, id);
    }

    public static int matchSubStr(String str, String match) {
        return matchProvider.match(str, match);
    }
}
