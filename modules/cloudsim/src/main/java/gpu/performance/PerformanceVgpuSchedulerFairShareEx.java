package gpu.performance;

import java.util.List;

import gpu.VgpuSchedulerFairShare;
import gpu.performance.models.PerformanceModel;
import gpu.Pgpu;
import gpu.Vgpu;
import gpu.VgpuScheduler;
import gpu.VgpuSchedulerFairShareEx;
import gpu.selection.PgpuSelectionPolicy;

/**
 * * {@link PerformanceVgpuSchedulerFairShareEx} extends
 * {@link VgpuSchedulerFairShareEx VgpuSchedulerFairShareEx}
 * to add support for
 * {@link PerformanceModel
 * PerformanceModels}.
 * 
 * @author Ahmad Siavashi
 * 
 */
public class PerformanceVgpuSchedulerFairShareEx extends VgpuSchedulerFairShareEx implements PerformanceScheduler<Vgpu> {
	/** The performance model */
	private PerformanceModel<VgpuScheduler, Vgpu> performanceModel;

	/**
	 * @see VgpuSchedulerFairShare#VgpuSchedulerFairShare(int,
	 *      List, PgpuSelectionPolicy) VgpuSchedulerFairShare(int, List,
	 *      PgpuSelectionPolicy)
	 * 
	 * @param performanceModel
	 *            the performance model
	 */
	public PerformanceVgpuSchedulerFairShareEx(String videoCardType, List<Pgpu> pgpuList,
			PgpuSelectionPolicy pgpuSelectionPolicy, PerformanceModel<VgpuScheduler, Vgpu> performanceModel) {
		super(videoCardType, pgpuList, pgpuSelectionPolicy);
		this.performanceModel = performanceModel;
	}

	@Override
	public List<Double> getAvailableMips(Vgpu vgpu, List<Vgpu> vgpuList) {
		return this.performanceModel.getAvailableMips(this, vgpu, vgpuList);
	}
}
