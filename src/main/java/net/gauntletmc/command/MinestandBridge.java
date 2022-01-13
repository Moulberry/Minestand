package net.gauntletmc.command;

import net.gauntletmc.command.annotations.Completions;
import net.gauntletmc.command.annotations.Max;
import net.gauntletmc.command.annotations.Min;
import net.gauntletmc.command.annotations.Optional;
import net.gauntletmc.command.arguments.ArgumentOptional;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.function.Supplier;

class MinestandBridge {

    static Argument<?> createArgument(Class<?> clazz, Parameter parameter, String name) {
        Argument<?> arg = MinestomArgumentFactory.getForClass(clazz, parameter, name);

        // Min & Max
        if (arg instanceof ArgumentNumber<?> number) {
            applyMinMax(number, parameter);
        }

        if (parameter.isAnnotationPresent(Optional.class)) {
            //((ArgumentBoolean)arg).setDefaultValue(false);
            arg = new ArgumentOptional<>(arg);
            ((ArgumentOptional)arg).setDefaultValue(createDefault(clazz));
        }

        final Argument<?> argF = arg;

        // Completions
        if (parameter.isAnnotationPresent(Completions.class)) {
            Completions completions = parameter.getAnnotation(Completions.class);
            final String completionId = completions.value();
            if (!Minestand.COMPLETIONS.containsKey(completionId)) {
                throw new RuntimeException("Completion `"+completionId+"` referenced before registration");
            }
            argF.setSuggestionCallback((sender, context, suggestion) -> {
                String start = context.getRaw(argF);
                for (String completion : Minestand.COMPLETIONS.get(completionId)) {
                    if (start == null || completion.startsWith(start)) {
                        suggestion.getEntries().add(new SuggestionEntry(completion));
                    }
                }
            });
        }

        argF.setCallback((sender, exception) -> {
            Minestand.ERROR_MESSAGE_HANDLER.accept(sender, "Incorrect argument for command, see below for error");
            Minestand.ERROR_MESSAGE_HANDLER.accept(sender, exception.getMessage());
        });

        return argF;
    }

    private static final Supplier<Object> DEFAULT_OBJECT = () -> null;
    private static final Supplier<Object> DEFAULT_BOOLEAN = () -> false;
    private static final Supplier<Object> DEFAULT_NUMBER = () -> 0;
    private static Supplier<Object> createDefault(Class<?> clazz) {
        if (clazz == boolean.class) {
            return DEFAULT_BOOLEAN;
        } else if (clazz == double.class || clazz == float.class ||
                clazz == int.class || clazz == long.class) {
            return DEFAULT_NUMBER;
        }
        return DEFAULT_OBJECT;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> void applyMinMax(ArgumentNumber<T> arg, AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(Min.class)) {
            Min min = annotatedElement.getAnnotation(Min.class);
            arg.min((T) (Float) min.value());
        }
        if (annotatedElement.isAnnotationPresent(Max.class)) {
            Max max = annotatedElement.getAnnotation(Max.class);
            arg.min((T) (Float) max.value());
        }
    }

    static Command createCommand(MinestandParser.ParsedClass parsed) {
        Command command = new Command(parsed.names()[0], parsed.names());
        setDefaultCommandAttributes(command, parsed.requiredPermission());

        // Add subcommands
        for (MinestandParser.ParsedClass subclass : parsed.subclasses()) {
            command.addSubcommand(createCommand(subclass));
        }
        // Add methods
        for (MinestandParser.ParsedMethod method : parsed.methods()) {
            Command subcommand = new Command(method.names()[0], method.names());
            addExecutable(subcommand, method.executable());
            setDefaultCommandAttributes(subcommand, method.requiredPermission());
            command.addSubcommand(subcommand);
        }
        // Add default command
        if (parsed.defaultCommand() != null) {
            addExecutable(command, parsed.defaultCommand());
        }

        return command;
    }

    private static void setDefaultCommandAttributes(Command command, String requiredPermission) {
        if (requiredPermission != null) {
            command.setCondition((sender, string) -> Minestand.PERMISSION_HANDLER.apply(sender, requiredPermission));
        }
        command.setDefaultExecutor((sender, context) -> {
            Minestand.ERROR_MESSAGE_HANDLER.accept(sender, "Unknown or incomplete command, see below for error");
            Minestand.ERROR_MESSAGE_HANDLER.accept(sender, "\u00a77/"+context.getInput().trim()+"\u00a7r<--[HERE]");
        });
    }

    static void addExecutable(Command command, MinestandParser.ExecutableCommand executable) {
        command.addSyntax(executable.executor(), executable.arguments());
    }

}
