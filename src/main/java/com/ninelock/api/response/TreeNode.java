package com.ninelock.api.response;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gongym
 * @version 创建时间: 2023-12-15 16:57
 */
@Setter
@Getter
public class TreeNode<T> {
    private String key;
    private String label;
    private boolean isLeaf;
    private List<T> children;

    public void setKey(Long key) {
        this.key = String.valueOf(key);
    }

    public void setKey(Integer key) {
        this.key = String.valueOf(key);
    }

    public void setKey(String key) {
        this.key = String.valueOf(key);
    }

    public void addChild(T node) {
        if (CollUtil.isEmpty(this.children)) {
            this.children = new ArrayList<>();
        }
        this.children.add(node);
    }
}
