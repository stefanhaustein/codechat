package org.kobjects.codechat.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedString;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.AbstractInstance;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

import static android.content.Context.SENSOR_SERVICE;

public class Sensors extends AbstractInstance {
    static final private ListType TYPE_VECTOR = new ListType(Type.NUMBER);

    static InstanceType TYPE = new InstanceType(true) {
        @Override
        public String getName() {
            return "Sensors";
        }

        public void printDocumentationBody(AnnotatedStringBuilder asb) {
            asb.append("Hardware sensors are available as properties of the system variable 'sensors'. " +
                    "The set of supportes sensors is likely to change in the future.");
        }
    }
            .addProperty(0, "temperature", Type.NUMBER, false,
                    "The current room temperature in degree Celsius")
            .addProperty(1, "light", Type.NUMBER, false,
                "The current ambient light level in lux.")
            .addProperty(2, "proximity", Type.NUMBER, false,
                    "The current estimated user proximity in centimeters.")
            .addProperty(3, "pressure", Type.NUMBER, false,
                    "The current pressure in millibar.")
            .addProperty(4, "humidity", Type.NUMBER, false,
                    "The current relative humidity in percent")
            .addProperty(5, "heartRate", Type.NUMBER, false,
                    "The current user heart rate in beats per minute.")
            .addProperty(6, "heartBeat", Type.NUMBER, false,
                    "The confidence in detecting the last heart beat. On-listeners will be triggered every time a beat is detected.")

            .addProperty(7, "acceleration", TYPE_VECTOR, false,
                    "List of accelleration on the x, y and z-axis in m/s\u00b2, excluding gravity.")
            .addProperty(8, "gravity", TYPE_VECTOR, false,
                    "List of the split of the gravity acceleration on the x, y an z-axis in m/s\u00b2.")
            .addProperty(9, "gyroscope", TYPE_VECTOR, false,
                    "Angular speed around the x, y and z-axis.")
            .addProperty(10, "orientation", TYPE_VECTOR, false,
                    "Azimuth, pitch and roll in degrees.")
            .addProperty(10, "rotation", TYPE_VECTOR, false,
                    "The value of the Android sensor of type TYPE_ROTATION_VECTOR. " +
                    "safer but more complex handling of the current orientation; please refer to the corresponding" +
                            "Android documentation for details.")
            .addProperty(11, "compass", TYPE_VECTOR, false,
                    "Similar to rotation, but bases on the magnetic field.");

    SensorManager sensorManager;

    HandlerThread sensorHandlerThread;
    Handler sensorHandler;

    public ScalarSensorProperty temperature = new ScalarSensorProperty(android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE);
    public ScalarSensorProperty light = new ScalarSensorProperty(android.hardware.Sensor.TYPE_LIGHT);
    public ScalarSensorProperty proximity = new ScalarSensorProperty(android.hardware.Sensor.TYPE_PROXIMITY);
    public ScalarSensorProperty pressure = new ScalarSensorProperty(android.hardware.Sensor.TYPE_PRESSURE);
    public ScalarSensorProperty humidity = new ScalarSensorProperty(android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY);
    public ScalarSensorProperty heartRate = new ScalarSensorProperty(android.hardware.Sensor.TYPE_HEART_RATE);
    public ScalarSensorProperty heartBeat = new ScalarSensorProperty(android.hardware.Sensor.TYPE_HEART_BEAT);

    public VectorSensorProperty acceleration = new VectorSensorProperty(Sensor.TYPE_LINEAR_ACCELERATION, 3);
    public VectorSensorProperty gravity = new VectorSensorProperty(Sensor.TYPE_GRAVITY, 3);
    public VectorSensorProperty gyroscope = new VectorSensorProperty(Sensor.TYPE_GYROSCOPE, 3);
    public VectorSensorProperty orientation = new VectorSensorProperty(Sensor.TYPE_ORIENTATION, 3);
    public VectorSensorProperty rotation = new VectorSensorProperty(Sensor.TYPE_ROTATION_VECTOR, 4);
    public VectorSensorProperty compass = new VectorSensorProperty(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, 3);

    Sensors(Environment environment, Context context) {
        super(environment);
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorHandlerThread = new HandlerThread("SensorHandlerThread");
        sensorHandlerThread.start();
        sensorHandler = new Handler(sensorHandlerThread.getLooper());
    }

    @Override
    public InstanceType getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 0: return temperature;
            case 1: return light;
            case 2: return proximity;
            case 3: return pressure;
            case 4: return humidity;
            case 5: return heartRate;
            case 6: return heartBeat;

            case 7: return acceleration;
            case 8: return gravity;
            case 9: return gyroscope;
            case 10: return orientation;
            case 11: return rotation;
            case 12: return compass;

            default:
                throw new IllegalArgumentException("property index: " + index);
        }
    }

    @Override
    public void getDependencies(DependencyCollector result) {

    }


    abstract class SensorProperty<T> extends Property<T> implements SensorEventListener {
        T value;
        int sensorType;
        long timeStamp;
        android.hardware.Sensor sensor;
        SensorEventListener sensorEventListener;

        SensorProperty(int sensorType) {
            this.sensorType = sensorType;
        }

        public void addListener(Property.PropertyListener<T> listener) {
            if (!hasListeners()) {
                if (sensor == null) {
                    this.sensor = sensorManager.getDefaultSensor(sensorType);
                }
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
            }
            super.addListener(listener);
        }

        public void removeListener(PropertyListener<T> listener) {
            super.removeListener(listener);
            if (!hasListeners()) {
                sensorManager.unregisterListener(this);
            }
        }

        @Override
        public T get() {
            synchronized (SensorProperty.this) {
                if (System.currentTimeMillis() - timeStamp > 100) {
                    final PropertyListener<T> oneOff = new PropertyListener<T>() {
                        @Override
                        public void valueChanged(Property<T> property, T oldValue, T newValue) {
                            synchronized (SensorProperty.this) {
                                SensorProperty.this.notify();
                            }
                        }
                    };
                    addListener(oneOff);
                    try {
                        SensorProperty.this.wait(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    removeListener(oneOff);
                }
            }
            return value;
        }

        abstract T convertSensorData(float[] data);

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            T oldValue = value;
            value = convertSensorData(sensorEvent.values);
            timeStamp = System.currentTimeMillis();
            if (!value.equals(oldValue)) {
                SensorProperty.this.notifyChanged(oldValue, value);
            }
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {
        }
    }


    public String toString() {
        return "sensors";
    }

    class ScalarSensorProperty extends SensorProperty<Double> {
        ScalarSensorProperty(int sensorType) {
            super(sensorType);
            value = 0.0;
        }

        @Override
        Double convertSensorData(float[] data) {
            return (double) data[0];
        }
    }

    class VectorSensorProperty extends SensorProperty<List<Double>> {
        int size;

        VectorSensorProperty(int sensorType, int size) {
            super(sensorType);
            this.size = size;
            value = convertSensorData(new float[size]);
        }

        @Override
        List<Double> convertSensorData(float[] data) {
            System.out.println(Arrays.toString(data));
            ArrayList<Double> result = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                result.add((double) data[i]);
            }
            System.out.println(result);
            return result;
        }
    }

}
