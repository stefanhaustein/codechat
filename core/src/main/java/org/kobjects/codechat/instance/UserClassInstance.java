package org.kobjects.codechat.instance;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.UserClassifier;

public class UserClassInstance extends AbstractInstance {
  UserClassifier type;
  Property[] properties;

  public UserClassInstance(Environment environment, UserClassifier type) {
    super(environment);
    this.type = type;
    java.util.Collection<Classifier<UserClassInstance>.PropertyDescriptor> propertyDescriptors = type.properties();
    properties = new Property[propertyDescriptors.size()];
    for (Classifier.PropertyDescriptor descriptor : propertyDescriptors) {
      // Classifier.PropertyDescriptor descriptor = type.getProperty();
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
  public Classifier<?> getType() {
    return type;
  }
}
