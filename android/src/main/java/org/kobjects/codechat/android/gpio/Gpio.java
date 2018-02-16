package org.kobjects.codechat.android.gpio;

import com.google.android.things.pio.PeripheralManagerService;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.AbstractInstance;
import org.kobjects.codechat.lang.Collection;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.Type;

public class Gpio extends AbstractInstance {

    private final PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

    public static final InstanceType<Gpio> TYPE = new InstanceType<Gpio>(true) {
        @Override
        public String toString() {
            return "Gpio";
        }
        @Override
        public void printDocumentationBody(AnnotatedStringBuilder asb) {
            asb.append("The pio object contains information about the periperal hardware io pins available.");
        }
    };

    static {
        TYPE.addProperty(0, "digital", new ListType(Type.STRING), false,
                new AnnotatedStringBuilder().append("The names of the available gpio pins"));

    }

    Property<Collection> digital = new Property<Collection>() {
        @Override
        public Collection get() {
            Collection result = new Collection(environment, new ListType(Type.STRING));
            result.addAll(peripheralManagerService.getGpioList());
            return result;
        }
    };

    public Gpio(Environment environment) {
        super(environment);
    }

    @Override
    public InstanceType<?> getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        return digital;
    }
}
