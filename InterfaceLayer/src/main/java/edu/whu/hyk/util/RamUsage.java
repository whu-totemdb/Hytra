package edu.whu.hyk.util;

import org.apache.lucene.util.RamUsageEstimator;

public class RamUsage {
   public static long getRamUsage (Object obj) {
       return RamUsageEstimator.sizeOf(obj);
   }

    public static void main(String[] args) {
       int mu = 7;
       int size = (int) (Math.pow(8,mu+1)-1)/7;
       int[] test = new int[size];
       System.out.println(getRamUsage(test)/(1024*1024));
    }
}
