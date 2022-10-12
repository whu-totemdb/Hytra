package edu.whu.hyk.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PostingList {
    //cid -> String, tid -> int, pid -> int, gid -> int
    public static HashMap<String, HashSet<Integer>> CT = new HashMap<>();

    public static HashMap<String, HashSet<Integer>> mergeCT = new HashMap<>();

    //no merge
    public static HashMap<String, HashSet<Integer>> CP = new HashMap<>();

    // with merging
    public static HashMap<String, HashSet<Integer>> mergeCP = new HashMap<>();

    public static HashMap<Integer, List<String>> TC = new HashMap<>();

    public static HashMap<Integer, List<String>> mergeTC = new HashMap<>();

    public static HashMap<Integer, List<Integer>> TP = new HashMap<>();

    public static HashMap<Integer, HashSet<Integer>> GT = new HashMap<>();

    public static HashMap<Integer, Integer> TlP = new HashMap<>();



}
