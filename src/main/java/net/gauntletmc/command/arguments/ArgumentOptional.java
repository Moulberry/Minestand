package net.gauntletmc.command.arguments;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ArgumentOptional<T> extends Argument<T> {

    private final Argument<T> argument;

    public ArgumentOptional(Argument<T> argument) {
        super(argument.getId(), true, true);
        this.argument = argument;
    }

    @Override
    public @NotNull T parse(@NotNull String input) throws ArgumentSyntaxException {
        return argument.parse(input);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        nodeMaker.getLatestNodes()[nodeMaker.getLatestNodes().length-1].flags |= 0x04;
        argument.processNodes(nodeMaker, executable);
    }
}
