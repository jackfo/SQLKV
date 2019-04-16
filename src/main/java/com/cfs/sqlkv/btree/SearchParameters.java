package com.cfs.sqlkv.btree;


import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Arrays;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-02 18:53
 */
public class SearchParameters {
    /**
     * 二分查找初始左位置
     */
    public static final int POSITION_LEFT_OF_PARTIAL_KEY_MATCH = 1;
    /**
     * 二分查找初始有位置
     */
    public static final int POSITION_RIGHT_OF_PARTIAL_KEY_MATCH = -1;
    /**
     * 查找值
     */
    public DataValueDescriptor[] searchKey;
    /**
     * 比较操作
     */
    public int partial_key_match_op;
    /**
     *
     */
    public DataValueDescriptor[] template;
    public OpenBTree btree;
    /**
     * 经过二分查找之后返回的槽位
     */
    public int resultSlot;
    /**
     * 如果为true表明查找成功
     */
    public boolean resultExact;
    public boolean searchForOptimizer;
    public float left_fraction;
    public float current_fraction;

    public SearchParameters(DataValueDescriptor[] searchKey, int partial_key_match_op, DataValueDescriptor[] template, OpenBTree btree, boolean searchForOptimizer) {
        this.searchKey = searchKey;
        this.partial_key_match_op = partial_key_match_op;
        this.template = template;
        this.btree = btree;
        this.resultSlot = 0;
        this.resultExact = false;
        this.searchForOptimizer = searchForOptimizer;

        if (this.searchForOptimizer) {
            this.left_fraction = 0;
            this.current_fraction = 1;
        }
    }


    @Override
    public String toString() {
        return "SearchParameters{" +
                "searchKey=" + Arrays.toString(searchKey) +
                ", partial_key_match_op=" + partial_key_match_op +
                ", template=" + Arrays.toString(template) +
                ", btree=" + btree +
                ", resultSlot=" + resultSlot +
                ", resultExact=" + resultExact +
                ", searchForOptimizer=" + searchForOptimizer +
                ", left_fraction=" + left_fraction +
                ", current_fraction=" + current_fraction +
                '}';
    }
}
