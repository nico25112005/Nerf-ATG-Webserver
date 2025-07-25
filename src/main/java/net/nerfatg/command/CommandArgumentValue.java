/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command;

public class CommandArgumentValue extends CommandArgument {

    public CommandArgumentValue(String description, int index, CommandAction commandAction, CommandArgument... subArguments) {
        super("", description, index, commandAction, subArguments);
    }

    public CommandArgumentValue(String description, int index, CommandAction commandAction) {
        super("", description, index, commandAction);
    }

    public CommandArgumentValue(int index, CommandAction commandAction) {
        super("", null, index, commandAction);
    }
}
