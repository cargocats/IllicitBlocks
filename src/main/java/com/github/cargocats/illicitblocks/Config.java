package com.github.cargocats.illicitblocks;

import java.util.ArrayList;

public class Config {
    public ArrayList<String> excluded_identifiers = new ArrayList<>();
    public ArrayList<String> excluded_namespaces = new ArrayList<>();
    public ArrayList<String> included_identifiers = new ArrayList<>();
    public boolean use_static_list = false;
    public boolean register_block_items = true;
    public ArrayList<String> static_list = new ArrayList<>();
}
