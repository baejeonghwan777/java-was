package enumfile;

public enum PathIndex {
    METHOD_INDEX(0),
    URL_INDEX(1),
    MAX_INDEX(3);

    private final int index;

    PathIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
