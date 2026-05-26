package enumfile;

public enum HeaderIndex {
    KEY_INDEX(0),
    VALUE_INDEX(1),
    MAX_INDEX(2);

    private final int index;

    HeaderIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}