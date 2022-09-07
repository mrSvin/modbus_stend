package de.re.easymodbus.exceptions;

public class ConnectionException extends ModbusException {
    public ConnectionException() {
    }

    public ConnectionException(String s) {
        super(s);
    }
}