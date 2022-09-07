package gnu.io;

import java.util.EventObject;

public class SerialPortEvent extends EventObject {
    public static final int DATA_AVAILABLE = 1;
    public static final int OUTPUT_BUFFER_EMPTY = 2;
    public static final int CTS = 3;
    public static final int DSR = 4;
    public static final int RI = 5;
    public static final int CD = 6;
    public static final int OE = 7;
    public static final int PE = 8;
    public static final int FE = 9;
    public static final int BI = 10;
    private boolean OldValue;
    private boolean NewValue;
    private int eventType;

    public SerialPortEvent(SerialPort var1, int var2, boolean var3, boolean var4) {
        super(var1);
        this.OldValue = var3;
        this.NewValue = var4;
        this.eventType = var2;
    }

    public int getEventType() {
        return this.eventType;
    }

    public boolean getNewValue() {
        return this.NewValue;
    }

    public boolean getOldValue() {
        return this.OldValue;
    }
}
