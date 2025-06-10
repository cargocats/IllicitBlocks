package com.github.cargocats.illicitblocks;

import java.util.ArrayList;

public class Config {
    public ArrayList<String> excluded_identifiers = new ArrayList<>();
    public ArrayList<String> excluded_namespaces = new ArrayList<>();
    public ArrayList<String> included_identifiers = new ArrayList<>();
    public boolean create_list_after_freeze = true;
    public boolean debug = true;
    public ArrayList<String> modded_block_list = new ArrayList<>();
}
