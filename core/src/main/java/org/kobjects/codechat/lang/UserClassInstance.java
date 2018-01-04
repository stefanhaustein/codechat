package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.UserClassType;

public class UserClassInstance extends AbstractInstance {
  UserClassType type;
  Property[] properties;

  public UserClassInstance(Environment environment, UserClassType type) {
    super(environment);
    this.type = type;
    java.util.Collection<InstanceType<UserClassInstance>.PropertyDescriptor> propertyDescriptors = type.properties();
    properties = new Property[propertyDescriptors.size()];
    for (InstanceType.PropertyDescriptor descriptor : propertyDescriptors) {
      // InstanceType.PropertyDescriptor descriptor = type.getProperty();
      Expression initializer = descriptor.initializer;
      Object value = initializer == null ? null : initializer.eval(new EvaluationContext(environment, 0));
      properties[descriptor.index] = new MaterialProperty(value);
    }
  }

  @Override
  public Property getProperty(int index) {
    return properties[index];
  }

  @Override
  public InstanceType<?> getType() {
    return type;
  }
}
