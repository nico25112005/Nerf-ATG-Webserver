/*
	Author: Elias (Gotzi) Gottsbacher
	Copyright (c) 2023 Elias Gottsbacher
*/

package net.nerfatg.command;

import jline.console.completer.Completer;

import java.util.List;

/**
 * Simple test completer to verify jline completion is working
 */
public class SimpleCompleter implements Completer {
    
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        System.out.println("\n[SIMPLE COMPLETER] Called with buffer: '" + buffer + "', cursor: " + cursor);
        
        // Add some simple test completions
        candidates.add("test1");
        candidates.add("test2");
        candidates.add("help");
        candidates.add("version");
        
        System.out.println("[SIMPLE COMPLETER] Added candidates: " + candidates);
        
        return 0;
    }
}