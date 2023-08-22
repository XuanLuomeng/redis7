package com.gduf.red_lock.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * @author LuoXuanwei
 * @date 2023/8/21 21:20
 */
public class JavaDemo {
    public static void main(String[] args) {
        new HashSet<>().add("a");
        new ArrayList<>().add("a");

        //redis结构

        /**
         * redis6相关的底层模型和结构
         * String = SDS
         * Set = inset + hashtable
         * ZSet = skipList + zipList
         * List = quickList + zipList
         * Hash = hashtable +zipList
         *
         * =================================
         *
         * redis7相关的底层模型和结构(不再使用zipList压缩列表)
         * String = SDS
         * Set = inset + hashtable
         * ZSet = skipList + listpack紧凑列表
         * List = quickList
         * Hash = hashtable + listpack
         */

        /**redis6相关底层模型和结构**/
        new HashMap<>().put(1, "abc");

        new LinkedList<>().add(1);

        /**
         *
         * java list
         * ArrayList ===> Object[]
         * LinkedList ===> 放入node节点的一个双端链表
         *
         * redis list
         *  都是双端链表结构，借鉴java思想，redis也给用户新建了一个权限的数据结构，俗称
         * 1 redis6 ===》
         * 2 redis7 ===》    quickList
         *
         * 总纲
         *
         * 分
         */
    }
}
