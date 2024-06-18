package gpu.power;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import gpu.GpuDatacenterBroker;
import gpu.core.GpuCloudSimTags;

/**
 * {@link PowerGpuDatacenterBroker} extends {@link GpuDatacenterBroker} to
 * handle extra power-events that occur in the simulation.
 * 
 * @author Ahmad Siavashi
 * 
 */
public class PowerGpuDatacenterBroker extends GpuDatacenterBroker {

	/**
	 * @see GpuDatacenterBroker#GpuDatacenterBroker(String)
	 */
	public PowerGpuDatacenterBroker(String name) throws Exception {
		super(name);
	}

	@Override
	protected void finishExecution() {
		for (Integer datacenterId : getDatacenterIdsList()) {
			CloudSim.cancelAll(datacenterId.intValue(),
					new PredicateType(GpuCloudSimTags.GPU_VM_DATACENTER_POWER_EVENT));
		}
		super.finishExecution();
	}

}
