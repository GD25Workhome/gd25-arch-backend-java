package com.lance.testall.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 2-3 树（教学用实现）。
 * <p>
 * 规则摘要：
 * <ul>
 *   <li>2-节点：1 个键，2 个子指针（叶子则无子指针）</li>
 *   <li>3-节点：2 个键，3 个子指针</li>
 *   <li>插入落到叶子；满（3 键）则分裂，中间键上推</li>
 *   <li>所有叶子在同一层</li>
 * </ul>
 * 对应学习文档：{@code ai_docs/tree/26052902-阶段3-23树与234树.md}
 */
public class TwoThreeTree {

    /** 节点最多 2 个键；插入临时溢出为 3 个键后立刻分裂 */
    private static final int MAX_KEYS = 2;

    private Node root;

    /**
     * 动态插入一个键。
     *
     * @param key 待插入整数
     * @return 若发生根分裂导致树增高，返回 true
     */
    public boolean insert(int key) {
        if (contains(key)) {
            return false;
        }
        if (root == null) {
            root = Node.leaf(key);
            return true;
        }
        SplitResult split = insertRecursive(root, key);
        if (split != null) {
            // 根分裂：中间键成为新根，树高 +1
            root = Node.internal(split.promotedKey(), split.left(), split.right());
            return true;
        }
        return true;
    }

    /** 查找键是否存在 */
    public boolean contains(int key) {
        return search(root, key);
    }

    /** 中序遍历，结果应为升序 */
    public List<Integer> inOrder() {
        List<Integer> result = new ArrayList<>();
        collectInOrder(root, result);
        return result;
    }

    /** 树高（空树为 0） */
    public int height() {
        return heightOf(root);
    }

    /** 是否为空 */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * 文本化打印树结构，便于观察插入过程。
     */
    public String formatTree() {
        if (root == null) {
            return "(空树)";
        }
        StringBuilder sb = new StringBuilder();
        appendTree(sb, root, "", true);
        return sb.toString().stripTrailing();
    }

    @Override
    public String toString() {
        return formatTree();
    }

    // ------------------------- 插入核心 -------------------------

    /**
     * 递归插入。
     *
     * @return 若当前子树根发生分裂，返回分裂结果；否则 null
     */
    private SplitResult insertRecursive(Node node, int key) {
        if (node.isLeaf()) {
            node.addKeySorted(key);
            if (node.keyCount() <= MAX_KEYS) {
                return null;
            }
            return splitNode(node);
        }

        int childIndex = node.childIndexFor(key);
        SplitResult childSplit = insertRecursive(node.children[childIndex], key);
        if (childSplit == null) {
            return null;
        }

        // 子节点分裂：将中间键并入当前节点，原左右子树挂在 promoted 两侧
        node.insertPromotedChild(
                childSplit.promotedKey(),
                childSplit.left(),
                childSplit.right(),
                childIndex
        );
        if (node.keyCount() <= MAX_KEYS) {
            return null;
        }
        return splitNode(node);
    }

    /**
     * 节点已有 3 个键（溢出）时分裂。
     * <pre>
     * [a | b | c]  →  左 [a]  上推 b  右 [c]
     * </pre>
     */
    private SplitResult splitNode(Node node) {
        int a = node.keys[0];
        int b = node.keys[1];
        int c = node.keys[2];

        Node left;
        Node right;
        if (node.isLeaf()) {
            left = Node.leaf(a);
            right = Node.leaf(c);
        } else {
            left = Node.internal(a, node.children[0], node.children[1]);
            right = Node.internal(c, node.children[2], node.children[3]);
        }
        return new SplitResult(left, b, right);
    }

    // ------------------------- 查找 / 遍历 -------------------------

    private boolean search(Node node, int key) {
        if (node == null) {
            return false;
        }
        for (int i = 0; i < node.keyCount(); i++) {
            if (node.keys[i] == key) {
                return true;
            }
        }
        if (node.isLeaf()) {
            return false;
        }
        if (key < node.keys[0]) {
            return search(node.children[0], key);
        }
        if (node.keyCount() == 1) {
            return search(node.children[1], key);
        }
        if (key < node.keys[1]) {
            return search(node.children[1], key);
        }
        return search(node.children[2], key);
    }

