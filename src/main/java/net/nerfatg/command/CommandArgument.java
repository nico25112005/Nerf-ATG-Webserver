/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CommandArgument implements Comparable<CommandArgument> {
    private SortedSet<CommandArgument> subArguments;
    private final String label;
    private final int index;
    private CommandAction commandAction;

    public CommandArgument(String label, int index, CommandArgument[] subArguments, CommandAction commandAction) {
        this.label = label;
        this.index = index;
        this.subArguments = new TreeSet<>(List.of(subArguments));
        this.commandAction = commandAction;
    }

    public CommandArgument(String label, int index, CommandAction commandAction) {
        this.label = label;
        this.index = index;
        this.commandAction = commandAction;
    }

    public void setSubCommands(SortedSet<CommandArgument> subArguments) {
        this.subArguments = subArguments;
    }

    public void setCommandAction(CommandAction commandAction) {
        this.commandAction = commandAction;
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }

    public SortedSet<CommandArgument> getSubCommands() {
        return subArguments;
    }

    public CommandAction getCommandAction() {
        return commandAction;
    }

    @Override
    public int compareTo(CommandArgument o) {
        return getLabel().compareTo(o.getLabel());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CommandArgument arg)
            return getLabel().equals(arg.getLabel());

        return false;
    }

    @Override
    public String toString() {
        return getLabel().equals("") ? "!val" : getLabel();
    }
}