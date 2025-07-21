/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command;

public class CommandArgumentValue extends CommandArgument {

    public CommandArgumentValue(int index, CommandArgument[] subArguments, CommandAction commandAction) {
        super("", index, subArguments, commandAction);
    }

    public CommandArgumentValue(int index, CommandAction commandAction) {
        super("", index, commandAction);
    }
}
