package org.kobjects.codechat.type;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.UserClassInstance;

public class UserClassType extends InstanceType<UserClassInstance> implements Printable {
  private final String name;

  public UserClassType(String name) {
    this.name = name;
  }


  @Override
  public String toString() {
    return name;
  }


  @Override
  public void print(AnnotatedStringBuilder asb, Flavor flavor) {
    asb.append("class ").append(name).append(":\n");
    for (PropertyDescriptor descriptor : properties()) {
      descriptor.print(asb, flavor);
    }
    asb.append("end\n");
  }

  public UserClassInstance createInstance(Environment environment) {
    return new UserClassInstance(environment, this);
  }
}
