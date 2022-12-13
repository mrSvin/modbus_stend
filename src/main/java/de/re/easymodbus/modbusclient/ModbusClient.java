package de.re.easymodbus.modbusclient;

import de.re.easymodbus.exceptions.ConnectionException;
import de.re.easymodbus.exceptions.FunctionCodeNotSupportedException;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.exceptions.QuantityInvalidException;
import de.re.easymodbus.exceptions.StartingAddressInvalidException;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModbusClient {
    private Socket tcpClientSocket = new Socket();
    protected String ipAddress = "190.201.100.100";
    protected int port = 502;
    private byte[] transactionIdentifier = new byte[2];
    private byte[] protocolIdentifier = new byte[2];
    private byte[] length = new byte[2];
    private byte[] crc = new byte[2];
    private byte unitIdentifier = 1;
    private byte functionCode;
    private byte[] startingAddress = new byte[2];
    private byte[] quantity = new byte[2];
    private boolean udpFlag = false;
    private boolean serialflag = false;
    private int connectTimeout = 500;
    private InputStream inStream;
    private DataOutputStream outStream;
    public byte[] receiveData;
    public byte[] sendData;
    private List<ReceiveDataChangedListener> receiveDataChangedListener = new ArrayList();
    private List<SendDataChangedListener> sendDataChangedListener = new ArrayList();
    private SerialPort serialPort;
    OutputStream out;
    InputStream in;
    CommPortIdentifier portIdentifier;

    public ModbusClient(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public ModbusClient() {
    }

    public void Connect() throws UnknownHostException, IOException {
        if (!this.udpFlag) {
            this.tcpClientSocket.setSoTimeout(this.connectTimeout);
            this.tcpClientSocket = new Socket(this.ipAddress, this.port);
            this.outStream = new DataOutputStream(this.tcpClientSocket.getOutputStream());
            this.inStream = this.tcpClientSocket.getInputStream();
        }

    }

    public void Connect(String ipAddress, int port) throws UnknownHostException, IOException {
        this.ipAddress = ipAddress;
        this.port = port;
        this.tcpClientSocket.setSoTimeout(this.connectTimeout);
        this.tcpClientSocket = new Socket(ipAddress, port);
        this.outStream = new DataOutputStream(this.tcpClientSocket.getOutputStream());
        this.inStream = this.tcpClientSocket.getInputStream();
    }

    public void Connect(String comPort) throws Exception {
        this.portIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
        if (this.portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            int timeout = 2000;
            CommPort commPort = this.portIdentifier.open(this.getClass().getName(), timeout);
            if (commPort instanceof SerialPort) {
                this.serialPort = (SerialPort)commPort;
                this.serialPort.setSerialPortParams(9600, 8, 1, 2);
                this.in = this.serialPort.getInputStream();
                this.out = this.serialPort.getOutputStream();
                this.serialflag = true;
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }

    }

    public static float ConvertRegistersToFloat(int[] registers) throws IllegalArgumentException {
        if (registers.length != 2) {
            throw new IllegalArgumentException("Input Array length invalid");
        } else {
            int highRegister = registers[1];
            int lowRegister = registers[0];
            byte[] highRegisterBytes = toByteArray(highRegister);
            byte[] lowRegisterBytes = toByteArray(lowRegister);
            byte[] floatBytes = new byte[]{highRegisterBytes[1], highRegisterBytes[0], lowRegisterBytes[1], lowRegisterBytes[0]};
            return ByteBuffer.wrap(floatBytes).getFloat();
        }
    }

    public static float ConvertRegistersToFloat(int[] registers, ModbusClient.RegisterOrder registerOrder) throws IllegalArgumentException {
        int[] swappedRegisters = new int[]{registers[0], registers[1]};
        if (registerOrder == ModbusClient.RegisterOrder.HighLow) {
            swappedRegisters = new int[]{registers[1], registers[0]};
        }

        return ConvertRegistersToFloat(swappedRegisters);
    }

    public static int ConvertRegistersToDouble(int[] registers) throws IllegalArgumentException {
        if (registers.length != 2) {
            throw new IllegalArgumentException("Input Array length invalid");
        } else {
            int highRegister = registers[1];
            int lowRegister = registers[0];
            byte[] highRegisterBytes = toByteArray(highRegister);
            byte[] lowRegisterBytes = toByteArray(lowRegister);
            byte[] doubleBytes = new byte[]{highRegisterBytes[1], highRegisterBytes[0], lowRegisterBytes[1], lowRegisterBytes[0]};
            return ByteBuffer.wrap(doubleBytes).getInt();
        }
    }

    public static int ConvertRegistersToDouble(int[] registers, ModbusClient.RegisterOrder registerOrder) throws IllegalArgumentException {
        int[] swappedRegisters = new int[]{registers[0], registers[1]};
        if (registerOrder == ModbusClient.RegisterOrder.HighLow) {
            swappedRegisters = new int[]{registers[1], registers[0]};
        }

        return ConvertRegistersToDouble(swappedRegisters);
    }

    public static int[] ConvertFloatToTwoRegisters(float floatValue) {
        byte[] floatBytes = toByteArray(floatValue);
        byte[] highRegisterBytes = new byte[]{0, 0, floatBytes[0], floatBytes[1]};
        byte[] lowRegisterBytes = new byte[]{0, 0, floatBytes[2], floatBytes[3]};
        int[] returnValue = new int[]{ByteBuffer.wrap(lowRegisterBytes).getInt(), ByteBuffer.wrap(highRegisterBytes).getInt()};
        return returnValue;
    }

    public static int[] ConvertFloatToTwoRegisters(float floatValue, ModbusClient.RegisterOrder registerOrder) {
        int[] registerValues = ConvertFloatToTwoRegisters(floatValue);
        int[] returnValue = registerValues;
        if (registerOrder == ModbusClient.RegisterOrder.HighLow) {
            returnValue = new int[]{registerValues[1], registerValues[0]};
        }

        return returnValue;
    }

    public static int[] ConvertDoubleToTwoRegisters(int doubleValue) {
        byte[] doubleBytes = toByteArrayDouble(doubleValue);
        byte[] highRegisterBytes = new byte[]{0, 0, doubleBytes[0], doubleBytes[1]};
        byte[] lowRegisterBytes = new byte[]{0, 0, doubleBytes[2], doubleBytes[3]};
        int[] returnValue = new int[]{ByteBuffer.wrap(lowRegisterBytes).getInt(), ByteBuffer.wrap(highRegisterBytes).getInt()};
        return returnValue;
    }

    public static int[] ConvertDoubleToTwoRegisters(int doubleValue, ModbusClient.RegisterOrder registerOrder) {
        int[] registerValues = ConvertFloatToTwoRegisters((float)doubleValue);
        int[] returnValue = registerValues;
        if (registerOrder == ModbusClient.RegisterOrder.HighLow) {
            returnValue = new int[]{registerValues[1], registerValues[0]};
        }

        return returnValue;
    }

    public static byte[] calculateCRC(byte[] data, int numberOfBytes, int startByte) {
        byte[] auchCRCHi = new byte[]{0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64};
        byte[] auchCRCLo = new byte[]{0, -64, -63, 1, -61, 3, 2, -62, -58, 6, 7, -57, 5, -59, -60, 4, -52, 12, 13, -51, 15, -49, -50, 14, 10, -54, -53, 11, -55, 9, 8, -56, -40, 24, 25, -39, 27, -37, -38, 26, 30, -34, -33, 31, -35, 29, 28, -36, 20, -44, -43, 21, -41, 23, 22, -42, -46, 18, 19, -45, 17, -47, -48, 16, -16, 48, 49, -15, 51, -13, -14, 50, 54, -10, -9, 55, -11, 53, 52, -12, 60, -4, -3, 61, -1, 63, 62, -2, -6, 58, 59, -5, 57, -7, -8, 56, 40, -24, -23, 41, -21, 43, 42, -22, -18, 46, 47, -17, 45, -19, -20, 44, -28, 36, 37, -27, 39, -25, -26, 38, 34, -30, -29, 35, -31, 33, 32, -32, -96, 96, 97, -95, 99, -93, -94, 98, 102, -90, -89, 103, -91, 101, 100, -92, 108, -84, -83, 109, -81, 111, 110, -82, -86, 106, 107, -85, 105, -87, -88, 104, 120, -72, -71, 121, -69, 123, 122, -70, -66, 126, 127, -65, 125, -67, -68, 124, -76, 116, 117, -75, 119, -73, -74, 118, 114, -78, -77, 115, -79, 113, 112, -80, 80, -112, -111, 81, -109, 83, 82, -110, -106, 86, 87, -105, 85, -107, -108, 84, -100, 92, 93, -99, 95, -97, -98, 94, 90, -102, -101, 91, -103, 89, 88, -104, -120, 72, 73, -119, 75, -117, -118, 74, 78, -114, -113, 79, -115, 77, 76, -116, 68, -124, -123, 69, -121, 71, 70, -122, -126, 66, 67, -125, 65, -127, -128, 64};
        short usDataLen = (short)numberOfBytes;
        byte uchCRCHi = -1;
        byte uchCRCLo = -1;

        for(int i = 0; usDataLen > 0; ++i) {
            --usDataLen;
            int uIndex = uchCRCLo ^ data[i + startByte];
            if (uIndex < 0) {
                uIndex += 256;
            }

            uchCRCLo = (byte)(uchCRCHi ^ auchCRCHi[uIndex]);
            uchCRCHi = auchCRCLo[uIndex];
        }

        byte[] returnValue = new byte[]{uchCRCLo, uchCRCHi};
        return returnValue;
    }

    public boolean[] ReadDiscreteInputs(int startingAddress, int quantity) throws ModbusException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null) {
            throw new ConnectionException("connection Error");
        } else if (startingAddress > 65535 | quantity > 2000) {
            throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 2000");
        } else {
            boolean[] response = null;
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 2;
            this.startingAddress = toByteArray(startingAddress);
            this.quantity = toByteArray(quantity);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], this.quantity[1], this.quantity[0], this.crc[0], this.crc[1]};
            if (this.serialflag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            int intData;
            if (this.serialflag) {
                this.out.write(data, 6, 8);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + quantity / 8 + 1;
                if (quantity % 8 == 0) {
                    expectedlength = 5 + quantity / 8;
                }

                int currentLength = 0;

                while(currentLength < expectedlength) {
                    len = 1;

                    while((intData = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(int i = 0; i < intData; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    serialdata = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            int i;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length - 2, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var15 = this.sendDataChangedListener.iterator();

                        while(var15.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var15.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);
                        Iterator var21 = this.receiveDataChangedListener.iterator();

                        while(var21.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var21.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 130 & data[8] == 1) {
                throw new FunctionCodeNotSupportedException("Function code not supported by master");
            } else if ((data[7] & 255) == 130 & data[8] == 2) {
                throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
            } else if ((data[7] & 255) == 130 & data[8] == 3) {
                throw new QuantityInvalidException("Quantity invalid");
            } else if ((data[7] & 255) == 130 & data[8] == 4) {
                throw new ModbusException("Error reading");
            } else {
                response = new boolean[quantity];

                for(i = 0; i < quantity; ++i) {
                    intData = data[9 + i / 8];
                    int mask = (int)Math.pow(2.0D, (double)(i % 8));
                    intData = (intData & mask) / mask;
                    if (intData > 0) {
                        response[i] = true;
                    } else {
                        response[i] = false;
                    }
                }

                return response;
            }
        }
    }

    public boolean[] ReadCoils(int startingAddress, int quantity) throws ModbusException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null) {
            throw new ConnectionException("connection Error");
        } else if (startingAddress > 65535 | quantity > 2000) {
            throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 2000");
        } else {
            boolean[] response = new boolean[quantity];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 1;
            this.startingAddress = toByteArray(startingAddress);
            this.quantity = toByteArray(quantity);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], this.quantity[1], this.quantity[0], this.crc[0], this.crc[1]};
            if (this.serialflag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            int intData;
            if (this.serialflag) {
                this.out.write(data, 6, 8);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + quantity / 8 + 1;
                if (quantity % 8 == 0) {
                    expectedlength = 5 + quantity / 8;
                }

                int currentLength = 0;

                while(currentLength < expectedlength) {
                    len = 1;

                    while((intData = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(int i = 0; i < intData; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    serialdata = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            int i;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var14 = this.sendDataChangedListener.iterator();

                        while(var14.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var14.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);
                        Iterator var20 = this.receiveDataChangedListener.iterator();

                        while(var20.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var20.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 129 & data[8] == 1) {
                throw new FunctionCodeNotSupportedException("Function code not supported by master");
            } else if ((data[7] & 255) == 129 & data[8] == 2) {
                throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
            } else if ((data[7] & 255) == 129 & data[8] == 3) {
                throw new QuantityInvalidException("Quantity invalid");
            } else if ((data[7] & 255) == 129 & data[8] == 4) {
                throw new ModbusException("Error reading");
            } else {
                for(i = 0; i < quantity; ++i) {
                    intData = data[9 + i / 8];
                    int mask = (int)Math.pow(2.0D, (double)(i % 8));
                    intData = (intData & mask) / mask;
                    if (intData > 0) {
                        response[i] = true;
                    } else {
                        response[i] = false;
                    }
                }

                return response;
            }
        }
    }

    public int[] ReadHoldingRegisters(int startingAddress, int quantity) throws ModbusException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null) {
            throw new ConnectionException("connection Error");
        } else if (startingAddress > 65535 | quantity > 125) {
            throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125");
        } else {
            int[] response = new int[quantity];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 3;
            this.startingAddress = toByteArray(startingAddress);
            this.quantity = toByteArray(quantity);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], this.quantity[1], this.quantity[0], this.crc[0], this.crc[1]};
            if (this.serialflag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialflag) {
                this.out.write(data, 6, 8);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + 2 * quantity;
                int currentLength = 0;

                int i;
                while(currentLength < expectedlength) {
                    len = 1;

                    while((len = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(i = 0; i < len; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }

                if (serialdata != null) {
                    data = new byte[262];
                    System.arraycopy(serialdata, 0, data, 6, serialdata.length);
                }

                for(i = 0; i < quantity; ++i) {
                    byte[] bytes = new byte[]{data[3 + i * 2], data[3 + i * 2 + 1]};
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    response[i] = byteBuffer.getShort();
                }
            }

            int i;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var17 = this.sendDataChangedListener.iterator();

                        while(var17.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var17.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);
                        Iterator var23 = this.receiveDataChangedListener.iterator();

                        while(var23.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var23.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if (data[7] == 131 & data[8] == 1) {
                throw new FunctionCodeNotSupportedException("Function code not supported by master");
            } else if (data[7] == 131 & data[8] == 2) {
                throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
            } else if (data[7] == 131 & data[8] == 3) {
                throw new QuantityInvalidException("Quantity invalid");
            } else if (data[7] == 131 & data[8] == 4) {
                throw new ModbusException("Error reading");
            } else {
                for(i = 0; i < quantity; ++i) {
                    byte[] bytes = new byte[]{data[9 + i * 2], data[9 + i * 2 + 1]};
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    response[i] = byteBuffer.getShort();
                }

                return response;
            }
        }
    }

    public int[] ReadInputRegisters(int startingAddress, int quantity) throws ModbusException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null) {
            throw new ConnectionException("connection Error");
        } else if (startingAddress > 65535 | quantity > 125) {
            throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125");
        } else {
            int[] response = new int[quantity];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 4;
            this.startingAddress = toByteArray(startingAddress);
            this.quantity = toByteArray(quantity);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], this.quantity[1], this.quantity[0], this.crc[0], this.crc[1]};
            if (this.serialflag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialflag) {
                this.out.write(data, 6, 8);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + 2 * quantity;
                int currentLength = 0;

                int i;
                while(currentLength < expectedlength) {
                    len = 1;

                    while((len = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(i = 0; i < len; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }

                if (serialdata != null) {
                    data = new byte[262];
                    System.arraycopy(serialdata, 0, data, 6, serialdata.length);
                }

                for(i = 0; i < quantity; ++i) {
                    byte[] bytes = new byte[]{data[3 + i * 2], data[3 + i * 2 + 1]};
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    response[i] = byteBuffer.getShort();
                }
            }

            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                int i;
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var16 = this.sendDataChangedListener.iterator();

                        while(var16.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var16.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);
                        Iterator var21 = this.receiveDataChangedListener.iterator();

                        while(var21.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var21.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }

                if ((data[7] & 255) == 132 & data[8] == 1) {
                    throw new FunctionCodeNotSupportedException("Function code not supported by master");
                }

                if ((data[7] & 255) == 132 & data[8] == 2) {
                    throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
                }

                if ((data[7] & 255) == 132 & data[8] == 3) {
                    throw new QuantityInvalidException("Quantity invalid");
                }

                if ((data[7] & 255) == 132 & data[8] == 4) {
                    throw new ModbusException("Error reading");
                }

                for(i = 0; i < quantity; ++i) {
                    byte[] bytes = new byte[]{data[9 + i * 2], data[9 + i * 2 + 1]};
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    response[i] = byteBuffer.getShort();
                }
            }

            return response;
        }
    }

    public void WriteSingleCoil(int startingAddress, boolean value) throws ModbusException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else {
            byte[] coilValue = new byte[2];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 5;
            this.startingAddress = toByteArray(startingAddress);
            if (value) {
                coilValue = toByteArray(65280);
            } else {
                coilValue = toByteArray(0);
            }

            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], coilValue[1], coilValue[0], this.crc[0], this.crc[1]};
            if (this.serialflag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialflag) {
                this.out.write(data, 6, 8);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 8;
                int currentLength = 0;

                while(currentLength < expectedlength) {
                    len = 1;

                    while((len = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(int i = 0; i < len; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var14 = this.sendDataChangedListener.iterator();

                        while(var14.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var14.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    int numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
                        Iterator var20 = this.receiveDataChangedListener.iterator();

                        while(var20.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var20.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 133 & data[8] == 1) {
                throw new FunctionCodeNotSupportedException("Function code not supported by master");
            } else if ((data[7] & 255) == 133 & data[8] == 2) {
                throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 133 & data[8] == 3) {
                throw new QuantityInvalidException("quantity invalid");
            } else if ((data[7] & 255) == 133 & data[8] == 4) {
                throw new ModbusException("error reading");
            }
        }
    }

    public void WriteSingleRegister(int startingAddress, int value) throws ModbusException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else {
            byte[] registerValue = new byte[2];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 6;
            this.startingAddress = toByteArray(startingAddress);
            registerValue = toByteArray((short)value);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], registerValue[1], registerValue[0], this.crc[0], this.crc[1]};
            if (this.serialflag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialflag) {
                this.out.write(data, 6, 8);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 8;
                int currentLength = 0;

                while(currentLength < expectedlength) {
                    len = 1;

                    while((len = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(int i = 0; i < len; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var14 = this.sendDataChangedListener.iterator();

                        while(var14.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var14.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    int numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
                        Iterator var20 = this.receiveDataChangedListener.iterator();

                        while(var20.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var20.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 134 & data[8] == 1) {
                throw new FunctionCodeNotSupportedException("Function code not supported by master");
            } else if ((data[7] & 255) == 134 & data[8] == 2) {
                throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 134 & data[8] == 3) {
                throw new QuantityInvalidException("quantity invalid");
            } else if ((data[7] & 255) == 134 & data[8] == 4) {
                throw new ModbusException("error reading");
            }
        }
    }

    public void WriteMultipleCoils(int startingAddress, boolean[] values) throws ModbusException, UnknownHostException, SocketException, IOException {
        byte byteCount = (byte)(values.length / 8 + 1);
        if (values.length % 8 == 0) {
            --byteCount;
        }

        byte[] quantityOfOutputs = toByteArray(values.length);
        byte singleCoilValue = 0;
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else {
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(7 + values.length / 8 + 1);
            this.functionCode = 15;
            this.startingAddress = toByteArray(startingAddress);
            byte[] data = new byte[16 + byteCount - 1];
            data[0] = this.transactionIdentifier[1];
            data[1] = this.transactionIdentifier[0];
            data[2] = this.protocolIdentifier[1];
            data[3] = this.protocolIdentifier[0];
            data[4] = this.length[1];
            data[5] = this.length[0];
            data[6] = this.unitIdentifier;
            data[7] = this.functionCode;
            data[8] = this.startingAddress[1];
            data[9] = this.startingAddress[0];
            data[10] = quantityOfOutputs[1];
            data[11] = quantityOfOutputs[0];
            data[12] = byteCount;

            for(int i = 0; i < values.length; ++i) {
                if (i % 8 == 0) {
                    singleCoilValue = 0;
                }

                byte CoilValue;
                if (values[i]) {
                    CoilValue = 1;
                } else {
                    CoilValue = 0;
                }

                singleCoilValue = (byte)(CoilValue << i % 8 | singleCoilValue);
                data[13 + i / 8] = singleCoilValue;
            }

            if (this.serialflag) {
                this.crc = calculateCRC(data, data.length - 8, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialflag) {
                this.out.write(data, 6, 9 + byteCount);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 8;
                int currentLength = 0;

                while(currentLength < expectedlength) {
                    len = 1;


                    while((len = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(int i = 0; i < len; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var20 = this.sendDataChangedListener.iterator();

                        while(var20.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var20.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    int numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
                        Iterator var24 = this.receiveDataChangedListener.iterator();

                        while(var24.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var24.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 143 & data[8] == 1) {
                throw new FunctionCodeNotSupportedException("Function code not supported by master");
            } else if ((data[7] & 255) == 143 & data[8] == 2) {
                throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 143 & data[8] == 3) {
                throw new QuantityInvalidException("quantity invalid");
            } else if ((data[7] & 255) == 143 & data[8] == 4) {
                throw new ModbusException("error reading");
            }
        }
    }

    public void WriteMultipleRegisters(int startingAddress, int[] values) throws ModbusException, UnknownHostException, SocketException, IOException {
        byte byteCount = (byte)(values.length * 2);
        byte[] quantityOfOutputs = toByteArray(values.length);
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else {
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(7 + values.length * 2);
            this.functionCode = 16;
            this.startingAddress = toByteArray(startingAddress);
            byte[] data = new byte[15 + values.length * 2];
            data[0] = this.transactionIdentifier[1];
            data[1] = this.transactionIdentifier[0];
            data[2] = this.protocolIdentifier[1];
            data[3] = this.protocolIdentifier[0];
            data[4] = this.length[1];
            data[5] = this.length[0];
            data[6] = this.unitIdentifier;
            data[7] = this.functionCode;
            data[8] = this.startingAddress[1];
            data[9] = this.startingAddress[0];
            data[10] = quantityOfOutputs[1];
            data[11] = quantityOfOutputs[0];
            data[12] = byteCount;

            for(int i = 0; i < values.length; ++i) {
                byte[] singleRegisterValue = toByteArray(values[i]);
                data[13 + i * 2] = singleRegisterValue[1];
                data[14 + i * 2] = singleRegisterValue[0];
            }

            if (this.serialflag) {
                this.crc = calculateCRC(data, data.length - 8, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialflag) {
                this.out.write(data, 6, 9 + byteCount);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 8;
                int currentLength = 0;

                while(currentLength < expectedlength) {
                    len = 1;

                    while((len = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(int i = 0; i < len; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var17 = this.sendDataChangedListener.iterator();

                        while(var17.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var17.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    int numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
                        Iterator var23 = this.receiveDataChangedListener.iterator();

                        while(var23.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var23.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 144 & data[8] == 1) {
                throw new FunctionCodeNotSupportedException("Function code not supported by master");
            } else if ((data[7] & 255) == 144 & data[8] == 2) {
                throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 144 & data[8] == 3) {
                throw new QuantityInvalidException("quantity invalid");
            } else if ((data[7] & 255) == 144 & data[8] == 4) {
                throw new ModbusException("error reading");
            }
        }
    }

    public int[] ReadWriteMultipleRegisters(int startingAddressRead, int quantityRead, int startingAddressWrite, int[] values) throws ModbusException, UnknownHostException, SocketException, IOException {
        byte[] startingAddressReadLocal = new byte[2];
        byte[] quantityReadLocal = new byte[2];
        byte[] startingAddressWriteLocal = new byte[2];
        byte[] quantityWriteLocal = new byte[2];
        byte writeByteCountLocal = 0;
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else if (startingAddressRead > 65535 | quantityRead > 125 | startingAddressWrite > 65535 | values.length > 121) {
            throw new IllegalArgumentException("Starting address must be 0 - 65535; quantity must be 0 - 125");
        } else {
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 23;
            startingAddressReadLocal = toByteArray(startingAddressRead);
            quantityReadLocal = toByteArray(quantityRead);
            startingAddressWriteLocal = toByteArray(startingAddressWrite);
            quantityWriteLocal = toByteArray(values.length);
            writeByteCountLocal = (byte)(values.length * 2);
            byte[] data = new byte[19 + values.length * 2];
            data[0] = this.transactionIdentifier[1];
            data[1] = this.transactionIdentifier[0];
            data[2] = this.protocolIdentifier[1];
            data[3] = this.protocolIdentifier[0];
            data[4] = this.length[1];
            data[5] = this.length[0];
            data[6] = this.unitIdentifier;
            data[7] = this.functionCode;
            data[8] = startingAddressReadLocal[1];
            data[9] = startingAddressReadLocal[0];
            data[10] = quantityReadLocal[1];
            data[11] = quantityReadLocal[0];
            data[12] = startingAddressWriteLocal[1];
            data[13] = startingAddressWriteLocal[0];
            data[14] = quantityWriteLocal[1];
            data[15] = quantityWriteLocal[0];
            data[16] = writeByteCountLocal;

            for(int i = 0; i < values.length; ++i) {
                byte[] singleRegisterValue = toByteArray(values[i]);
                data[17 + i * 2] = singleRegisterValue[1];
                data[18 + i * 2] = singleRegisterValue[0];
            }

            if (this.serialflag) {
                this.crc = calculateCRC(data, data.length - 8, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialflag) {
                this.out.write(data, 6, 13 + writeByteCountLocal);
                byte receivedUnitIdentifier = 1;
                int len = 1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + quantityRead;
                int currentLength = 0;

                while(currentLength < expectedlength) {
                    len = 1;

                    while((len = this.in.read(serialBuffer)) <= 0) {
                    }

                    for(int i = 0; i < len; ++i) {
                        serialdata[currentLength] = serialBuffer[i];
                        ++currentLength;
                    }
                }

                receivedUnitIdentifier = serialdata[0];
                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            int i;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
                        Iterator var24 = this.sendDataChangedListener.iterator();

                        while(var24.hasNext()) {
                            SendDataChangedListener hl = (SendDataChangedListener)var24.next();
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);
                        Iterator var30 = this.receiveDataChangedListener.iterator();

                        while(var30.hasNext()) {
                            ReceiveDataChangedListener hl = (ReceiveDataChangedListener)var30.next();
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 151 & data[8] == 1) {
                throw new FunctionCodeNotSupportedException("Function code not supported by master");
            } else if ((data[7] & 255) == 151 & data[8] == 2) {
                throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 151 & data[8] == 3) {
                throw new QuantityInvalidException("quantity invalid");
            } else if ((data[7] & 255) == 151 & data[8] == 4) {
                throw new ModbusException("error reading");
            } else {
                int[] response = new int[quantityRead];

                for(i = 0; i < quantityRead; ++i) {
                    byte highByte = data[9 + i * 2];
                    byte lowByte = data[9 + i * 2 + 1];
                    byte[] bytes = new byte[]{highByte, lowByte};
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    response[i] = byteBuffer.getShort();
                }

                return response;
            }
        }
    }

    public void Disconnect() throws IOException {
        if (!this.serialflag) {
            if (this.inStream != null) {
                this.inStream.close();
            }

            if (this.outStream != null) {
                this.outStream.close();
            }

            if (this.tcpClientSocket != null) {
                this.tcpClientSocket.close();
            }

            this.tcpClientSocket = null;
        } else if (this.serialPort != null) {
            this.serialPort.close();
        }

    }

    public static byte[] toByteArray(int value) {
        byte[] result = new byte[]{(byte)value, (byte)(value >> 8)};
        return result;
    }

    public static byte[] toByteArrayDouble(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte[] toByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public boolean isConnected() {
        if (this.serialflag) {
            if (this.portIdentifier == null) {
                return false;
            } else {
                return this.portIdentifier.isCurrentlyOwned();
            }
        } else {
            boolean returnValue = false;
            if (this.tcpClientSocket == null) {
                returnValue = false;
            } else if (this.tcpClientSocket.isConnected()) {
                returnValue = true;
            } else {
                returnValue = false;
            }

            return returnValue;
        }
    }

    public String getipAddress() {
        return this.ipAddress;
    }

    public void setipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean getUDPFlag() {
        return this.udpFlag;
    }

    public void setUDPFlag(boolean udpFlag) {
        this.udpFlag = udpFlag;
    }

    public int getConnectionTimeout() {
        return this.connectTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectTimeout = connectionTimeout;
    }

    public void setSerialFlag(boolean serialflag) {
        this.serialflag = serialflag;
    }

    public boolean getSerialFlag() {
        return this.serialflag;
    }

    public void setUnitIdentifier(byte unitIdentifier) {
        this.unitIdentifier = unitIdentifier;
    }

    public byte getUnitIdentifier() {
        return this.unitIdentifier;
    }

    public void addReveiveDataChangedListener(ReceiveDataChangedListener toAdd) {
        this.receiveDataChangedListener.add(toAdd);
    }

    public void addSendDataChangedListener(SendDataChangedListener toAdd) {
        this.sendDataChangedListener.add(toAdd);
    }

    public static enum RegisterOrder {
        LowHigh,
        HighLow;

        private RegisterOrder() {
        }
    }
}
