package servicesecurity;

import data.AlarmStatus;
import data.PretendDatabaseSecurityRepositoryImpl;
import data.Sensor;
import data.SensorType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import serviceimage.FakeImageService;

import static data.AlarmStatus.ALARM;

//@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    static PretendDatabaseSecurityRepositoryImpl securityImpl = null;
    static FakeImageService imageService = null;
    static SecurityService service = null;


    @BeforeAll
    public static void init(){
        securityImpl = new PretendDatabaseSecurityRepositoryImpl();
        imageService = new FakeImageService();
        service = new SecurityService(securityImpl, imageService);


    }

    /*@Test
    public void armedAlarm_ActivatedSensor_pendingAlarmStatus(){
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        securityImpl.addSensor(sensor);
        service.changeSensorActivationStatus(sensor, true);
        assert(securityImpl.getAlarmStatus() == ALARM);

    }*/
}


