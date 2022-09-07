package gnu.io;

import java.io.FileDescriptor;
import java.util.Enumeration;
import java.util.Vector;

public class CommPortIdentifier {
    public static final int PORT_SERIAL = 1;
    public static final int PORT_PARALLEL = 2;
    public static final int PORT_I2C = 3;
    public static final int PORT_RS485 = 4;
    public static final int PORT_RAW = 5;
    private String PortName;
    private boolean Available = true;
    private String Owner;
    private CommPort commport;
    private CommDriver RXTXDriver;
    static CommPortIdentifier CommPortIndex;
    CommPortIdentifier next;
    private int PortType;
    private static final boolean debug = false;
    static Object Sync = new Object();
    Vector ownershipListener;
    private boolean HideOwnerEvents;

    CommPortIdentifier(String var1, CommPort var2, int var3, CommDriver var4) {
        this.PortName = var1;
        this.commport = var2;
        this.PortType = var3;
        this.next = null;
        this.RXTXDriver = var4;
    }

    public static void addPortName(String var0, int var1, CommDriver var2) {
        AddIdentifierToList(new CommPortIdentifier(var0, (CommPort)null, var1, var2));
    }

    private static void AddIdentifierToList(CommPortIdentifier var0) {
        synchronized(Sync) {
            if (CommPortIndex == null) {
                CommPortIndex = var0;
            } else {
                CommPortIdentifier var2;
                for(var2 = CommPortIndex; var2.next != null; var2 = var2.next) {
                }

                var2.next = var0;
            }

        }
    }

    public void addPortOwnershipListener(CommPortOwnershipListener var1) {
        if (this.ownershipListener == null) {
            this.ownershipListener = new Vector();
        }

        if (!this.ownershipListener.contains(var1)) {
            this.ownershipListener.addElement(var1);
        }

    }

    public String getCurrentOwner() {
        return this.Owner;
    }

    public String getName() {
        return this.PortName;
    }

    public static CommPortIdentifier getPortIdentifier(String var0) throws NoSuchPortException {
        CommPortIdentifier var1 = CommPortIndex;
        synchronized(Sync) {
            while(var1 != null && !var1.PortName.equals(var0)) {
                var1 = var1.next;
            }
        }

        if (var1 != null) {
            return var1;
        } else {
            throw new NoSuchPortException();
        }
    }

    public static CommPortIdentifier getPortIdentifier(CommPort var0) throws NoSuchPortException {
        CommPortIdentifier var1 = CommPortIndex;
        synchronized(Sync) {
            while(var1 != null && var1.commport != var0) {
                var1 = var1.next;
            }
        }

        if (var1 != null) {
            return var1;
        } else {
            throw new NoSuchPortException();
        }
    }

    public static Enumeration getPortIdentifiers() {
        CommPortIndex = null;

        try {
            CommDriver var0 = (CommDriver)Class.forName("gnu.io.RXTXCommDriver").newInstance();
            var0.initialize();
        } catch (Throwable var1) {
            System.err.println(var1 + " thrown while loading " + "gnu.io.RXTXCommDriver");
        }

        return new CommPortEnumerator();
    }

    public int getPortType() {
        return this.PortType;
    }

    public synchronized boolean isCurrentlyOwned() {
        return !this.Available;
    }

    public synchronized CommPort open(FileDescriptor var1) throws UnsupportedCommOperationException {
        throw new UnsupportedCommOperationException();
    }

    private native String native_psmisc_report_owner(String var1);

    public synchronized CommPort open(String var1, int var2) throws PortInUseException {
        if (!this.Available) {
            synchronized(Sync) {
                this.fireOwnershipEvent(3);

                try {
                    this.wait((long)var2);
                } catch (InterruptedException var6) {
                }
            }
        }

        if (!this.Available) {
            throw new PortInUseException(this.getCurrentOwner());
        } else {
            if (this.commport == null) {
                this.commport = this.RXTXDriver.getCommPort(this.PortName, this.PortType);
            }

            if (this.commport != null) {
                this.Owner = var1;
                this.Available = false;
                this.fireOwnershipEvent(1);
                return this.commport;
            } else {
                throw new PortInUseException(this.native_psmisc_report_owner(this.PortName));
            }
        }
    }

    public void removePortOwnershipListener(CommPortOwnershipListener var1) {
        if (this.ownershipListener != null) {
            this.ownershipListener.removeElement(var1);
        }

    }

    synchronized void internalClosePort() {
        this.Owner = null;
        this.Available = true;
        this.commport = null;
        this.notifyAll();
        this.fireOwnershipEvent(2);
    }

    void fireOwnershipEvent(int var1) {
        if (this.ownershipListener != null) {
            Enumeration var3 = this.ownershipListener.elements();

            while(var3.hasMoreElements()) {
                CommPortOwnershipListener var2 = (CommPortOwnershipListener)var3.nextElement();
                var2.ownershipChange(var1);
            }
        }

    }

    static {
        try {
            CommDriver var0 = (CommDriver)Class.forName("gnu.io.RXTXCommDriver").newInstance();
            var0.initialize();
        } catch (Throwable var1) {
            System.err.println(var1 + " thrown while loading " + "gnu.io.RXTXCommDriver");
        }

        String var2 = System.getProperty("os.name");
        if (var2.toLowerCase().indexOf("linux") == -1) {
        }

        System.loadLibrary("rxtxSerial");
    }
}
