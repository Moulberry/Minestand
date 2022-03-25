package net.gauntletmc.command;

import net.gauntletmc.command.annotations.Alias;
import net.gauntletmc.command.annotations.DefaultCommand;
import net.gauntletmc.command.annotations.Name;
import net.gauntletmc.command.annotations.RequiresPermission;
import net.gauntletmc.command.exception.CommandParseException;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MinestandParser {

    record ParsedClass(String[] names, @Nullable String requiredPermission,
                       @Nullable ExecutableCommand defaultCommand, @Nullable String defaultCommandPermission,
                       List<ParsedClass> subclasses, List<ParsedMethod> methods) {}
    record ParsedMethod(String[] names, @Nullable String requiredPermission, ExecutableCommand executable) {}
    record ExecutableCommand(Argument<?>[] arguments, CommandExecutor executor) {}

    static ParsedClass parseClass(Class<?> clazz) {
        return parseClass(clazz, null);
    }

    static ParsedClass parseClass(Class<?> clazz, Object declaring) {
        Alias aliasAnnotation = clazz.getAnnotation(Alias.class);
        if (aliasAnnotation == null) {
            throw new CommandParseException("Commands must include the @Alias annotation");
        }

        checkAnnotation(null, aliasAnnotation);

        boolean allStatic = true;
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Alias.class) &&
                    !method.isAnnotationPresent(DefaultCommand.class)) continue;
            if ((method.getModifiers() & Modifier.STATIC) == 0) { // if (not_static)
                allStatic = false;
                break;
            }
        }
        for (Class<?> subclass : clazz.getDeclaredClasses()) {
            if (!subclass.isAnnotationPresent(Alias.class)) continue;
            if ((subclass.getModifiers() & Modifier.STATIC) == 0) { // if (not_static)
                allStatic = false;
                break;
            }
        }

        Object object = null;
        if (!allStatic) {
            try {
                if ((clazz.getModifiers() & Modifier.STATIC) == 0) { // if (not_static)
                    if (clazz.getDeclaringClass() == null) {
                        if (declaring != null) {
                            throw new CommandParseException("Non-static inner class " + clazz +
                                    " doesn't have a declaring class, but was provided a declaring object");
                        }
                        object = clazz.getConstructor().newInstance();
                    } else {
                        if (declaring == null || declaring.getClass() != clazz.getDeclaringClass()) {
                            throw new CommandParseException("Non-static inner class " + clazz +
                                    " must be given an instance of the declaring class");
                        }
                        object = clazz.getConstructor(clazz.getDeclaringClass()).newInstance(declaring);
                    }
                } else {
                    object = clazz.getConstructor().newInstance();
                }
            } catch(NoSuchMethodException | IllegalAccessException e) {
                throw new CommandParseException("Command class must have a public no-args constructor", e);
            } catch(InstantiationException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        Set<String> names = new HashSet<>();

        List<ParsedClass> subclasses = new ArrayList<>();
        for (Class<?> subclass : clazz.getDeclaredClasses()) {
            if (!subclass.isAnnotationPresent(Alias.class)) continue;
            checkAnnotation(names, subclass.getAnnotation(Alias.class));
            subclasses.add(parseClass(subclass, object));
        }

        ExecutableCommand defaultCommand = null;
        String defaultCommandPermission = null;
        List<ParsedMethod> methods = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DefaultCommand.class)) {
                if (defaultCommand != null) {
                    throw new CommandParseException("Only one method can be annotated with @DefaultCommand");
                }
                methodSanityCheck(method, true);

                defaultCommand = createExecutable(object, clazz, method);
                if (method.isAnnotationPresent(RequiresPermission.class)) {
                    defaultCommandPermission = method.getAnnotation(RequiresPermission.class).value();
                }
            } else {
                Alias subcommandAnnotation = method.getAnnotation(Alias.class);
                methodSanityCheck(method, subcommandAnnotation != null);

                if (subcommandAnnotation == null) continue;

                checkAnnotation(names, subcommandAnnotation);

                String requiredPermission = null;
                if (method.isAnnotationPresent(RequiresPermission.class)) {
                    requiredPermission = method.getAnnotation(RequiresPermission.class).value();
                }

                methods.add(new ParsedMethod(subcommandAnnotation.value(), requiredPermission, createExecutable(object, clazz, method)));
            }
        }
        String requiredPermission = null;
        if (clazz.isAnnotationPresent(RequiresPermission.class)) {
            requiredPermission = clazz.getAnnotation(RequiresPermission.class).value();
        }

        return new ParsedClass(aliasAnnotation.value(), requiredPermission, defaultCommand, defaultCommandPermission, subclasses, methods);
    }

    private static ExecutableCommand createExecutable(Object object, Class<?> clazz, Method method) {
        Class<?>[] classes = method.getParameterTypes();
        Parameter[] parameters = method.getParameters();

        Argument<?>[] arguments = new Argument[parameters.length-1];
        String[] names = new String[parameters.length-1];

        for (int i=0; i<arguments.length; i++) {
            Parameter parameter = parameters[i+1];
            String name = parameter.getName();
            if (parameter.isAnnotationPresent(Name.class)) {
                name = parameter.getAnnotation(Name.class).value();
            }

            names[i] = name;
            arguments[i] = MinestandBridge.createArgument(classes[i+1], object.getClass(), parameters[i+1], name);
        }

        CommandExecutor executor;
        try {
            executor = FastInvokerFactory.createCommandExecutor(object, clazz, method, names);
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }

        return new ExecutableCommand(arguments, executor);
    }

    private static void checkAnnotation(@Nullable Set<String> names, Alias annotation) {
        if (annotation.value().length <= 0) {
            throw new CommandParseException("An empty array is not a valid command name");
        }
        for (String name : annotation.value()) {
            if (name.contains(" ")) throw new CommandParseException("Command names must not contain ` `");
        }

        if (names != null) {
            for (String name : annotation.value()) {
                if (names.contains(name)) throw new CommandParseException("Duplicate command name: "+name);
                names.add(name);
            }
        }
    }

    private static void methodSanityCheck(Method method, boolean hasAnnotation) {
        boolean isPrivate = (method.getModifiers() & Modifier.PRIVATE) != 0;

        if (!hasAnnotation) {
            if (!isPrivate && !method.isSynthetic() && method.getReturnType() == void.class) {
                throw new CommandParseException("Public method in command class is missing @Alias annotation");
            }
            return;
        } else {
            if (isPrivate) {
                throw new CommandParseException("Methods annotated with @Alias must also be public");
            }
        }

        Class<?>[] parameters = method.getParameterTypes();

        if (parameters.length <= 0) {
            throw new CommandParseException("Method annotated with @Alias is missing `Player` or `CommandSender` as first argument");
        }

        if (parameters[0] != Player.class && parameters[0] != CommandSender.class) {
            throw new CommandParseException("Method annotated with @Alias must have `Player` or `CommandSender` as first argument");
        }
    }

}
