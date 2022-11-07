package servicesecurity;

import data.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serviceimage.FakeImageService;
import org.junit.jupiter.params.ParameterizedTest;
import serviceimage.ImageService;

import java.awt.image.BufferedImage;
import java.util.EnumSet;

import static data.AlarmStatus.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    static PretendDatabaseSecurityRepositoryImpl securityImpl = null;

    @Mock
    static ImageService imageService = null;

    static SecurityService service = null;


    @BeforeAll
    public static void init(){
        securityImpl = new PretendDatabaseSecurityRepositoryImpl();
        imageService = new FakeImageService();
        service = spy(new SecurityService(securityImpl, imageService));

    }

    @BeforeEach
    public void resetAlarm(){
        service.setArmingStatus(ArmingStatus.DISARMED);
    }

    @Test
    public void armedAlarm_activatedSensor_pendingAlarmStatus(){

        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        securityImpl.addSensor(sensor);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.changeSensorActivationStatus(sensor, true);
        assert(securityImpl.getAlarmStatus() == PENDING_ALARM);
        service.setArmingStatus(ArmingStatus.DISARMED);
        securityImpl.removeSensor(sensor);

    }

    @Test
    public void armedAlarm_activatedSensorAndPendingAlarmStatus_alarmAlarmStatus(){

        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        securityImpl.addSensor(sensor);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.setAlarmStatus(PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, true);
        assert(securityImpl.getAlarmStatus() == ALARM);
        service.setArmingStatus(ArmingStatus.DISARMED);
        securityImpl.removeSensor(sensor);

    }

    @Test
    public void pendingAlarm_inactiveSensors_noAlarm(){
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        Sensor extraSensor = new Sensor("sensor2", SensorType.DOOR);
        service.changeSensorActivationStatus(sensor, true);
        service.changeSensorActivationStatus(extraSensor, true);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.setAlarmStatus(PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, false);
        service.changeSensorActivationStatus(extraSensor, false);
        assert(securityImpl.getAlarmStatus() == NO_ALARM);
        securityImpl.removeSensor(sensor);
        securityImpl.removeSensor(extraSensor);
    }

    @Test
    public void activeAlarm_sensorStateChange_noAlarmChange(){
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.setAlarmStatus(ALARM);
        service.changeSensorActivationStatus(sensor, true);
        assert(securityImpl.getAlarmStatus() == ALARM);
        securityImpl.removeSensor(sensor);

    }
    @Test
    public void pendingAlarm_activatedActiveSensor_alarmAlarmState(){
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        service.changeSensorActivationStatus(sensor, true);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.setAlarmStatus(PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, true);
        assert(securityImpl.getAlarmStatus() == ALARM);
        service.removeSensor(sensor);

    }

    @Test
    public void deactivatedInactiveSensor_noAlarmChange(){
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        service.changeSensorActivationStatus(sensor, false);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.setAlarmStatus(PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, false);
        assert(securityImpl.getAlarmStatus() == PENDING_ALARM);
        service.setAlarmStatus(ALARM);
        service.changeSensorActivationStatus(sensor, false);
        assert(securityImpl.getAlarmStatus() == ALARM);
        service.removeSensor(sensor);
    }

    @Test
    public void imageContainsCat_systemArmedHome_alarmAlarmStatus(){

        when(imageService.imageContainsCat(ArgumentMatchers.any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        String x = securityImpl.getAlarmStatus().toString();
        assert(securityImpl.getAlarmStatus() == ALARM);


    }

    @Test
    public void imageDoesNotContainCat_noAlarm_noActiveSensors(){
        service.setAlarmStatus(ALARM);
        when(imageService.imageContainsCat(ArgumentMatchers.any(BufferedImage.class), ArgumentMatchers.any(Float.class))).thenReturn(false);
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        Sensor extraSensor = new Sensor("sensor2", SensorType.DOOR);
        service.changeSensorActivationStatus(sensor, true);
        service.changeSensorActivationStatus(extraSensor, true);
        String x = securityImpl.getAlarmStatus().toString();


        assert(securityImpl.getAlarmStatus() == ALARM);
        service.changeSensorActivationStatus(sensor, false);
        service.changeSensorActivationStatus(extraSensor, false);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        assert(securityImpl.getAlarmStatus() == NO_ALARM);



    }
}


