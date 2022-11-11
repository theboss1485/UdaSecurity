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

    PretendDatabaseSecurityRepositoryImpl securityImpl = null;
    ArmingStatus[] statuses;
    int[] ints;

    @Mock
    ImageService imageService;

    SecurityService service = null;


    @BeforeEach
    public void init(){
        securityImpl = new PretendDatabaseSecurityRepositoryImpl();
        service = spy(new SecurityService(securityImpl, imageService));
        service.setArmingStatus(ArmingStatus.DISARMED);

    }


    @Test
    public void armedAlarm_activatedSensor_pendingAlarmStatus(){

        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        service.addSensor(sensor);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.changeSensorActivationStatus(sensor, true);
        assert(service.getAlarmStatus() == PENDING_ALARM);
        service.setArmingStatus(ArmingStatus.DISARMED);
        service.removeSensor(sensor);

    }

    @Test
    public void armedAlarm_activatedSensorAndPendingAlarmStatus_alarmAlarmStatus(){

        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        service.addSensor(sensor);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.setAlarmStatus(PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, true);
        assert(service.getAlarmStatus() == ALARM);
        service.setArmingStatus(ArmingStatus.DISARMED);
        service.removeSensor(sensor);

    }

    @Test
    public void pendingAlarm_inactiveSensors_noAlarm(){
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        service.changeSensorActivationStatus(sensor, true);
        service.setAlarmStatus(PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, false);
        assert(service.getAlarmStatus() == NO_ALARM);
        service.removeSensor(sensor);
    }

    @Test
    public void activeAlarm_sensorStateChange_noAlarmChange(){
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        service.setAlarmStatus(ALARM);
        service.changeSensorActivationStatus(sensor, true);
        assert(service.getAlarmStatus() == ALARM);
        service.removeSensor(sensor);

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
        assert(service.getAlarmStatus() == PENDING_ALARM);
        service.setAlarmStatus(ALARM);
        service.changeSensorActivationStatus(sensor, false);
        assert(service.getAlarmStatus() == ALARM);
        service.removeSensor(sensor);
    }

    @ParameterizedTest
    @ValueSource(ints  = {1, 2, 3})
    public void imageContainsCat_systemArmedHome_alarmAlarmStatus(int number){

        ArmingStatus status = null;

        switch(number){
            case 1: status = ArmingStatus.DISARMED;
                break;
            case 2: status = ArmingStatus.ARMED_HOME;
                break;
            case 3: status = ArmingStatus.ARMED_AWAY;
        }

        when(imageService.imageContainsCat(ArgumentMatchers.any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        service.setArmingStatus(status);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        String x = service.getAlarmStatus().toString();
        if(number == 2){
            assert(service.getAlarmStatus() == ALARM);
        } else {
            assert(service.getAlarmStatus() == NO_ALARM);
        }



    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void imageDoesNotContainCat_noAlarm_noActiveSensors(boolean condition){
        service.setArmingStatus(ArmingStatus.ARMED_HOME);

        when(imageService.imageContainsCat(ArgumentMatchers.any(BufferedImage.class), ArgumentMatchers.any(Float.class))).thenReturn(false);
        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        Sensor extraSensor = new Sensor("sensor2", SensorType.DOOR);

        service.changeSensorActivationStatus(sensor, condition);
        service.changeSensorActivationStatus(extraSensor, condition);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        String x = service.getAlarmStatus().toString();

        if(condition == true){
            assert(service.getAlarmStatus() == ALARM);
        } else {
            assert (service.getAlarmStatus() == NO_ALARM);
        }

        service.removeSensor(sensor);
        service.removeSensor(extraSensor);
    }

    @Test
    public void systemDisarmed_statusNoAlarm(){

        service.setArmingStatus(ArmingStatus.DISARMED);
        assert(service.getAlarmStatus() == NO_ALARM);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void systemArmed_sensorsDeactivated(int status){
        Sensor sensor1 = new Sensor("sensor1", SensorType.DOOR);
        Sensor sensor2 = new Sensor("sensor2", SensorType.DOOR);
        service.changeSensorActivationStatus(sensor1, true);
        service.changeSensorActivationStatus(sensor2, true);
        boolean active = false;
        switch(status){
            case 1:
                service.setArmingStatus(ArmingStatus.ARMED_HOME);
                break;
            case 2:
                service.setArmingStatus(ArmingStatus.ARMED_AWAY);
                break;
        }

        for(Sensor sensor : service.getSensors()){
            if (sensor.getActive() == true){
                active = true;
            }
        }
        assert(active == false);
        service.removeSensor(sensor1);
        service.removeSensor(sensor2);
    }

    @Test
    public void armedHome_cameraShowingCat_alarmAlarmStatus(){
        service.setArmingStatus(ArmingStatus.DISARMED);
        when(imageService.imageContainsCat(ArgumentMatchers.any(BufferedImage.class), ArgumentMatchers.any(Float.class))).thenReturn(true);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        assert(service.getAlarmStatus() == ALARM);
    }

    @Test
    public void activeAlarm_sensorStateChange_activeAlarm(){
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        Sensor sensor1 = new Sensor("sensor1", SensorType.DOOR);
        Sensor sensor2 = new Sensor("sensor2", SensorType.DOOR);
        service.changeSensorActivationStatus(sensor1, true);
        service.changeSensorActivationStatus(sensor2, true);
        assert(service.getAlarmStatus() == ALARM);
        service.changeSensorActivationStatus(sensor2, false);
        assert(service.getAlarmStatus() == ALARM);
    }

    @Test
    public void scanCat_activeAlarm_scanNotCat_noAlarm(){
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(ArgumentMatchers.any(BufferedImage.class), ArgumentMatchers.any(Float.class))).thenReturn(true);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        assert(service.getAlarmStatus() == ALARM);
        when(imageService.imageContainsCat(ArgumentMatchers.any(BufferedImage.class), ArgumentMatchers.any(Float.class))).thenReturn(false);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        assert(service.getAlarmStatus() == NO_ALARM);
    }

    @Test
    public void disarmedSystem_scanCat_noAlarm(){
        service.setArmingStatus(ArmingStatus.DISARMED);
        when(imageService.imageContainsCat(ArgumentMatchers.any(BufferedImage.class), ArgumentMatchers.any(Float.class))).thenReturn(true);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        assert(service.getAlarmStatus() == NO_ALARM);
    }

    @Test
    public void scanCat_activeSensor_alarm_scanNotCat_alarm(){
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(ArgumentMatchers.any(BufferedImage.class), ArgumentMatchers.any(Float.class))).thenReturn(true);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        Sensor sensor1 = new Sensor("sensor1", SensorType.DOOR);
        service.changeSensorActivationStatus(sensor1, true);
        when(imageService.imageContainsCat(ArgumentMatchers.any(BufferedImage.class), ArgumentMatchers.any(Float.class))).thenReturn(false);
        service.processImage(new BufferedImage(5, 5,BufferedImage.TYPE_3BYTE_BGR));
        assert(service.getAlarmStatus() == ALARM);
    }


    //Penultimate application requirement is fulfilled by Test systemArmed_sensorsDeactivated
    //final application requirement is fulfilled by Test armedHome_cameraShowingCat_alarmAlarmStatus
}


