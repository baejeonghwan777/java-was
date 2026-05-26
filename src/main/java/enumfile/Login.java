package enumfile;

public enum Login {
    UNDEFINED(0),
    LOGIN_SUCCESS(1),
    LOGIN_FAIL(2);

    private final int flag;

    Login(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
