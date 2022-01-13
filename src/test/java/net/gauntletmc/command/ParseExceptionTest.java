package net.gauntletmc.command;

import net.gauntletmc.command.annotations.Alias;
import net.gauntletmc.command.annotations.DefaultCommand;
import net.gauntletmc.command.exception.CommandParseException;
import net.minestom.server.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParseExceptionTest {

    /**
     * Missing Annotation Test:
     * Command classes must be annotated with @Alias
     */
    @Test
    public void missingAnnotationTest() {
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(MissingAnnotation.class));
    }

    public static class MissingAnnotation {}


    /**
     * Invalid Name Test:
     * You can't give an empty array or a name with spaces
     */
    @Test
    public void emptyNameTest() {
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(EmptyName.class));
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(EmptyName2.class));
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(IllegalName.class));
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(IllegalName2.class));
    }

    @Alias({})
    public static class EmptyName {}

    @Alias("name")
    public static class EmptyName2 {
        @Alias({})
        public void method(CommandSender sender) {}
    }

    @Alias("a b")
    public static class IllegalName {}

    @Alias("name")
    public static class IllegalName2 {
        @Alias("a b")
        public void method(CommandSender sender) {}
    }

    /**
     * Duplicate Name Test:
     * Command names must be unique
     */
    @Test
    public void duplicateNameTest() {
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(DuplicateNames1.class));
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(DuplicateNames2.class));
    }

    @Alias("name")
    public static class DuplicateNames1 {
        @Alias("subcommand")
        public void method(CommandSender sender) {}
        @Alias("subcommand")
        public void method2(CommandSender sender) {}
    }

    @Alias("name")
    public static class DuplicateNames2 {
        @Alias("subcommand")
        public static class StaticSubclass {}
        @Alias("subcommand")
        public static class StaticSubclass2 {}
    }

    /**
     * Missing Default Ctor Test:
     * Non-static command classes must have a default ctor
     */
    @Test
    public void noDefaultConstructorTest() {
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(NoDefaultConstructor.class));
    }

    @Alias("name")
    public static class NoDefaultConstructor {
        public NoDefaultConstructor(int argument) {}
        @Alias("subcommand")
        public void method(CommandSender sender) {} // Subcommand is needed so that class isn't considered all-static
    }

    /**
     * Unannotated Public Method Test:
     * Command classes can't have public methods that are not annotated with @Alias
     */
    @Test
    public void unannotatedPublicMethodTest() {
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(UnannotatedPublicMethod.class));
    }

    @Alias("name")
    public static class UnannotatedPublicMethod {
        public void method(CommandSender sender) {}
    }

    /**
     * Annotated Private Method Test:
     * Methods annotated with @Alias can't be private
     */
    @Test
    public void annotatedPrivateMethodTest() {
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(AnnotatedPrivateMethod.class));
    }

    @Alias("name")
    public static class AnnotatedPrivateMethod {
        @Alias("subcommand")
        private void method(CommandSender sender) {}
    }

    /**
     * Missing Command Sender Test:
     * Command Sender or Player must be the first argument
     */
    @Test
    public void missingSenderTest() {
        //MinestandParser.parseClass(MissingSender1.class);
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(MissingSender1.class));
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(MissingSender2.class));
    }

    @Alias("name")
    public static class MissingSender1 {
        @Alias("subcommand")
        public void method() {}
    }

    @Alias("name")
    public static class MissingSender2 {
        @Alias("subcommand")
        public void method(int notAPlayer) {}
    }

    /**
     * Unknown Type Test:
     * Unknown types in method aren't allowed
     */
    @Test
    public void unknownTypeTest() {
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(UnknownType.class));
    }

    @Alias("name")
    public static class UnknownType {
        @Alias("subcommand")
        public void method(CommandSender sender, UnknownType type) {}
    }

    /**
     * Two @DefaultCommand Test:
     * Only one method can be annotated with @DefaultCommand
     */
    @Test
    public void twoDefaultCommandTest() {
        assertThrows(CommandParseException.class, () -> MinestandParser.parseClass(TwoDefaultCommand.class));
    }

    @Alias("name")
    public static class TwoDefaultCommand {
        @DefaultCommand
        public void method(CommandSender sender) {}
        @DefaultCommand
        public void method2(CommandSender sender) {}
    }



}