package org.example.work.parse.nodes;

import org.example.kit.entity.ByteArray;

/**
 * 文本节点类型
 */
public class TextNode extends Node {
    private final ByteArray text;

    public TextNode(ByteArray text, Element parent, int index){
        super(parent, index);
        this.text = text;
    }

    public ByteArray getText(){
        return text;
    }

    @Override
    public String toString(){
        if(text.length() > 20000)
            return text.subByteArray(0, 20000).toStr() + "...文本过长，剩下内容不再显示...";
        return text.toStr();
    }

    @Override
    public String toFpString(){
        String t = text.toStr();
        if(t.length() > 20)
            return (t.substring(0, 20) + "...").trim();
        return t.trim();
    }
}
