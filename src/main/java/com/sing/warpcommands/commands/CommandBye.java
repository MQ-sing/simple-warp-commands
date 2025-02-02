package com.sing.warpcommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sing.warpcommands.commands.utils.Utils;
import net.minecraft.command.CommandSource;

public class CommandBye {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        final LiteralCommandNode<CommandSource> bye = dispatcher.register(
                Utils.command("bye").executes(ctx -> {
                    ctx.getSource().getEntityOrException().kill();
                    return 1;
                })
        );
        dispatcher.register(Utils.command("kil").redirect(bye));
    }
}
