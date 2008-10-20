package com.gamalocus.sgs.datastructures;

import java.io.Serializable;
import java.util.Collection;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;

/**
 * This class is a utility for running a series of tasks where all the "results" are being 
 * operated on in pairs.
 * 
 * @author emanuel
 *
 */
public class TaskGroupAggregation {
	/**
	 * This is the operator that will work on the results of the tasks.
	 *  
	 * @author emanuel
	 *
	 * @param <ValueType>
	 */
	public interface Operator<ValueType extends Serializable> extends Serializable {
		ValueType sum(ValueType v1, ValueType v2);
	}
	
	/**
	 * This is the actual tasks.
	 * 
	 * @author emanuel
	 *
	 * @param <ValueType>
	 */
	public interface SubTask<ValueType extends Serializable> extends Serializable {
		ValueType getValue();
	}
	
	/**
	 * This "task" will be run as the last thing.
	 * 
	 * @author emanuel
	 *
	 * @param <ValueType>
	 */
	public interface FinalTask<ValueType extends Serializable> extends Serializable, ManagedObject {
		void run(ValueType result);
	}
	
	/**
	 * Set up a group of subtasks to be processed in parallel.
	 * 
	 * @param <ValueType>
	 * @param operator
	 * @param subtasks
	 */
	public static <ValueType extends Serializable> void createGroupAggregation(
			Operator<ValueType> operator, 
			Collection<SubTask<ValueType>> subTasks,
			FinalTask<ValueType> finalTask) 
	{
		// Divide and conquour
		SubTask<ValueType>[] arr = subTasks.toArray(new SubTask[0]);
		createGroupAggregationRec(operator, arr, 0, arr.length-1, finalTask);
	}
	
	private static <ValueType extends Serializable> void createGroupAggregationRec(
			final Operator<ValueType> operator, 
			SubTask<ValueType>[] arr,
			int fromIndex, 
			int toIndex,
			final FinalTask<ValueType> finalTask)
	{
		int diff = toIndex - fromIndex;
		//System.out.println(toIndex+" - "+fromIndex+" = "+diff);
		if(diff <= 1)
		{
			SubTask<ValueType> sub1 = arr[fromIndex];
			SubTask<ValueType> sub2 = fromIndex != toIndex ? arr[toIndex] : null;
			new BinaryOperatorTask<ValueType>(operator, sub1, sub2, finalTask);
		}
		else
		{
			// We must subdivide further
			int subdiv = diff/2;
			
			// The parent "task"
			FinalTask<ValueType> operatorTask = new SimpleOperatorTask(operator, finalTask);
			
			// The two child tasks
			createGroupAggregationRec(operator, arr, fromIndex, toIndex-subdiv, operatorTask);
			createGroupAggregationRec(operator, arr, toIndex-subdiv+1, toIndex, operatorTask);
		}
	}
}

class SimpleOperatorTask<ValueType extends Serializable> implements TaskGroupAggregation.FinalTask<ValueType>, Task
{
	boolean hasFirstResult = false;
	boolean hasFinalResult = false;
	ValueType sub1_result, sub2_result, final_result;
	private ManagedReference<TaskGroupAggregation.FinalTask<ValueType>> finalTask;
	private TaskGroupAggregation.Operator<ValueType> operator;
	
	public SimpleOperatorTask(TaskGroupAggregation.Operator<ValueType> operator,
			TaskGroupAggregation.FinalTask<ValueType> finalTask) {
		this.operator = operator;
		this.finalTask = AppContext.getDataManager().createReference(finalTask);
	}

	public void run(ValueType result)
	{
		AppContext.getDataManager().markForUpdate(this);
		
		if(!hasFirstResult)
		{
			sub1_result = result;
			hasFirstResult = true;
		}
		else
		{
			sub2_result = result;
			AppContext.getTaskManager().scheduleTask(this);
		}
	}

	public void run() throws Exception {
		AppContext.getDataManager().markForUpdate(this);
		if(hasFinalResult)
		{
			finalTask.get().run(final_result);
		}
		else
		{
			final_result = operator.sum(sub1_result, sub2_result);
			hasFinalResult = true;
			AppContext.getTaskManager().scheduleTask(this);
		}
	}
};

/**
 * This task will run one or two child tasks and operate on their results. 
 * 
 * @author emanuel
 *
 * @param <ValueType>
 */
class BinaryOperatorTask<ValueType extends Serializable> implements Serializable, Task, ManagedObject
{
	private static final long serialVersionUID = -1887691508525667853L;
	
	private final TaskGroupAggregation.Operator<ValueType> operator;
	private final TaskGroupAggregation.SubTask<ValueType> sub1;
	private final TaskGroupAggregation.SubTask<ValueType> sub2;
	private ValueType sub1_result;
	private ValueType sub2_result;
	private final ManagedReference<TaskGroupAggregation.FinalTask<ValueType>> finalTask;
	private ValueType final_result;
	enum NextTask { SUB1, SUB2, OPERATOR, RETURN }
	NextTask nextTask = NextTask.SUB1;

	
	public BinaryOperatorTask(TaskGroupAggregation.Operator<ValueType> operator, 
			TaskGroupAggregation.SubTask<ValueType> sub1, 
			TaskGroupAggregation.SubTask<ValueType> sub2,
			TaskGroupAggregation.FinalTask<ValueType> finalTask) {
		this.operator = operator;
		this.sub1 = sub1;
		this.sub2 = sub2;
		this.finalTask = AppContext.getDataManager().createReference(finalTask);
		AppContext.getTaskManager().scheduleTask(this);
	}

	public void run() throws Exception {
		DataManager dataManager = AppContext.getDataManager();
		TaskManager taskManager = AppContext.getTaskManager();
		dataManager.markForUpdate(this);
		
		//System.out.println("We shall: "+nextTask);
		switch(nextTask)
		{
		case SUB1:
			sub1_result = sub1.getValue();
			if(sub2 != null)
			{
				nextTask = NextTask.SUB2;
			}
			else
			{
				final_result = sub1_result;
				nextTask = NextTask.RETURN;
			}
			break;
		case SUB2:
			sub2_result = sub2.getValue();
			nextTask = NextTask.OPERATOR;
			break;
		case OPERATOR:
			final_result = operator.sum(sub1_result, sub2_result);
			nextTask = NextTask.RETURN;
			break;
		case RETURN:
			finalTask.get().run(final_result);
			nextTask = null;
			dataManager.removeObject(this);
			break;
		default:
			throw new RuntimeException("Unknown NextTask:"+nextTask);
		}
		
		if(nextTask != null)
		{
			taskManager.scheduleTask(this);
		}
	}
}
