package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.type.UserClassType;

public class ClassDeclaration extends AbstractStatement {

  private final UserClassType userClassType;

  public ClassDeclaration(UserClassType userClassType) {
    this.userClassType = userClassType;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return KEEP_GOING;
  }

  @Override
  public void toString(AnnotatedStringBuilder sb, int indent) {
    userClassType.print(sb, Printable.Flavor.DEFAULT);
  }

  @Override
  public void getDependencies(DependencyCollector result) {

  }
}