    private void collectInOrder(Node node, List<Integer> out) {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            for (int i = 0; i < node.keyCount(); i++) {
                out.add(node.keys[i]);
            }
            return;
        }
        collectInOrder(node.children[0], out);
        out.add(node.keys[0]);
        if (node.keyCount() == 1) {
            collectInOrder(node.children[1], out);
        } else {
            collectInOrder(node.children[1], out);
            out.add(node.keys[1]);
            collectInOrder(node.children[2], out);
        }
    }

    private int heightOf(Node node) {
        if (node == null) {
            return 0;
        }
        if (node.isLeaf()) {
            return 1;
        }
        int maxChild = 0;
        for (int i = 0; i <= node.keyCount(); i++) {
            if (node.children[i] != null) {
                maxChild = Math.max(maxChild, heightOf(node.children[i]));
            }
        }
        return maxChild + 1;
    }

    private void appendTree(StringBuilder sb, Node node, String prefix, boolean isTail) {
        sb.append(prefix).append(isTail ? "└── " : "├── ");
        sb.append(node.formatKeys()).append('\n');
        if (node.isLeaf()) {
            return;
        }
        int childSlots = node.keyCount() + 1;
        for (int i = 0; i < childSlots; i++) {
            if (node.children[i] == null) {
                continue;
            }
            boolean last = (i == childSlots - 1) || allRemainingNull(node.children, i + 1);
            appendTree(sb, node.children[i], prefix + (isTail ? "    " : "│   "), last);
        }
    }

    private boolean allRemainingNull(Node[] children, int from) {
        for (int i = from; i < children.length; i++) {
            if (children[i] != null) {
                return false;
            }
        }
        return true;
    }

    // ------------------------- 分裂结果 -------------------------

    private record SplitResult(Node left, int promotedKey, Node right) {
    }

    // ------------------------- 节点 -------------------------

    /**
     * 2-3 树节点：最多 2 个有效键；分裂前临时允许 3 个键。
     */
    static final class Node {

        /** 键数组，有效长度由 keyCount 决定 */
        private final int[] keys = new int[3];
        private int keyCount;

        /** 非叶子：children.length == keyCount + 1；叶子：children 全为 null */
        private final Node[] children = new Node[4];

        private Node(int keyCount) {
            this.keyCount = keyCount;
        }

        static Node leaf(int key) {
            Node node = new Node(1);
            node.keys[0] = key;
            return node;
        }

        static Node leaf(int k1, int k2) {
            Node node = new Node(2);
            node.keys[0] = k1;
            node.keys[1] = k2;
            return node;
        }

        static Node internal(int key, Node left, Node right) {
            Node node = new Node(1);
            node.keys[0] = key;
            node.children[0] = left;
            node.children[1] = right;
            return node;
        }

        boolean isLeaf() {
            return children[0] == null && children[1] == null && children[2] == null && children[3] == null;
        }

        int keyCount() {
            return keyCount;
        }

        /** 在叶子中有序插入 */
        void addKeySorted(int key) {
            if (keyCount == 0) {
                keys[0] = key;
                keyCount = 1;
                return;
            }
            int pos = 0;
            while (pos < keyCount && keys[pos] < key) {
                pos++;
            }
            for (int i = keyCount; i > pos; i--) {
                keys[i] = keys[i - 1];
            }
            keys[pos] = key;
            keyCount++;
        }

        /**
         * 子节点分裂后，将 promoted 键插入当前内部节点，并接上左右子树。
         */
        void insertPromotedChild(int promoted, Node left, Node right, int childIndex) {
            // 先替换 childIndex 位置为 left，再在 promoted 右侧插入 right
            children[childIndex] = left;

            int keyPos = childIndex;
            for (int i = keyCount; i > keyPos; i--) {
                keys[i] = keys[i - 1];
            }
            keys[keyPos] = promoted;
            keyCount++;

            for (int i = keyCount; i > keyPos + 1; i--) {
                children[i] = children[i - 1];
            }
            children[keyPos + 1] = right;
        }

        /** 根据 key 选择下落子树下标 */
        int childIndexFor(int key) {
            if (key < keys[0]) {
                return 0;
            }
            if (keyCount == 1) {
                return 1;
            }
            if (key < keys[1]) {
                return 1;
            }
            return 2;
        }

        String formatKeys() {
            if (keyCount == 1) {
                return "[" + keys[0] + "]";
            }
            if (keyCount == 2) {
                return "[" + keys[0] + " | " + keys[1] + "]";
            }
            return "[" + keys[0] + " | " + keys[1] + " | " + keys[2] + "]";
        }

        @Override
        public String toString() {
            return formatKeys();
        }
    }
}
