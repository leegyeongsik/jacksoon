package io.jacksoon.init.registration;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
@Getter
public class InitNode {
    Class<?> clazz;
    Set<InitNode> parents;
    Set<InitNode> children;
    public InitNode(Class<?> clazz){
        this.clazz = clazz;
        this.parents = new HashSet<>();
        this.children = new HashSet<>();
    }
    public void putParent(InitNode initNode){
        this.parents.add(initNode);
    }
    public void putChildren(InitNode initNode){
        this.children.add(initNode);
    }
}