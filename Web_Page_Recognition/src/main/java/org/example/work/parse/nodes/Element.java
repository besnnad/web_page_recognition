package org.example.work.parse.nodes;

import org.example.kit.entity.ByteArray;
import org.example.work.parse.Attribute;
import org.example.work.parse.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 网页元素类-> 继承节点类，实现当前节点的指纹提取。
 */
public class Element extends Node {
    private static final List<Node> EMPTY_CHILDREN = Collections.emptyList();
    private static final Attribute[] EMPTY_ATTRIBUTE = new Attribute[0];

    private final Tag tag;

    private List<Node> childNodes;
    private Attribute[] attrs;

    public Element(Tag tag, Element parent, int index){
        super(parent, index);
        this.tag = tag;
        childNodes = EMPTY_CHILDREN;
        attrs = EMPTY_ATTRIBUTE;
    }

    public Element(Tag tag){
        this.tag = tag;
        childNodes = EMPTY_CHILDREN;
        attrs = EMPTY_ATTRIBUTE;
    }

    public void appendChild(Node child){
        if(childNodes == EMPTY_CHILDREN)
            childNodes = new ArrayList<>(8);
//        child.index = childNodes.size();
        childNodes.add(child);
    }

    public void appendChild(int index, Node child){
        if(childNodes == EMPTY_CHILDREN)
            childNodes = new ArrayList<>(8);
        childNodes.add(index, child);
        for(int i = index + 1; i < childNodes.size(); i++){
            childNodes.get(i).index++;
        }
        child.index = index;
    }

    public Node child(int num){
        if(num >= childNodes.size())
            return null;
        return childNodes.get(num);
    }

    public Element childElement(int num){
        for(Node node : childNodes){
            if(node instanceof Element){
                if(num == 0)
                    return (Element)node;
                num--;
            }
        }
        return null;
    }

    public Element childElement(String tag){
        for(Node node : childNodes){
            if(!(node instanceof Element))
                continue;
            Element e = (Element)node;
            if(e.getTag().is(tag))
                return e;
        }
        return null;
    }

    public int indexOfElement(String tag){
        for(int i = 0; i < childNodes.size(); i++){
            Node node = childNodes.get(i);
            if(!(node instanceof Element))
                continue;
            Element e = (Element)node;
            if(e.getTag().is(tag))
                return i;
        }
        return -1;
    }

    public boolean hasChild(){return childNodes.size() > 0;}

    public List<Node> children(){
        return childNodes;
    }

    public int childrenSize(){
        return childNodes.size();
    }

    public Node removeChild(Node child){
        int index = childNodes.indexOf(child);
        if(index < 0)
            return null;
        Node oldNode = childNodes.remove(index);
        for(int i = index; i < childNodes.size(); i++){
            childNodes.get(i).index--;
        }
        return oldNode;
    }

    public Node removeChild(int index){
        if(index < 0 || index >= childNodes.size())
            return null;
        Node oldNode = childNodes.remove(index);
        for(int i = index; i < childNodes.size(); i++){
            childNodes.get(i).index--;
        }
        return oldNode;
    }

    public Attribute[] attrs(){
        return attrs;
    }

    /**
     * 根据属性key值获取属性value值，如果没有返回空
     * @param attr
     * @return
     */
    public ByteArray attr(String attr){
        for(Attribute a : attrs){
            if(a.isKey(attr))
                return a.getValue();
        }
        return null;
    }

    public void setAttrs(Attribute[] attrs){
        if(attrs == null || attrs.length == 0)
            return;
        this.attrs = attrs;
    }

    public Tag getTag(){
        return tag;
    }

    public String getTagName(){
        return tag.getName();
    }

    public boolean hasChildNodes(){
        return childNodes != EMPTY_CHILDREN;
    }

    public Node firstChild(){
        return childNodes.size() == 0 ? null : childNodes.get(0);
    }

    public Node lastChild(){
        return childNodes.size() == 0 ? null : childNodes.get(childNodes.size() - 1);
    }

    public Node pre(){
        if(parent == null || getIndex() == 0)
            return null;
        return parent.child(getIndex() - 1);
    }

    public Node next(){
        if(parent == null || parent.children().size() - 1 == getIndex())
            return null;
        return parent.child(getIndex() + 1);
    }

    /**
     * 根据标签查找父系元素
     *
     * @param tag 标签（每个Parser使用的标签都是独立的）
     * @return 标签 tag 对应的父系元素
     */
    public Element getParentByTag(Tag tag){
        Element p = parent;
        while(p != null && p.getTag() != tag){
            p = p.parent;
        }
        return p;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Node t = pre();
        if(!(t instanceof TextNode)){
            for(int i = 0; i < depth; i++){
                sb.append("\t");
            }
        }
        sb.append("<").append(tag.getName());
        if(attrs.length > 0){
            sb.append(" ");
            sb.append(Arrays.toString(attrs).replace(",", "").replaceAll("[\\[\\]]", ""));
        }
        sb.append(">");
        if(!tag.isEmpty()){
            t = child(0);
            if(t != null && !(t instanceof TextNode))
                sb.append("\r\n");
            for(Node childNode : childNodes){
                sb.append(childNode.toString());
            }
            if(t != null && !(lastChild() instanceof TextNode))
                for(int i = 0; i < depth; i++){
                    sb.append("\t");
                }
            sb.append("</").append(tag.getName()).append(">");
        }
        t = next();
        if(!(t instanceof TextNode))
            sb.append("\r\n");
        return sb.toString();
    }

    @Override
    public String toFpString(){
        StringBuilder stringBulider = new StringBuilder();
        Node t = pre();
        if(!(t instanceof TextNode)){
            for(int i = 0; i < depth; i++){
                stringBulider.append("\t");
            }
        }
        stringBulider.append("<").append(tag.getName());
        if(attr("class") != null){
            stringBulider.append(" ");
            stringBulider.append("class=\"").append(attr("class").toStr()).append("\"");
        }else if(attrs.length > 0){
            stringBulider.append(" ");
            stringBulider.append(Arrays.toString(attrs).replace(",", "").replaceAll("[\\[\\]]", ""));
        }
        stringBulider.append(">");
        if(!tag.isEmpty()){
            t = child(0);
            if(t != null && !(t instanceof TextNode))
                stringBulider.append("\r\n");
            for(Node childNode : childNodes){
                stringBulider.append(childNode.toFpString());
            }
            if(t != null && !(lastChild() instanceof TextNode))
                for(int i = 0; i < depth; i++){
                    stringBulider.append("\t");
                }
            stringBulider.append("</").append(tag.getName()).append(">");
        }
        t = next();
        if(!(t instanceof TextNode))
            stringBulider.append("\r\n");
        return stringBulider.toString();
    }
}
