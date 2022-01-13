package net.gauntletmc.command;

import net.gauntletmc.command.annotations.Alias;
import net.gauntletmc.command.exception.CommandParseException;
import net.minestom.server.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class StaticTest {

    @Test
    public void staticTest() {
        MinestandParser.parseClass(AllStatic.class);

        // Inner class StaticTest$NonStatic must be given `this`
        MinestandParser.parseClass(AllNonStatic.class, this);
        MinestandParser.parseClass(SomeStatic.class, this);
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(AllNonStatic.class));
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(SomeStatic.class));
    }

    @Alias("name")
    public class AllStatic {
        public AllStatic() {
            throw new RuntimeException("Constructor called");
        }
        @Alias("command")
        public static void command(CommandSender sender) {}

        @Alias("subcommand")
        public static class StaticSubclass {}
    }

    @Alias("name")
    public class SomeStatic {
        @Alias("command")
        public void command(CommandSender sender) {}

        @Alias("command2")
        public static void command2(CommandSender sender) {}

        @Alias("subcommand")
        public static class StaticSubclass {}

        @Alias("subcommand2")
        public static class NotStaticSubclass {}
    }

    @Alias("name")
    public class AllNonStatic {
        @Alias("command")
        public void command(CommandSender sender) {}

        @Alias("subcommand")
        public class NotStaticSubclass {}
    }

}
