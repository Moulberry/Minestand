package net.gauntletmc.command;

import net.gauntletmc.command.annotations.Alias;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import org.junit.jupiter.api.Test;

public class TypeTest {

    @Test
    public void primitiveTest() {
        MinecraftServer.init();

        MinestandParser.parseClass(Primitives.class);
    }

    @Alias("name")
    public static class Primitives {
        @Alias("subcommand")
        public void method(CommandSender sender, boolean a, double b, float c, int d, long f) {}
    }





}
