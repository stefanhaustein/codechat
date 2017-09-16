package org.kobjects.codechat.lang;

import java.util.ArrayList;

public class Closure {

    private int varCount = -1;
    private ArrayList<Mapping> mappings = new ArrayList<>();

    public void addMapping(String name, int originalIndex, int closureIndex) {
        mappings.add(new Mapping(name, originalIndex, closureIndex));
    }

    public void setVarCount(int varCount) {
        this.varCount = varCount;
    }

    public EvaluationContext createEvalContext(EvaluationContext original) {
        EvaluationContext result = new EvaluationContext(original.environment, varCount);
        for (Mapping mappping : mappings) {
            result.variables[mappping.closureIndex] = original.variables[mappping.originalIndex];
        }
        return result;
    }

    public int getMappingCount() {
        return mappings.size();
    }

    public int getVarCount() {
        return varCount;
    }

    public Iterable<Mapping> getMappings() {
        return mappings;
    }


    public static class Mapping {
        final String name;
        final int originalIndex;
        final int closureIndex;

        public Mapping(String name, int originalIndex, int closureIndex) {
            this.name = name;
            this.originalIndex = originalIndex;
            this.closureIndex = closureIndex;
        }
    }


    public boolean toString(StringBuilder sb, EvaluationContext contextTemplate){
        if (getMappingCount() == 0) {
            return false;
        }
        sb.append("begin\n");
        for (Closure.Mapping mapping : getMappings()) {
            sb.append("  var ").append(mapping.name).append(" = ");
            sb.append(contextTemplate.variables[mapping.closureIndex]).append(";\n");
        }
        sb.append("  ");
        return true;
    }

}
