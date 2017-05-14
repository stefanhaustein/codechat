package org.kobjects.codechat.lang;

import java.util.ArrayList;

public class Closure {

    private int varCount = -1;
    private ArrayList<Mapping> mappings = new ArrayList<>();

    public void addMapping(String name, int originalIndex, int closureIndex) {
        mappings.add(new Mapping(name, originalIndex, closureIndex));
    }

    void setVarCount(int varCount) {
        this.varCount = varCount;
    }

    public EvaluationContext createEvalContext(EvaluationContext original) {
        EvaluationContext result = new EvaluationContext(original.environment, varCount);
        for (Mapping mappping : mappings) {
            result.variables[mappping.closureIndex] = original.variables[mappping.originalIndex];
        }
        return result;
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

}
