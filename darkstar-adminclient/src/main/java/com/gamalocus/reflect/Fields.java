package com.gamalocus.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

public class Fields {

	public static Collection<Field> getAllFields(Class<? extends Object> class1) {
		Stack<Class<?>> classHierachy = getClassHierachy(class1);
		
		ArrayList<Field> fields = new ArrayList<Field>();
		for(Class<? extends Object>  cl : classHierachy)
		{
			for(Field f : cl.getDeclaredFields())
			{
				fields.add(f);
			}
		}
		return fields;
	}

	private static Stack<Class<?>> getClassHierachy(
			Class<? extends Object> class1) {
		Stack<Class<?>> classHierachy = new Stack<Class<?>>();
		
		Class<? extends Object> current = class1;
		while(current != null && !current.equals(Object.class))
		{
			classHierachy.push(current);
			current = current.getSuperclass();
		}
		return classHierachy;
	}

}
