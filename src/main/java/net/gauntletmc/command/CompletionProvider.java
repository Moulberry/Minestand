package net.gauntletmc.command;

import net.gauntletmc.command.exception.CommandParseException;
import net.gauntletmc.command.functional.CommandCompletion;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompletionProvider {

    private static final Map<String, String> lookup = new HashMap<>();
    private static final Map<String, CommandCompletion> completions = new HashMap<>();

    private static final Pattern METHOD_REF_PATTERN = Pattern.compile("([A-Za-z.$]+;)?([A-Za-z]+)");
    private static final Pattern FULL_METHOD_REF_PATTERN = Pattern.compile("[A-Za-z.$]+;([A-Za-z]+)");

    public static void createLookup(String lookup, String method) {
        if (!lookup.startsWith("@")) {
            throw new IllegalArgumentException("`lookup` must start with `@`");
        }

        Matcher matcher = FULL_METHOD_REF_PATTERN.matcher(method);
        if (!matcher.matches()) {
            throw new CommandParseException("Invalid full method reference, should look like `net.package.Class;method`");
        }

        CompletionProvider.lookup.put(lookup, method);
    }

    static CommandCompletion getCompletion(@NotNull Class<?> clazz, String completion) {
        if (completion.startsWith("@")) {
            String newCompletion = lookup.get(completion);
            if (newCompletion == null) {
                throw new CommandParseException("Unknown completion lookup: " + completion);
            }
            completion = newCompletion;
        }

        if (completion.contains(";") && completions.containsKey(completion)) {
            return completions.get(completion);
        }

        Matcher matcher = METHOD_REF_PATTERN.matcher(completion);
        if (!matcher.matches()) {
            throw new CommandParseException("Invalid method reference, should look like `method` or `net.package.Class;method`");
        }

        String groupClass = matcher.group(1);
        if (groupClass != null) {
            groupClass = groupClass.replace(";", "");
            try {
                clazz = Class.forName(groupClass);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        groupClass = clazz.getName();

        String methodName = matcher.group(2);
        if (methodName == null) {
            throw new CommandParseException("Method name was null?");
        }

        // Return already created CommandCompletion
        completion = groupClass+";"+methodName;
        if (completions.containsKey(completion)) {
            return completions.get(completion);
        }

        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, CommandSender.class, CommandContext.class);
        } catch (NoSuchMethodException e) {
            throw new CommandParseException(e);
        }

        if ((method.getModifiers() & Modifier.STATIC) == 0) {
            throw new CommandParseException("Completion method must be static");
        }

        if (method.getReturnType() != Collection.class) {
            throw new CommandParseException("Completion method must return collection");
        }

        try {
            CommandCompletion commandCompletion = FastInvokerFactory.createCompletionProvider(method);
            completions.put(completion, commandCompletion);
            return commandCompletion;
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
