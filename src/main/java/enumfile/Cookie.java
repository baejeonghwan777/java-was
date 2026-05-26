package enumfile;

public enum Cookie {
    COOKIE_VALUE_INDEX(1);

    private final int index;

    Cookie(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
