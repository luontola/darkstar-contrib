package com.sun.sgs.impl.kernel;

import java.math.BigInteger;

import com.gamalocus.sgs.adminclient.connection.AdminClientConnection;
import com.gamalocus.sgs.adminclient.messages.GetManagedReferenceFromNameBinding;
import com.gamalocus.sgs.adminclient.messages.GetNameBindings;
import com.gamalocus.sgs.adminclient.messages.ManagedReferenceCapsule;
import com.gamalocus.sgs.adminclient.serialization.ManagedReferenceImpl;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.service.DataService;

public class AdminClientKernelContext extends KernelContext
{
	protected AdminClientKernelContext(ComponentRegistry serviceComponents,
			ComponentRegistry managerComponents) {
		super("AdminClient", serviceComponents, managerComponents);
	}

	public static class AdminClientKernelIdentity implements Identity
	{
		public String getName() {
			return "AdminClientKernelIdentity";
		}

		public void notifyLoggedIn() {
			// Not going to happen
			
		}

		public void notifyLoggedOut() {
			// Not going to happen
			
		}
		
	}

	public static void initKernelContext(AdminClientConnection adminClientConnection) {
		ComponentRegistryImpl serviceComponents = new ComponentRegistryImpl();
		serviceComponents.addComponent(new AdminClientDataService());
		ComponentRegistryImpl managerComponents = new ComponentRegistryImpl();
		managerComponents.addComponent(new AdminClientDataManager(adminClientConnection));
		ContextResolver.setTaskState(new AdminClientKernelContext(serviceComponents, managerComponents), new AdminClientKernelIdentity());
	}
	
	private static class AdminClientDataManager implements DataManager
	{
		private AdminClientConnection adminClientConnection;

		public AdminClientDataManager(
				AdminClientConnection adminClientConnection) {
			this.adminClientConnection = adminClientConnection;
		}

		public <T> ManagedReference<T> createReference(final T object) {
			return new ManagedReference<T>()
			{
				public T get() {
					return object;
				}

				public T getForUpdate() {
					return object;
				}

				public BigInteger getId() {
					return null;
				}
			};
		}

		public ManagedObject getBinding(String name) {
			try
			{
				ManagedReferenceCapsule<ManagedObject> ref = adminClientConnection.sendSync(new GetManagedReferenceFromNameBinding<ManagedObject>(name));
				if(ref != null && ref.reference != null)
				{
					return ref.reference.get();
				}
			}
			catch(Throwable t)
			{
				throw new NameNotBoundException("The name "+name+" was not bound", t);
			}
			return null;
		}

		public void markForUpdate(Object object) {
			throw new RuntimeException("Not supported");
		}

		public String nextBoundName(String name) {
			throw new RuntimeException("Not supported");
		}

		public void removeBinding(String name) {
			throw new RuntimeException("Not supported");
		}

		public void removeObject(Object object) {
			throw new RuntimeException("Not supported");
		}

		public void setBinding(String name, Object object) {
			throw new RuntimeException("Not supported");
		}
		
	}

	private static class AdminClientDataService implements DataService
	{

		public ManagedReference<?> createReferenceForId(BigInteger id) {
			// TODO Auto-generated method stub
			return null;
		}

		public ManagedObject getServiceBinding(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		public BigInteger nextObjectId(BigInteger objectId) {
			// TODO Auto-generated method stub
			return null;
		}

		public String nextServiceBoundName(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		public void removeServiceBinding(String name) {
			// TODO Auto-generated method stub
			
		}

		public void setServiceBinding(String name, Object object) {
			// TODO Auto-generated method stub
			
		}

		public <T> ManagedReference<T> createReference(T object) {
			// TODO Auto-generated method stub
			return null;
		}

		public ManagedObject getBinding(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		public void markForUpdate(Object object) {
			// TODO Auto-generated method stub
			
		}

		public String nextBoundName(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		public void removeBinding(String name) {
			// TODO Auto-generated method stub
			
		}

		public void removeObject(Object object) {
			// TODO Auto-generated method stub
			
		}

		public void setBinding(String name, Object object) {
			// TODO Auto-generated method stub
			
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		public void ready() throws Exception {
			// TODO Auto-generated method stub
			
		}

		public boolean shutdown() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
