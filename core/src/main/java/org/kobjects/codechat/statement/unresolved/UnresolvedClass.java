package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Statement;

public class UnresolvedClass extends UnresolvedStatement {


  private final String name;

  public UnresolvedClass(String className) {
    this.name = className;
  }

  @Override
  public void toString(AnnotatedStringBuilder sb, int indent) {
    throw new RuntimeException("NYI");
  }

  public void addField(UnresolvedField unresolvedField) {

  }

  public void addMethod(UnresolvedMethod unresolvedField) {

  }
  @Override
  public Statement resolve(ParsingContext parsingContext) {
    throw new RuntimeException("NYI");
  }

  public static class UnresolvedField {
    private final UnresolvedExpression initializer;
    private final String name;

    public UnresolvedField(String name, UnresolvedExpression initializer) {
      this.name = name;
      this.initializer = initializer;
    }
  }

  public static class UnresolvedMethod {

  }
}
