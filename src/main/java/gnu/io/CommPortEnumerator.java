
package gnu.io;

import java.util.Enumeration;

class CommPortEnumerator implements Enumeration {
    private CommPortIdentifier index;
    private static final boolean debug = false;

    CommPortEnumerator() {
    }

    public Object nextElement() {
        synchronized(CommPortIdentifier.Sync) {
            if (this.index != null) {
                this.index = this.index.next;
            } else {
                this.index = CommPortIdentifier.CommPortIndex;
            }

            return this.index;
        }
    }

    public boolean hasMoreElements() {
        synchronized(CommPortIdentifier.Sync) {
            if (this.index != null) {
                return this.index.next != null;
            } else {
                return CommPortIdentifier.CommPortIndex != null;
            }
        }
    }
}