package org.kobjects.codechat.lang;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.expr.PropertyAccess;

import java.util.ArrayList;
import java.util.List;

import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

public class OnInstance extends Instance implements Property.PropertyListener {
    private List<Property> properties = new ArrayList<>();
    private Object lastValue = Boolean.FALSE;
//    private OnExpression onExpression;
    private EvaluationContext contextTemplate;
    private Timer timer;
    private OnExpression.Kind kind;
    private Expression trigger;
    private Statement body;
    private Closure closure;
    static Set<OnInstance> allOnInterval = new HashSet<OnInstance>();

    static Expression resolve(Expression expression, EvaluationContext context) {
        if (expression instanceof PropertyAccess) {
            PropertyAccess propertyAccess = (PropertyAccess) expression;
            Object base = propertyAccess.getChild(0).eval(context);
            Expression baseExpr;
          /*  if (base instanceof Instance) {
                baseExpr = new InstanceReference(((Instance) base).getType(), ((Instance) base).getId());
            } else { */
                baseExpr = new Literal(base);
            //}
            return new PropertyAccess(baseExpr, propertyAccess.property);
        } else {
            Expression[] children = new Expression[expression.getChildCount()];
            for (int i = 0; i < children.length; i++) {
                children[i] = resolve(expression.getChild(i), context);
            }
            return expression.reconstruct(children);
        }
    }

    public OnInstance(Environment environment, int id, OnExpression.Kind kind) {
        super(environment, id);
        this.kind = kind;
    }

    public void init(OnExpression onExpression, final EvaluationContext contextTemplate) {
        detach();
        this.closure = onExpression.closure;
        this.contextTemplate = contextTemplate;
        this.body = onExpression.body;
        this.trigger = resolve(onExpression.expression, contextTemplate);
        if (onExpression.kind == OnExpression.Kind.ON_INTERVAL) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!contextTemplate.environment.paused) {
                        EvaluationContext evalContext = OnInstance.this.contextTemplate.clone();
                        OnInstance.this.body.eval(evalContext);
                    }
                }
            }, 0, Math.round(((Number) trigger.eval(contextTemplate)).doubleValue()*1000));
            synchronized (allOnInterval) {
                allOnInterval.add(this);
            }
        } else {
            System.err.println("AddAll for trigger: " + trigger);
            addAll(trigger, contextTemplate);
        }
    }

    @Override
    public void valueChanged(Property property, Object oldValue, Object newValue) {
        switch (kind) {
            case ON_CHANGE: {
                EvaluationContext evalContext = contextTemplate.clone();
                body.eval(evalContext);
                break;
            }
            case ON: {
                Object conditionValue = trigger.eval(contextTemplate);
                if (!conditionValue.equals(lastValue)) {
                    lastValue = conditionValue;
                    if (Boolean.TRUE.equals(conditionValue)) {
                        EvaluationContext evalContext = contextTemplate.clone();
                        body.eval(evalContext);
                    }
                }
                break;
            }
            default:
                throw new IllegalStateException();
        }
    }

    public void detach() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            synchronized (allOnInterval) {
                allOnInterval.remove(this);
            }
        }
        for (Property property : properties) {
            property.removeListener(this);
        }
    }

    private void addAll(Expression expr, EvaluationContext context) {
        if (expr instanceof PropertyAccess) {
            Property property = ((PropertyAccess) expr).getProperty(context);
            property.addListener(this);
            properties.add(property);
        }
        for (int i = 0; i < expr.getChildCount(); i++) {
            addAll(expr.getChild(i), context);
        }
    }

    public void delete() {
        detach();
    }

    @Override
    public OnInstanceType getType() {
        return kind.type;
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        serializationContext.setSerialized(this);

        boolean wrap = closure.toString(asb.getStringBuilder(), contextTemplate);

        asb.append(kind.type.getName().toLowerCase() + "#" + String.valueOf(getId()), new EntityLink(this));
        asb.append(" ").append(trigger.toString()).append(":\n");
        body.toString(asb, wrap ? 2 : 1);
        if (wrap) {
            asb.append("  end;\n");
        }
        asb.append("end;\n");
    }

    @Override
    public Property getProperty(int index) {
        return null;
    }

    public static class OnInstanceType extends InstanceType {
        private String name;
        public OnExpression.Kind kind;

        public OnInstanceType(String name) {
            this.name = name;
        }

        @Override
        public OnInstance createInstance(Environment environment, int id) {
            return new OnInstance(environment, id, kind);
        }

        @Override
        public boolean isAssignableFrom(Type other) {
            return false;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    @Override
    public void getDependencies(DependencyCollector result) {
        if (trigger != null) {
            trigger.getDependencies(result);
        }
        if (body != null) {
            body.getDependencies(result);
        }
    }
}
