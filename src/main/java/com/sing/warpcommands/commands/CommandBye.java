package com.sing.warpcommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.sing.warpcommands.commands.utils.Utils;
import net.minecraft.command.CommandSource;

public class CommandBye {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Utils.command("bye").executes(ctx -> {
                    ctx.getSource().getEntityOrException().kill();
                    return 1;
                })
        );
        dispatcher.register(Utils.command("ks").executes(ctx -> {
            ctx.getSource().getEntityOrException().kill();
            return 1;
        }));

    }
}
