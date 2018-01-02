package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.ClassDeclaration;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.UserClassType;

import java.util.ArrayList;

public class UnresolvedClassDeclaration extends UnresolvedStatement {
  private final UserClassType type;
  private ArrayList<UnresolvedField> fields = new ArrayList<>();
  private ArrayList<UnresolvedMethod> methods = new ArrayList<>();

  public UnresolvedClassDeclaration(String className) {
    type = new UserClassType(className);
  }

  @Override
  public void toString(AnnotatedStringBuilder sb, int indent) {
    throw new RuntimeException("NYI");
  }

  public void addField(UnresolvedField unresolvedField) {
    fields.add(unresolvedField);
  }

  public void addMethod(UnresolvedMethod unresolvedMethod) {
    methods.add(unresolvedMethod);
  }

  @Override
  public Statement resolve(ParsingContext parsingContext) {

    int index = 0;
    for (UnresolvedField field: fields) {
      Expression resolvedInitializer = field.initializer.resolve(parsingContext, null);
      type.addProperty(index++, field.name, resolvedInitializer.getType(), true, null, resolvedInitializer);
    }

    for (UnresolvedMethod method: methods) {
      throw new RuntimeException("NYI");
    }

    return new ClassDeclaration(type);
  }

  @Override
  public void resolveTypes(ParsingContext parsingContext) {
    parsingContext.environment.declareRootVariable(type.getName(), type, true);
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
    private final String name;
    private final FunctionType type;
    private final ArrayList<String> paramNames;
    private final UnresolvedStatement body;

    public UnresolvedMethod(String name, FunctionType type, ArrayList<String> paramNames, UnresolvedStatement body) {
      this.name = name;
      this.type = type;
      this.paramNames = paramNames;
      this.body = body;
    }
  }
}
