/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command.commands;

import net.nerfatg.command.Command;
import net.nerfatg.command.CommandArgument;
import net.nerfatg.command.CommandArgumentValue;
import net.nerfatg.command.CommandContext;

public class VersionCommand extends Command {

    public VersionCommand(String label) {
        super(label);

        init();
    }

    public void init() {
        setNativeAction(this::nativeAction);

        addArgument(
                new CommandArgument("task1", 0, new CommandArgument[]{
                        new CommandArgument("subtask", 1, (ctx) -> {

                        }),
                        new CommandArgument("subitask", 1, (ctx) -> {

                        })
                }, (ctx) -> {
                    System.out.println("Called only task1");
                })
        );
    }

    private void nativeAction(CommandContext gCommandContext) {
        System.out.println("Version command started");
    }
}