package com.gamalocus.sgs.guiclient.windows;

import java.lang.reflect.Field;

import javax.swing.tree.DefaultMutableTreeNode;

public class FieldTreeItem<T> extends DefaultMutableTreeNode {

	private T object;
	private Field field;

	public FieldTreeItem(T object, Field f) {
		this.object = object;
		this.field = f;
	}
	
	public String toString()
	{
		try {
			return field.getName()+":"+field.get(object)+"["+field.getType()+"]";
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return "Failed:"+field.getName();
	}

}
