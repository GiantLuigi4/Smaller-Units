package test.test;

public class placeEvent {
    public String blockPos="";
    public String item="";
    public int meta=0;
    public int slot=0;

    public placeEvent(String blockPos, String item, int meta, int slot) {
        this.blockPos = blockPos;
        this.item = item;
        this.meta = meta;
        this.slot = slot;
    }
}
