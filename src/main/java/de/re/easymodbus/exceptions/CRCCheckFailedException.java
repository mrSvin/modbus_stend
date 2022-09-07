package de.re.easymodbus.exceptions;

public class CRCCheckFailedException extends ModbusException {
    public CRCCheckFailedException() {
    }

    public CRCCheckFailedException(String s) {
        super(s);
    }
}
