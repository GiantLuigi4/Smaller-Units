package test.test;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class smallUnit implements Comparable<smallUnit> {
    public Block bk;
    public int sc;
    public BlockPos pos;
    public BlockPos sPos;
    public int meta;
    public smallUnit(Block bk, int sc, int meta, BlockPos pos, BlockPos sPos) {
        this.bk = bk;
        this.sc = sc;
        this.pos = pos;
        this.sPos = sPos;
        this.meta = meta;
    }
    @Override
    public boolean equals(Object obj) {
//        if (bk.equals(((smallUnit)obj).bk)) {
            if (sc==(((smallUnit)obj).sc)) {
                if (pos.equals(((smallUnit)obj).pos)) {
                    if (sPos.equals(((smallUnit)obj).sPos)) {
                        return true;
                    }
                }
            }
//        }
        return false;
    }
    @Override
    public int compareTo(smallUnit o) {
        if (this.equals(o)) {
            return 1;
        }
        return 0;
    }
}
