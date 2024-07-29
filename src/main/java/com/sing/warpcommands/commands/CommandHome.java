package com.sing.warpcommands.commands;

import com.sing.warpcommands.ModWorldData;
import com.sing.warpcommands.utils.WayPoint;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommandHome {
    static class CommandHomeTeleport extends CommandBase {
        @Override
        public String getName() {
            return "home";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "home.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayerMP)) {
                return;
            }
            if (args.length != 0) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = (EntityPlayerMP) sender;
            ModWorldData data = ModWorldData.get(sender.getEntityWorld());

            WayPoint point = data.getHome(player);
            if (point == null) {
                throw new CommandException(I18n.format("home.tip"));
            }
            point.applyTo(player);
            player.sendMessage(new TextComponentTranslation("home.on", point.name));
        }
    }

    static class CommandHomeSet extends CommandBase {

        @Override
        public String getName() {
            return "sethome";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "sethome.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length != 0) throw new WrongUsageException(this.getUsage(sender));
            if (!(sender instanceof EntityPlayerMP)) return;
            EntityPlayerMP player = (EntityPlayerMP) sender;
            ModWorldData.get(sender.getEntityWorld()).setHome(player);
        }

        @Override
        public List<String> getAliases() {
            return Collections.singletonList("home!");
        }
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandHomeTeleport());
        e.registerServerCommand(new CommandHomeSet());
    }
}
