package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.lang.SerializationContext;
import org.kobjects.codechat.type.UserClassType;

public class ClassDeclaration extends AbstractStatement {

  private final RootVariable variable;
  private final UserClassType userClassType;

  public ClassDeclaration(RootVariable variable, UserClassType userClassType) {
    this.variable = variable;
    this.userClassType = userClassType;
  }

  @Override
  public Object eval(EvaluationContext context) {
    variable.value = userClassType;
    context.environment.constants.put(userClassType, variable.name);
    return KEEP_GOING;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, int indent) {
    asb.append("class ").append(variable.name).append(":\n");
    userClassType.printBody(asb);
    asb.append("end\n");
  }

  @Override
  public void getDependencies(DependencyCollector result) {

  }
}
