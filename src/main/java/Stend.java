import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

import java.io.IOException;

public class Stend {

    public void data() {

        ModbusClient modbusClient = new ModbusClient("192.168.17.115", 502);
        if (connect(modbusClient)) {
            parser(modbusClient);
        }


    }

    private boolean connect(ModbusClient target) {
        try {
            target.Connect();
            target.setConnectionTimeout(2000);
            byte slaveId = 10;
            target.setUnitIdentifier(slaveId);
            System.out.println("соединение установлено");
            return true;
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("соединение не установлено");
            return false;
        }
    }

    private void parser(ModbusClient modbusClient) {
        try {
            Float maxDeformation = ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(5, 2));
            System.out.println("maxDeformation: " + maxDeformation);

        } catch (ModbusException | IOException e) {
            e.printStackTrace();
        }
    }

}
