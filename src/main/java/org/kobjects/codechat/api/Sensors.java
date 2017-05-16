package org.kobjects.codechat.api;

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
import org.kobjects.codechat.lang.Property;

import static android.content.Context.SENSOR_SERVICE;

public class Sensors {
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

    Sensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorHandlerThread = new HandlerThread("SensorHandlerThread");
        sensorHandlerThread.start();
        sensorHandler = new Handler(sensorHandlerThread.getLooper());
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
