/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command;

public class CommandArgumentValue extends CommandArgument {

    public CommandArgumentValue(String description, int index, CommandArgument[] subArguments, CommandAction commandAction) {
        super("", description, index, subArguments, commandAction);
    }

    public CommandArgumentValue(String description, int index, CommandAction commandAction) {
        super("", description, index, commandAction);
    }

    // Backward compatibility constructors
    public CommandArgumentValue(int index, CommandArgument[] subArguments, CommandAction commandAction) {
        super("", null, index, subArguments, commandAction);
    }

    public CommandArgumentValue(int index, CommandAction commandAction) {
        super("", null, index, commandAction);
    }
}
