package com.gamalocus.sgs.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;

import com.gamalocus.sgs.datastructures.TaskGroupAggregation.FinalTask;
import com.gamalocus.sgs.datastructures.TaskGroupAggregation.Operator;
import com.gamalocus.sgs.datastructures.TaskGroupAggregation.SubTask;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Operators;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;

public class TaskGroupAggregationTest implements AppListener, Serializable {
	private static final long serialVersionUID = 1L;

	public void initialize(Properties props) {
		ArrayList<SubTask<Integer>> subTasks = new ArrayList<SubTask<Integer>>();
		for(int i = 0; i < 2000; i++)
		{
			subTasks.add(new SimpleIntegerSubTask());
		}
		TaskGroupAggregation.createGroupAggregation(new SimpleIntegerOperator(), 
				subTasks, 
				new SimpleIntegerFinalTask());
	}

	public ClientSessionListener loggedIn(ClientSession session) {
		return null;
	}
}

class SimpleIntegerFinalTask implements FinalTask<Integer>
{
	public void run(Integer result) {
		System.out.println("Final result is: "+result);
	}
}

class SimpleIntegerOperator implements Operator<Integer>
{
	public Integer sum(Integer v1, Integer v2) {
		Integer res = new Integer(v1+v2);
		//System.out.println(v1+" + "+v2+" = "+res);
		return res;
	}
}

class SimpleIntegerSubTask implements SubTask<Integer>
{
	public Integer getValue() {
		return 1;
	}
}
