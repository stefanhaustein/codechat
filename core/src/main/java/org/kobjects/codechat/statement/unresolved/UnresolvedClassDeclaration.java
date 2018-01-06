package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.ClassDeclaration;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.UserClassType;

import java.util.ArrayList;

public class UnresolvedClassDeclaration extends UnresolvedStatement {
  private ArrayList<UnresolvedField> fields = new ArrayList<>();
  private ArrayList<UnresolvedMethod> methods = new ArrayList<>();
  private RootVariable variable;
  private String className;
  private UserClassType type;

  public UnresolvedClassDeclaration(String className) {
    this.className = className;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, int indent) {
    asb.append("class ").append(className).append(":\n");
    for (UnresolvedField field: fields) {
      field.toString(asb, indent + 2);
    }
    for (UnresolvedMethod method: methods) {
      method.toString(asb, indent + 2);
    }
    asb.indent(indent);
    asb.append("end\n");
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

    return new ClassDeclaration(variable, type);
  }

  @Override
  public void resolveTypes(ParsingContext parsingContext) {
    type = new UserClassType(parsingContext.environment.getEnvironment());
    variable = parsingContext.environment.declareRootVariable(className, type.getType(), true);
    variable.value = type;
  }

  public static class UnresolvedField {
    private final UnresolvedExpression initializer;
    private final String name;

    public UnresolvedField(String name, UnresolvedExpression initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    public void toString(AnnotatedStringBuilder asb, int indent) {
      asb.indent(indent);
      asb.append(name);
      asb.append(" = ");
      initializer.toString(asb, indent + 4);
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

    public void toString(AnnotatedStringBuilder asb, int indent) {
      asb.indent(indent);
      type.serializeSignature(asb, -1, null, paramNames.toArray(new String[paramNames.size()]), null);
      asb.append(":\n");
      body.toString(asb, indent + 2);
    }
  }
}
