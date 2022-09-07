package gnu.io;

import java.util.EventListener;

public interface SerialPortEventListener extends EventListener {
    void serialEvent(SerialPortEvent var1);
}
