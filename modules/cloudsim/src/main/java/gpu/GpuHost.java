package gpu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gpu.allocation.VideoCardAllocationPolicy;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * 
 * {@link GpuHost} extends {@link Host} and supports {@link VideoCard}s through
 * a {@link VideoCardAllocationPolicy}.
 * 
 * @author Ahmad Siavashi
 * 
 */
public class GpuHost extends Host {

	/**
	 * type of the host
	 */
	public String type;

	/** video card allocation policy */
	private VideoCardAllocationPolicy videoCardAllocationPolicy;

	/**
	 * 
	 * See {@link Host#Host}
	 * 
	 * @param type                      type of the host which is specified in
	 *                                  {@link GpuHostTags}.
	 * @param videoCardAllocationPolicy the policy in which the host allocates video
	 *                                  cards to vms
	 */
	public GpuHost(int id, String type, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler, VideoCardAllocationPolicy videoCardAllocationPolicy) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setType(type);
		setVideoCardAllocationPolicy(videoCardAllocationPolicy);
	}

	/**
	 * 
	 * See {@link Host#Host}
	 * 
	 * @param type type of the host which is specified in {@link GpuHostTags}.
	 */
	public GpuHost(int id, String type, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setType(type);
		setVideoCardAllocationPolicy(null);
	}

	/**
	 * 遍历每张显卡上的每个 pGPU，调用 pGPU 上的任务调度器更新任务
	 * @param currentTime
	 * @return
	 */
	public double updatePgpuProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;
		if (isGpuEquipped()) {
			// 调用 pGPU 上的任务调度器更新任务
			for (VideoCard videoCard : getVideoCardAllocationPolicy().getVideoCards()) {
				// 遍历每张显卡上的每个 pGPU
				for(Pgpu pgpu : videoCard.getPgpuList()) {
					double time = pgpu.updateGpuTaskProcessing(currentTime);
					if (time > 0.0 && time < smallerTime) {
						smallerTime = time;
					}
				}
			}
		}
		return smallerTime;
	}

	public void notifyGpuTaskCompletion(GpuTask t) {
		Log.printLine("nn");
		GpuCloudletScheduler cloudletScheduler = (GpuCloudletScheduler) getCloudletScheduler();
		cloudletScheduler.notifyGpuTaskCompletion(t);
	}

	public List<ResGpuTask> checkGpuTaskCompletion() {
		Log.printLine("checkGpuTaskCompletion");
		List<ResGpuTask> ret = new ArrayList<>();
		for (VideoCard videoCard : getVideoCardAllocationPolicy().getVideoCards()) {
			// 遍历每张显卡上的每个 pGPU
			for(Pgpu pgpu : videoCard.getPgpuList()) {
				while(pgpu.getGpuTaskScheduler().hasFinishedTasks()) {
					ret.add(pgpu.getGpuTaskScheduler().getNextFinishedTask());
				}
			}
		}
		return ret;
	}

	public boolean hasGpuTask(){
		return ((GpuCloudletScheduler)getCloudletScheduler()).hasGpuTask();
	}

	public GpuTask getNextGpuTask() {
		return ((GpuCloudletScheduler)getCloudletScheduler()).getNextGpuTask();
	}

	public double updateVgpusProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;
		if (isGpuEquipped()) {
			// Update resident vGPUs
			for (Vgpu vgpu : getVideoCardAllocationPolicy().getVgpuVideoCardMap().keySet()) {
				double time = vgpu.updateGpuTaskProcessing(currentTime, getVideoCardAllocationPolicy()
						.getVgpuVideoCardMap().get(vgpu).getVgpuScheduler().getAllocatedMipsForVgpu(vgpu));
				if (time > 0.0 && time < smallerTime) {
					smallerTime = time;
				}
			}
		}
		return smallerTime;
	}

	@Override
	public boolean isSuitableForVm(Vm vm) {
		boolean result = vmCreate(vm);
		if (result) {
			vmDestroy(vm);
		}
		return result;
	}

	/**
	 * @return the videoCardAllocationPolicy
	 */
	public VideoCardAllocationPolicy getVideoCardAllocationPolicy() {
		return videoCardAllocationPolicy;
	}

	/**
	 * @param videoCardAllocationPolicy the videoCardAllocationPolicy to set
	 */
	public void setVideoCardAllocationPolicy(VideoCardAllocationPolicy videoCardAllocationPolicy) {
		this.videoCardAllocationPolicy = videoCardAllocationPolicy;
	}

	/**
	 * Checks the existence of a given video card id in the host
	 * 
	 * @param videoCardId id of the video card
	 * @return
	 */
	public boolean hasVideoCard(int videoCardId) {
		if (!isGpuEquipped()) {
			return false;
		}
		for (VideoCard videoCard : getVideoCardAllocationPolicy().getVideoCards()) {
			if (videoCard.getId() == videoCardId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks the existence of a given pgpu id in the host
	 * 
	 * @param pgpuId id of the video card
	 * @return
	 */
	public boolean hasPgpu(int pgpuId) {
		if (!isGpuEquipped()) {
			return false;
		}
		for (VideoCard videoCard : getVideoCardAllocationPolicy().getVideoCards()) {
			for (Pgpu pgpu : videoCard.getVgpuScheduler().getPgpuList()) {
				if (pgpu.getId() == pgpuId) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isGpuEquipped() {
		return getVideoCardAllocationPolicy() != null && !getVideoCardAllocationPolicy().getVideoCards().isEmpty();
	}

	public void vgpuDestroy(Vgpu vgpu) {
		if (vgpu != null) {
			getVideoCardAllocationPolicy().deallocate(vgpu);
		}
	}

	public boolean vgpuCreate(Vgpu vgpu) {
		return getVideoCardAllocationPolicy().allocate(vgpu, vgpu.getPCIeBw());
	}

	public boolean vgpuCreate(Vgpu vgpu, Pgpu pgpu) {
		return getVideoCardAllocationPolicy().allocate(pgpu, vgpu, vgpu.getPCIeBw());
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	protected void setType(String type) {
		this.type = type;
	}

	public Set<Vgpu> getVgpuSet() {
		if (!isGpuEquipped()) {
			return null;
		}
		return getVideoCardAllocationPolicy().getVgpuVideoCardMap().keySet();
	}

	public boolean isIdle() {

		if (!getVmList().isEmpty()) {
			return false;
		} else if (getVgpuSet() != null && !getVgpuSet().isEmpty()) {
			return false;
		}

		return true;
	}
}
