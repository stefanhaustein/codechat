package org.kobjects.codechat.android.api.gpio;

import com.google.android.things.pio.PeripheralManagerService;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.AbstractInstance;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

public class DigitalOutput extends AbstractInstance {

     public static InstanceType<DigitalOutput> TYPE = new InstanceType<DigitalOutput>(false) {
         @Override
         public String getName() {
             return "DigitalOutput";
         }

         @Override
         public DigitalOutput createInstance(Environment environment) {
            return new DigitalOutput(environment);
        }
    };

    static {
        TYPE.addProperty(0, "name", Type.STRING, true,
                new AnnotatedStringBuilder().append("The name of the pin."));
        TYPE.addProperty(1, "value", Type.BOOLEAN, true,
                new AnnotatedStringBuilder().append("The current output value."));
    }

    private com.google.android.things.pio.Gpio gpio;
    private boolean value;

    Property<String> nameProperty = new Property<String>() {
        @Override
        public String get() {
            return gpio == null ? "" : gpio.getName();
        }
        @Override
        public void set(String name) {
            if (gpio != null) {
                try {
                    gpio.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                gpio = null;
            }
            if (!name.isEmpty()) {
                try {
                    gpio = new PeripheralManagerService().openGpio(name);
                    gpio.setDirection(value ? com.google.android.things.pio.Gpio.DIRECTION_OUT_INITIALLY_HIGH : com.google.android.things.pio.Gpio.DIRECTION_OUT_INITIALLY_LOW);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Property<Boolean> valueProperty = new Property<Boolean>() {
        @Override
        public Boolean get() {
            return value;
        }
        @Override
        public void set(Boolean newValue) {
            value = newValue;
            if (gpio != null) {
                try {
                    gpio.setValue(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public DigitalOutput(Environment environment) {
        super(environment);
    }

    @Override
    public InstanceType<?> getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 0: return nameProperty;
            case 1: return valueProperty;
            default:
                throw new IllegalArgumentException();
        }
    }
}
