package org.kobjects.codechat.tree;

import java.util.List;
import org.kobjects.codechat.Environment;

public abstract class Node {
    public Node[] children;

    public Node(Node... children) {
        this.children = children;
    }

    public Node(List<Node> children) {
        this.children = new Node[children.size()];
        for (int i = 0; i < this.children.length; i++) {
            this.children[i] = children.get(i);
        }
    }

    public abstract Object eval(Environment environment);

    public void assign(Environment environment, Object value) {
        throw new RuntimeException("Assignment not supported for " + this);
    }
}
