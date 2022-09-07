package gnu.io;

public interface CommDriver {
    CommPort getCommPort(String var1, int var2);

    void initialize();
}
