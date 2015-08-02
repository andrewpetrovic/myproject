package com.itic.mobile.util.data;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;

/**
 * ArrayList 工具类
 */
public class Lists {

    /**
     * 创建一个空的ArrayList
     * @return  {@code ArrayList}
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    /**
     * 创建一个包含给定元素的ArrayList
     *
     * @param elements ArrayList中包含的元素
     * @return 包含给定元素的ArrayList
     */
    public static <E> ArrayList<E> newArrayList(E... elements) {
        int capacity = (elements.length * 110) / 100 + 5;
        ArrayList<E> list = new ArrayList<E>(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    /** 克隆一个SparseArray. */
    public static <E> SparseArray<E> cloneSparseArray(SparseArray<E> orig) {
        SparseArray<E> result = new SparseArray<E>();
        for (int i = 0; i < orig.size(); i++) {
            result.put(orig.keyAt(i), orig.valueAt(i));
        }
        return result;
    }
}
