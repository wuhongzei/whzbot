package org.example.whzbot.helper;

import java.util.ArrayList;

/**
 * Basic tree using list of node objects.
 */
public class TreeHelper {
    public int cursor = -1;

    ArrayList<TreeNode> array;

    public TreeHelper() {
        this.array = new ArrayList<>();
    }

    /**
     * put a node into this tree.
     * @param prev the parent of the node. if root, put -1
     * @param node a new node object with value in it.
     * @return the index of the newly put node.
     */
    public int put(int prev, TreeNode node) {
        node.prev = prev;
        array.add(node);
        this.cursor = array.size() - 1;
        return this.cursor;
    }

    /**
     * Wrapped function to put value in tree.
     * New node will be put as child of cursor,
     * and cursor will be maintained to be the new node.
     * @param val value of the new node.
     * @return the index of the new node.
     */
    public int put(int val) {
        TreeNode node = new TreeNode(val);
        node.prev = this.cursor;
        array.add(node);
        this.cursor = this.array.size() - 1;
        return this.cursor;
    }

    /**
     * get a node by index and return its value.
     * @param loc location/index of a node
     * @return Node at such location.
     */
    public TreeNode get(int loc) {
        return array.get(loc);
    }

    public TreeNode getPrev(int loc) {
        return array.get(array.get(loc).prev);
    }

    /**
     * Make a branch on the tree.
     * The new node will be a sibling of cursor.
     * @param val value of a branched node.
     * @return index of the new node.
     */
    public int branch(int val) {
        TreeNode node = new TreeNode(val);
        TreeNode sib = this.array.get(this.cursor);
        node.prev = sib.prev;
        node.sibling = this.cursor;
        array.add(node);
        this.cursor = array.size() - 1;
        return this.cursor;
    }

    /**
     * Simple helper object to put in an array, useful to construct a tree/linked list.
     *
     */
    public static class TreeNode {
        public int prev = -1; // parent node
        public int next = -1; // the main child node.
        public int sibling = -1;
        public int val;

        /**
         *  Basic Constructor for a tree node.
         *  prev and next are init to -1.
         * @param ipt input of value.
         */
        public TreeNode(int ipt) {
            this.val = ipt;
        }
    }
}
