package org.kobjects.codechat.type;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.instance.Instance;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.instance.Property;
import org.kobjects.codechat.instance.UserClassInstance;

public class UserClassifier extends Classifier<UserClassInstance> implements Instance, Printable {
  private final Environment environment;

  public UserClassifier(Environment environment) {
    this.environment = environment;
  }


  @Override
  public String toString() {
    String name = environment.constants.get(this);
    return name != null ? name : ("Class#" + environment.getId(this));
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
  public Environment getEnvironment() {
    return environment;
  }

  @Override
  public void delete() {

  }

  @Override
  public void getDependencies(DependencyCollector result) {

  }


  @Override
  public void print(AnnotatedStringBuilder asb, Flavor flavor) {
    String name = environment.constants.get(this);
    if (name == null) {
      asb.append("class#");
      asb.append(environment.getId(this));
    } else {
      asb.append("class ").append(name);
    }
    asb.append(":\n");
    printBody(asb);
    asb.append("end\n");
  }

  @Override
  public String getName() {
    return environment.getName(this);
  }
}
