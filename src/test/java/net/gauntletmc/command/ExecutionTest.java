package net.gauntletmc.command;


import net.gauntletmc.command.annotations.Alias;
import net.gauntletmc.command.annotations.Completions;
import net.gauntletmc.command.annotations.DefaultCommand;
import net.gauntletmc.command.annotations.Greedy;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.parser.ArgumentQueryResult;
import net.minestom.server.command.builder.parser.CommandParser;
import net.minestom.server.command.builder.parser.CommandQueryResult;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.truth.Truth.assertThat;

public class ExecutionTest {

    // Test Utility
    private static boolean INITIALIZED = false;
    private static final AtomicReference<String> commandResult = new AtomicReference<>(null);
    private static void testCommand(String command) {
        testCommand(command, command);
    }
    private static void testCommand(String command, String expect) {
        commandResult.set(null);
        CommandResult result = MinecraftServer.getCommandManager().executeServerCommand(command);
        assertThat(result.getType()).isEqualTo(CommandResult.Type.SUCCESS);
        assertThat(commandResult.get()).isEqualTo(expect);
    }
    private static void ensureInitialized() {
        if (INITIALIZED) return;
        MinecraftServer.init();
        INITIALIZED = true;
    }

    /**
     * Simple execution tests with no arguments
     */
    @Test
    public void simpleTest() {
        ensureInitialized();
        Minestand.register(Simple.class);
        testCommand("simple");
        testCommand("simple plum");
        testCommand("simple grapefruit");
        testCommand("simple orange pear");
    }

    @Alias("simple")
    public static class Simple {
        /**
         * A simple default command. This method is executed using "/simple"
         */
        @DefaultCommand
        public void defaultCommand(CommandSender sender) {
            commandResult.set("simple");
        }

        /**
         * A subcommand of 'simple'. This method is executing using "/simple plum"
         */
        @Alias("plum")
        public void subcommand(CommandSender sender) {
            commandResult.set("simple plum");
        }

        /**
         * A subcommand of 'simple', creating using an inner-class containing a command with @DefaultCommand
         * This should never be used over the previous case, but it is included in the test cases for completed-ness
         * This method is executed using "/simple grapefruit"
         */
        @Alias("grapefruit")
        public static class SubCommand {
            @DefaultCommand
            public void defaultCommand(CommandSender sender) {
                commandResult.set("simple grapefruit");
            }
        }

        /**
         * A sub-sub-command, created using an inner-class
         * This method is execuring using "/simple orange pear"
         */
        @Alias("orange")
        public static class SubSubCommand {
            @Alias("pear")
            public void subcommand(CommandSender sender) {
                commandResult.set("simple orange pear");
            }
        }
    }

    /**
     * Argument parsing tests
     */
    @Test
    public void argsTest() {
        ensureInitialized();
        Minestand.register(Args.class);
        testCommand("args 5 hello", "int:5 str:hello");
        testCommand("args vec 27 12 8", "Vec[x=27.0, y=12.0, z=8.0]");
        testCommand("args greedy this is a sentence with spaces", "this is a sentence with spaces");
    }

    @Alias("args")
    public static class Args {
        @DefaultCommand
        public void defaultCommand(CommandSender sender, int a, String str) {
            commandResult.set("int:" + a + " str:" + str);
        }
        @Alias("vec")
        public void vecCommand(CommandSender sender, Vec vec) {
            commandResult.set(vec.toString());
        }
        @Alias("greedy")
        public void greedyCommand(CommandSender sender, @Greedy String str) {
            commandResult.set(str);
        }
    }

    /**
     * Completion tests
     */
    @Test
    public void completionTest() {
        ensureInitialized();
        CompletionProvider.createLookup("@complete", "net.gauntletmc.command.ExecutionTest$Completion;completions");
        Minestand.register(Completion.class);

        final String text = "completion h";

        String commandString = text.replaceFirst(CommandManager.COMMAND_PREFIX, "");

        final CommandQueryResult commandQueryResult = CommandParser.findCommand(commandString);
        assertThat(commandQueryResult).isNotEqualTo(null);

        final ArgumentQueryResult queryResult = CommandParser.findEligibleArgument(commandQueryResult.command,
                commandQueryResult.args, commandString, text.endsWith(StringUtils.SPACE), false,
                CommandSyntax::hasSuggestion, Argument::hasSuggestion);
        assertThat(queryResult).isNotEqualTo(null);

        final SuggestionCallback suggestionCallback = queryResult.argument.getSuggestionCallback();
        assertThat(suggestionCallback).isNotEqualTo(null);

        Suggestion suggestion = new Suggestion(queryResult.input, 0, 0);
        suggestionCallback.apply(null, queryResult.context, suggestion);

        assertThat(suggestion.getEntries().size()).isEqualTo(2);
        assertThat(suggestion.getEntries().get(0).getEntry()).isEqualTo("hi");
        assertThat(suggestion.getEntries().get(1).getEntry()).isEqualTo("hello");
    }

    @Alias("completion")
    public static class Completion {
        public static Collection<String> completions(CommandSender sender, CommandContext context) {
            return List.of("hi", "hello", "banana");
        }

        @DefaultCommand
        public void defaultCommand(CommandSender sender, @Completions("@complete") String str) {
        }

        @Alias("other")
        public void otherCommand(CommandSender sender, @Completions("completions") String str) {
        }
    }



}
