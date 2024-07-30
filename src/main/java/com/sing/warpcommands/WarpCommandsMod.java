package com.sing.warpcommands;

import com.sing.simple_warp_commands.Tags;
import com.sing.warpcommands.commands.CommandBack;
import com.sing.warpcommands.commands.CommandHome;
import com.sing.warpcommands.commands.CommandNewTeleport;
import com.sing.warpcommands.commands.CommandWarp;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
@Mod.EventBusSubscriber
public class WarpCommandsMod {

    @Mod.EventHandler
    void preInit(FMLPreInitializationEvent e) {
        CapabilityManager.INSTANCE.register(CapabilityPlayer.PlayerLocations.class, new CapabilityPlayer.Data(), CapabilityPlayer.PlayerLocations::new);
    }
    @Mod.EventHandler
    void serverInit(FMLServerStartingEvent e) {
        if (Configure.enableWarpCommand) CommandWarp.init(e);
        if (Configure.enableHomeCommand) {
            CommandHome.init(e);
        }
        if (Configure.enableBackCommand) {
            e.registerServerCommand(new CommandBack());
        }
        if (Configure.enableTpPlusCommand) {
            e.registerServerCommand(new CommandNewTeleport());
        }
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
        p.homePosition = original.homePosition;
        p.backPosition = e.isWasDeath() ? new EntityPos(e.getOriginal()) : original.backPosition;

    }

    public static ResourceLocation id(String id) {
        return new ResourceLocation(Tags.MOD_ID, id);
    }
}
