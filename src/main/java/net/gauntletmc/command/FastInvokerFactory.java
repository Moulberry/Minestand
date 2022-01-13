package net.gauntletmc.command;

import net.gauntletmc.command.consumers.NConsumer;
import net.gauntletmc.command.exception.CommandParseException;
import net.gauntletmc.command.functional.CommandCompletion;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

import static net.gauntletmc.command.SenderTypeConverter.adapt;

class FastInvokerFactory {

    private static NConsumer createConsumer(Object bindTo, Class<?> bindToClass, Method method) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handle = lookup.unreflect(method);

        Class<?>[] erasedTypes = new Class[method.getParameterCount()];
        Arrays.fill(erasedTypes, Object.class);

        Class<?>[] fixedTypes = new Class[method.getParameterCount()];
        int index = 0;
        for (Class<?> clazz : method.getParameterTypes()) {
            fixedTypes[index++] = convertPrimitive(clazz);
        }

        boolean staticMethod = (method.getModifiers() & Modifier.STATIC) != 0;

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "accept"+method.getParameterCount(),
                MethodType.methodType(NConsumer.class, staticMethod ? new Class[0] : new Class[]{bindToClass}),
                MethodType.methodType(void.class, erasedTypes),
                handle,
                MethodType.methodType(void.class, fixedTypes));
        if (staticMethod) {
            return (NConsumer) callSite.getTarget().invoke();
        } else {
            return (NConsumer) callSite.getTarget().bindTo(bindTo).invoke();
        }
    }

    static CommandCompletion createCompletionProvider(Method method) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handle = lookup.unreflect(method);

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "apply",
                MethodType.methodType(CommandCompletion.class),
                MethodType.methodType(Collection.class, CommandSender.class, CommandContext.class),
                handle,
                MethodType.methodType(Collection.class, CommandSender.class, CommandContext.class));
        return (CommandCompletion) callSite.getTarget().invoke();
    }

    private static Class<?> convertPrimitive(Class<?> in) {
        if (in == int.class) {
            return Integer.class;
        } else if (in == float.class) {
            return Float.class;
        } else if (in == boolean.class) {
            return Boolean.class;
        } else if (in == double.class) {
            return Double.class;
        } else if (in == long.class) {
            return Long.class;
        } else {
            return in;
        }
    }

    @SuppressWarnings("unchecked")
    static CommandExecutor createCommandExecutor(Object bindTo, Class<?> bindToClass, Method method, String... argNames) throws Throwable {
        NConsumer consumer = createConsumer(bindTo, bindToClass, method);

        return switch (method.getParameterCount()) {
            case 1 -> (sender, context) -> {
                consumer.accept1(sender);
            };
            case 2 -> (sender, context) -> {
                consumer.accept2(sender, adapt(sender, context.get(argNames[0])));
            };
            case 3 -> (sender, context) -> {
                consumer.accept3(sender, adapt(sender, context.get(argNames[0])), adapt(sender, context.get(argNames[1])));
            };
            case 4 -> (sender, context) -> {
                consumer.accept4(sender, adapt(sender, context.get(argNames[0])), adapt(sender, context.get(argNames[1])),
                        adapt(sender, context.get(argNames[2])));
            };
            case 5 -> (sender, context) -> {
                consumer.accept5(sender, adapt(sender, context.get(argNames[0])), adapt(sender, context.get(argNames[1])),
                        adapt(sender, context.get(argNames[2])), adapt(sender, context.get(argNames[3])));
            };
            case 6 -> (sender, context) -> {
                consumer.accept6(sender, adapt(sender, context.get(argNames[0])), adapt(sender, context.get(argNames[1])),
                        adapt(sender, context.get(argNames[2])), adapt(sender, context.get(argNames[3])),
                        adapt(sender, context.get(argNames[4])));
            };
            case 7 -> (sender, context) -> {
                consumer.accept7(sender, adapt(sender, context.get(argNames[0])), adapt(sender, context.get(argNames[1])),
                        adapt(sender, context.get(argNames[2])), adapt(sender, context.get(argNames[3])),
                        adapt(sender, context.get(argNames[4])), adapt(sender, context.get(argNames[5])));
            };
            default -> throw new CommandParseException("Unsupported parameter count: " + method.getParameterCount());
        };
    }

}
