package com.lance.testall.tree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 2-3 树动态插入演示与断言。
 * <p>
 * 运行本测试时请关注控制台输出：每插入一个键会打印当前树形与中序序列。
 * 建议对照 {@code ai_docs/tree/26052902-阶段3-23树与234树.md} 纸笔推演。
 */
class TwoThreeTreeDemoTest {

    /**
     * 主演示：按序动态插入，逐步观察分裂与根变化。
     */
    @Test
    void demo_dynamicInsert_printEachStep() {
        TwoThreeTree tree = new TwoThreeTree();
        int[] sequence = {10, 20, 30, 5, 15, 25, 40, 1, 35};

        System.out.println("========== 2-3 树动态插入演示 ==========");
        for (int key : sequence) {
            tree.insert(key);
            System.out.println();
            System.out.println(">>> 插入 " + key + " 后 <<<");
            System.out.println(tree.formatTree());
            System.out.println("中序（应升序）: " + tree.inOrder());
            System.out.println("树高: " + tree.height());
            System.out.println("包含 " + key + "? " + tree.contains(key));
        }
        System.out.println();
        System.out.println("========== 演示结束 ==========");
    }

    /**
     * 对应学习文档过关题：插入 10, 20, 30。
     * 第三次插入应分裂，根变为 20。
     */
    @Test
    void insert_10_20_30_rootBecomes20_afterSplit() {
        TwoThreeTree tree = new TwoThreeTree();

        tree.insert(10);
        assertEquals(List.of(10), tree.inOrder());
        assertEquals(1, tree.height());

        tree.insert(20);
        assertEquals(List.of(10, 20), tree.inOrder());
        assertEquals(1, tree.height());

        tree.insert(30);
        assertEquals(List.of(10, 20, 30), tree.inOrder());
        assertEquals(2, tree.height());
        assertTrue(tree.contains(20));
        // 分裂后：根 [20]，左右叶 [10]、[30]
        String treeText = tree.formatTree();
        assertTrue(treeText.startsWith("└── [20]"), "根应为 [20]，实际:\n" + treeText);
        assertTrue(treeText.contains("[10]") && treeText.contains("[30]"));
    }

    /**
     * 升序 1..7：不应退化成 BST 链，高度应远小于节点数。
     */
    @Test
    void insert_sorted1To7_heightIsLogarithmic_notChain() {
        TwoThreeTree tree = new TwoThreeTree();
        for (int i = 1; i <= 7; i++) {
            tree.insert(i);
        }
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), tree.inOrder());
        assertTrue(tree.height() < 7, "2-3 树不应链化到高度 7");
        assertTrue(tree.height() <= 3);
    }

    @Test
    void duplicateInsert_ignored() {
        TwoThreeTree tree = new TwoThreeTree();
        assertTrue(tree.insert(5));
        assertFalse(tree.insert(5));
        assertEquals(List.of(5), tree.inOrder());
    }

    @Test
    void search_afterMultipleInserts() {
        TwoThreeTree tree = new TwoThreeTree();
        for (int k : new int[]{10, 20, 30, 5, 15}) {
            tree.insert(k);
        }
        assertTrue(tree.contains(15));
        assertFalse(tree.contains(99));
    }
}
