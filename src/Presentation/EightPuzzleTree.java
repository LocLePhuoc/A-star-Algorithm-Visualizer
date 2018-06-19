package Presentation;

import aima.core.environment.eightpuzzle.EightPuzzleBoard;
import aima.core.search.framework.Node;
import java.util.Vector;

public class EightPuzzleTree {
    public static Vector<Vector<EightPuzzleTreeNode>> tree = new Vector<Vector<EightPuzzleTreeNode>>();
    public static int numberOfExpandedNodes = 0;

    public static void addToTree(Node state) {
        if (state.getDepth() > (tree.size() - 1)) {
            tree.add(new Vector<EightPuzzleTreeNode>());
        }
        EightPuzzleTreeNode nodeToAdd = new EightPuzzleTreeNode(state);
        int[] fatherPosition = findFather(state);
        if (fatherPosition != null) {
            nodeToAdd.setFatherPosition(fatherPosition);
        }
        tree.get(((int) state.getDepth())).add(nodeToAdd);
    }

    public static int[] findFather(Node state) {
        if (state.getParent() == null) {
            return null;
        }
        int fatherDepth = state.getParent().getDepth();
        int[] retVal = new int[2];
        for (int i = 0; i < tree.get(fatherDepth).size();i++) {
            EightPuzzleBoard currentState = (EightPuzzleBoard)tree.get(fatherDepth).get(i).node.getState();
            EightPuzzleBoard parentState = (EightPuzzleBoard) state.getParent().getState();
            if (currentState.toString().equals(parentState.toString())) {
                retVal[0] = i;
                retVal[1] = fatherDepth;
                return retVal;
            }
        }
        return null;
    }

    public static void setChosenNodeFromFrontier(Node state, int depth) {
        for (int i = 0; i < tree.get(depth).size(); i++) {
            EightPuzzleBoard currentState = (EightPuzzleBoard)tree.get(depth).get(i).node.getState();
            EightPuzzleBoard stateToFind = (EightPuzzleBoard) state.getState();
            if (currentState.toString().equals(stateToFind.toString())) {
                tree.get(depth).get(i).setIsChosen();
                tree.get(depth).get(i).setOrderChosenFromFrontier(numberOfExpandedNodes);
            }
        }
    }

    public static void setGoalState(Node state, int depth) {
        for (int i = 0; i < tree.get(depth).size(); i++) {
            EightPuzzleBoard currentState = (EightPuzzleBoard)tree.get(depth).get(i).node.getState();
            EightPuzzleBoard stateToFind = (EightPuzzleBoard) state.getState();
            if (currentState.toString().equals(stateToFind.toString())) {
                tree.get(depth).get(i).setIsGoalState();
            }
        }
    }

    public static int[] getCurrentExpandedNode(int currentDepth, int currentChosenOrder) {
        for (int i = 0; i <= currentDepth; i++) {
            for (int j = 0; j < tree.get(i).size(); j++) {
                if (tree.get(i).get(j).orderChosenFromFrontier == currentChosenOrder + 1) {
                    //{depth, position in depth}
                    int[] retVal = {i,j};
                    return retVal;
                }
            }
        }
        return null;
    }

    public static void clearTree() {
        tree.clear();
    }
    public static class EightPuzzleTreeNode {
        EightPuzzleApp.EightPuzzleView view;
        Node node;

        /**
         * the position of father node in the tree
         * X: the position of the father in the same depth
         * Y: the depth of the father
         */
        public int fatherX;
        public int fatherY;

        boolean isChosenFromFrontier;
        int orderChosenFromFrontier;
        boolean isGoalState;

        EightPuzzleTreeNode() {
            view = null;
            node = null;
        }

        EightPuzzleTreeNode(Node state) {
            node = state;
            view = new EightPuzzleApp.EightPuzzleView();
            view.copyStateFromNode(state);
            fatherX = -1;
            fatherY = -1;
            orderChosenFromFrontier = -1;
            isChosenFromFrontier = false;
            isGoalState = false;
        }

        public void setFatherPosition(int[] father) {
            fatherX = father[0];
            fatherY = father[1];
        }

        public void setIsChosen() {
            isChosenFromFrontier = true;
        }

        public boolean isChosen() {
            return isChosenFromFrontier;
        }

        public void setIsGoalState() {
            isGoalState = true;
        }

        public boolean isGoalState() {
            return isGoalState;
        }

        public void setOrderChosenFromFrontier(int order) {
            orderChosenFromFrontier = order;
        }

        public boolean hasParent(int fatherX, int fatherY) {
            return this.fatherX == fatherX && this.fatherY == fatherY;
        }
    }
}
