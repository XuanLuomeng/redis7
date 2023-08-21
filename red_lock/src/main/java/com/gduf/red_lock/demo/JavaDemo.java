package com.gduf.red_lock.demo;

import java.util.ArrayList;
import java.util.HashSet;

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
    }
}
