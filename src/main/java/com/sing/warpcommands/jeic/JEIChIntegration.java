package com.sing.warpcommands.jeic;

import com.sing.warpcommands.commands.utils.IMatchProvider;
import com.sing.warpcommands.commands.utils.Utils;
import me.towdium.jecharacters.utils.Match;

public class JEIChIntegration {
    public static IMatchProvider provider() {
        return (match, str) -> Utils.matchesSubStr(match, str, (a, starts, b) -> Match.context.begins(a.substring(starts), b));
    }
}
