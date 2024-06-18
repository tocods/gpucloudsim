package gpu;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import workflow.GpuJob;

import java.util.ArrayList;
import java.util.List;

public class ResGpuCloudlet extends ResCloudlet {

	private final List<GpuTask> gpuTasks;
	private final List<GpuTask> remainGpuTasks;

	private final GpuTask gpuTask;

	public ResGpuCloudlet(GpuCloudlet cloudlet) {
		super(cloudlet);
		this.gpuTask = cloudlet.getGpuTask();
		this.gpuTasks = new ArrayList<>(((GpuJob)cloudlet).getTasks());
		this.remainGpuTasks = new ArrayList<>(((GpuJob)cloudlet).getTasks());
		for(GpuTask t: ((GpuJob)cloudlet).getTasks()) {
			t.setResId(getCloudletId());
		}
		//this.gpuTasks = new ArrayList<>();
		//gpuTasks.add(gpuTask);
	}

	public ResGpuCloudlet(GpuCloudlet cloudlet, long startTime, int duration, int reservID) {
		super(cloudlet, startTime, duration, reservID);
		this.gpuTask = cloudlet.getGpuTask();
		this.gpuTasks = new ArrayList<>(((GpuJob)cloudlet).getTasks());
		this.remainGpuTasks = new ArrayList<>(((GpuJob)cloudlet).getTasks());
		for(GpuTask t: ((GpuJob)cloudlet).getTasks()) {
			t.setResId(getCloudletId());
		}
	}
	
	public GpuCloudlet finishCloudlet() {
		setCloudletStatus(GpuCloudlet.SUCCESS);
		finalizeCloudlet();
		return (GpuCloudlet) getCloudlet();
	}

	public GpuTask getGpuTask() {
		return gpuTask;
	}

	public List<GpuTask> getGpuTasks() {
		return gpuTasks;
	}

	public List<GpuTask> getRemainGpuTasks() {
		return remainGpuTasks;
	}

	public boolean finishGpuTasks(GpuTask t) {
		for(GpuTask task: remainGpuTasks) {
			if(task.getTaskId() == t.getTaskId()) {
				remainGpuTasks.remove(task);
				break;
			}
		}
		return remainGpuTasks.isEmpty();
	}

	public boolean hasGpuTask() {
//		if (getGpuTask() != null) {
//			return true;
//		}
//		return false;
		return !gpuTasks.isEmpty();
	}

}
