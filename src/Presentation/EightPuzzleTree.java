package Presentation;

import aima.core.environment.eightpuzzle.EightPuzzleBoard;
import aima.core.search.framework.Node;
import java.util.Vector;

public class EightPuzzleTree {
    public static Vector<Vector<TreeNode>> tree = new Vector<Vector<TreeNode>>();

    public static void addToTree(Node state) {
        System.out.println("Node depth: " + state.getDepth());
        if (state.getDepth() > (tree.size() - 1)) {
            tree.add(new Vector<TreeNode>());
        }
        tree.get(((int) state.getDepth())).add(new TreeNode(state));
    }

    public static void clearTree() {
        tree.clear();
    }
    public static class TreeNode {
        EightPuzzleApp.EightPuzzleView view;
        Node node;

        TreeNode() {
            view = null;
            node = null;
        }

        TreeNode(Node state) {
            node = state;
            view = new EightPuzzleApp.EightPuzzleView();
            view.copyStateFromNode(state);
        }
    }
}
