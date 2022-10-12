package edu.whu.hyk.encoding;

public class Decoder extends Encoder{


    /**
     * 针对xz2+的实现，以gid作为左下角，每个round大小扩张四倍，返回右上角的gid
     * @param gid
     * @param round
     * @return
     */
    public static int enlargeGrid(int gid, int round) {
        int[] ij = decodeZ2(gid);
        int delta = (int) Math.pow(2,round) - 1;
        return combine2(ij[0] + delta, ij[1] + delta,RESOLUTION * 2);
    }

    /**
     * 将二维z-order解析为（i，j）
     * @param zorder
     * @return
     */
    public static int[] decodeZ2(int zorder) {
        int digits =  2 * RESOLUTION;
        String bits = Integer.toBinaryString(zorder);
        while(digits > bits.length()) {
            bits = "0" + bits;
        }
        String bitsI = "", bitsJ = "";
        for(int i = 0; i < bits.length(); i++){
            //偶数位是i的bits
            if((i & 1) == 0) {bitsI += bits.charAt(i);}
            //奇数位是j的bits
            else{bitsJ += bits.charAt(i);}
        }

        return new int[] {bitToint(bitsI), bitToint(bitsJ)};
    }

    /**
     * 将一个大cube（zorder+level）转换为最小level范围
     * @param zorder
     * @param level
     * @return [i1,i2,j1,j2,k1,k2]
     */
    public static int[] decodeZ3(int zorder, int level) {
        int digits =  3 * RESOLUTION;
        String bits = Integer.toBinaryString(zorder);
        while(digits > bits.length()) {
            bits = "0" + bits;
        }
        String bitsI = "", bitsJ = "",bitsK = "";
        for(int i = 0; i < bits.length(); i++){
            if((i % 3)== 0) {bitsK += bits.charAt(i);}
            if((i % 3) == 1) {bitsJ += bits.charAt(i);}
            if((i % 3) == 2) {bitsI += bits.charAt(i);}
        }
        int I = bitToint(bitsI),J = bitToint(bitsJ),K = bitToint(bitsK);
        int i1 = I * (int) Math.pow(8,level), i2 = i1 + (int) Math.pow(8,level) - 1,
                j1 = J * (int) Math.pow(8,level), j2 = j1 + (int) Math.pow(8,level) - 1,
                k1 = K * (int) Math.pow(8,level), k2 = k1 + (int) Math.pow(8,level) - 1;

        return new int[]{i1,i2,j1,j2,k1,k2};



    }

}
