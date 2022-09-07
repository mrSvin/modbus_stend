package gnu.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class CommPort {
    protected String name;
    private static final boolean debug = false;

    public CommPort() {
    }

    public abstract void enableReceiveFraming(int var1) throws UnsupportedCommOperationException;

    public abstract void disableReceiveFraming();

    public abstract boolean isReceiveFramingEnabled();

    public abstract int getReceiveFramingByte();

    public abstract void disableReceiveTimeout();

    public abstract void enableReceiveTimeout(int var1) throws UnsupportedCommOperationException;

    public abstract boolean isReceiveTimeoutEnabled();

    public abstract int getReceiveTimeout();

    public abstract void enableReceiveThreshold(int var1) throws UnsupportedCommOperationException;

    public abstract void disableReceiveThreshold();

    public abstract int getReceiveThreshold();

    public abstract boolean isReceiveThresholdEnabled();

    public abstract void setInputBufferSize(int var1);

    public abstract int getInputBufferSize();

    public abstract void setOutputBufferSize(int var1);

    public abstract int getOutputBufferSize();

    public void close() {
        try {
            CommPortIdentifier var1 = CommPortIdentifier.getPortIdentifier(this);
            if (var1 != null) {
                CommPortIdentifier.getPortIdentifier(this).internalClosePort();
            }
        } catch (NoSuchPortException var2) {
        }

    }

    public abstract InputStream getInputStream() throws IOException;

    public abstract OutputStream getOutputStream() throws IOException;

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }
}