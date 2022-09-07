package de.re.easymodbus.exceptions;

public class FunctionCodeNotSupportedException extends ModbusException {
    public FunctionCodeNotSupportedException() {
    }

    public FunctionCodeNotSupportedException(String s) {
        super(s);
    }
}
