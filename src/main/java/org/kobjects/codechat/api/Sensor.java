package org.kobjects.codechat.api;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import org.kobjects.codechat.lang.Property;

import static android.content.Context.SENSOR_SERVICE;

public class Sensor {
    SensorManager sensorManager;

    HandlerThread sensorHandlerThread;
    Handler sensorHandler;

    public SimpleSensorProperty temperature = new SimpleSensorProperty(android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE);
    public SimpleSensorProperty light = new SimpleSensorProperty(android.hardware.Sensor.TYPE_LIGHT);
    public SimpleSensorProperty proximity = new SimpleSensorProperty(android.hardware.Sensor.TYPE_PROXIMITY);
    public SimpleSensorProperty pressure = new SimpleSensorProperty(android.hardware.Sensor.TYPE_PRESSURE);
    public SimpleSensorProperty humidity = new SimpleSensorProperty(android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY);
    public SimpleSensorProperty heartRate = new SimpleSensorProperty(android.hardware.Sensor.TYPE_HEART_RATE);
    public SimpleSensorProperty heartBeat = new SimpleSensorProperty(android.hardware.Sensor.TYPE_HEART_BEAT);

    Sensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorHandlerThread = new HandlerThread("SensorHandlerThread");
        sensorHandlerThread.start();
        sensorHandler = new Handler(sensorHandlerThread.getLooper());
    }


    class SimpleSensorProperty extends Property<Double> implements SensorEventListener {
        Double value = 0.0;
        int sensorType;
        long timeStamp;
        android.hardware.Sensor sensor;
        SensorEventListener sensorEventListener;

        SimpleSensorProperty(int sensorType) {
            this.sensorType = sensorType;
        }

        public void addListener(Property.PropertyListener<Double> listener) {
            if (!hasListeners()) {
                if (sensor == null) {
                    this.sensor = sensorManager.getDefaultSensor(sensorType);
                }
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
            }
            super.addListener(listener);
        }

        public void removeListener(PropertyListener<Double> listener) {
            super.removeListener(listener);
            if (!hasListeners()) {
                sensorManager.unregisterListener(this);
            }
        }

        @Override
        public Double get() {
            synchronized (SimpleSensorProperty.this) {
                if (System.currentTimeMillis() - timeStamp > 100) {
                    final PropertyListener<Double> oneOff = new PropertyListener<Double>() {
                        @Override
                        public void valueChanged(Property<Double> property, Double oldValue, Double newValue) {
                            synchronized (SimpleSensorProperty.this) {
                                SimpleSensorProperty.this.notify();
                            }
                        }
                    };
                    addListener(oneOff);
                    try {
                        SimpleSensorProperty.this.wait(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    removeListener(oneOff);
                }
            }
            return value;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Double oldValue = value;
            value = (double) sensorEvent.values[0];
            timeStamp = System.currentTimeMillis();
            if (!value.equals(oldValue)) {
                SimpleSensorProperty.this.notifyChanged(oldValue, value);
            }
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {

        }
    }

}
