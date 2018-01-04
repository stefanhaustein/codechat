package org.kobjects.codechat.type;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.SerializationContext;
import org.kobjects.codechat.lang.UserClassInstance;

public class UserClassType extends InstanceType<UserClassInstance> implements Instance, Printable {
  private final String name;

  public UserClassType(String name) {
    this.name = name;
  }


  @Override
  public String toString() {
    return name;
  }



  public UserClassInstance createInstance(Environment environment) {
    return new UserClassInstance(environment, this);
  }

  @Override
  public Property getProperty(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean needsTwoPhaseSerilaization() {
    return false;
  }

  @Override
  public void delete() {

  }

  @Override
  public void getDependencies(DependencyCollector result) {

  }

  @Override
  public void print(AnnotatedStringBuilder asb, Flavor flavor) {
    asb.append("class ").append(name).append(":\n");
    for (PropertyDescriptor descriptor : properties()) {
      descriptor.print(asb, Printable.Flavor.DEFAULT);
    }
    asb.append("end\n");

  }
}
