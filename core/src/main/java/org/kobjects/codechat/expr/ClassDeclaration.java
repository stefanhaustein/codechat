package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.lang.SerializationContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.UserClassType;

public class ClassDeclaration extends Expression {

  private final UserClassType userClassType;

  public ClassDeclaration(UserClassType userClassType) {
    this.userClassType = userClassType;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return userClassType;
  }

  @Override
  public Type getType() {
    return userClassType.getType();
  }

  @Override
  public int getPrecedence() {
    return 0;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, int indent) {
    asb.append("class").append(":\n");
    printBody(asb, indent);
    asb.indent(indent);
    asb.append("end\n");
  }

  public void printBody(AnnotatedStringBuilder asb, int indent) {
    userClassType.printBody(asb);
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public void getDependencies(DependencyCollector result) {

  }

  @Override
  public Expression reconstruct(Expression... children) {
    throw new UnsupportedOperationException();
  }
}
