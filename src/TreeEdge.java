/**
 * Created by sbk on 15.09.15.
 */
public class TreeEdge {
    private Value value;
    private TreeNode child;

    public TreeEdge(Value value, TreeNode child) {
        this.value = value;
        this.child = child;
    }

    public Value getValue() {
        return value;
    }

    public TreeNode getChild() {
        return child;
    }
}
