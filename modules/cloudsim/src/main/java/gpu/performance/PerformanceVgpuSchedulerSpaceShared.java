package gpu.performance;

import java.util.List;

import gpu.performance.models.PerformanceModel;
import gpu.Pgpu;
import gpu.Vgpu;
import gpu.VgpuScheduler;
import gpu.VgpuSchedulerSpaceShared;
import gpu.selection.PgpuSelectionPolicy;

/**
 * {@link PerformanceVgpuSchedulerSpaceShared} extends
 * {@link VgpuSchedulerSpaceShared
 * VgpuSchedulerSpaceShared} to add support for
 * {@link PerformanceModel
 * PerformanceModels}.
 * 
 * @author Ahmad Siavashi
 * 
 */
public class PerformanceVgpuSchedulerSpaceShared extends VgpuSchedulerSpaceShared
		implements PerformanceScheduler<Vgpu> {

	/** The performance model */
	private PerformanceModel<VgpuScheduler, Vgpu> performanceModel;

	/**
	 * @see VgpuSchedulerSpaceShared#VgpuSchedulerSpaceShared(int,
	 *      List, PgpuSelectionPolicy) VgpuSchedulerSpaceShared(int, List,
	 *      PgpuSelectionPolicy)
	 * @param performanceModel
	 *            the performance model
	 */
	public PerformanceVgpuSchedulerSpaceShared(String videoCardType, List<Pgpu> pgpuList,
			PgpuSelectionPolicy pgpuSelectionPolicy, PerformanceModel<VgpuScheduler, Vgpu> performanceModel) {
		super(videoCardType, pgpuList, pgpuSelectionPolicy);
		this.performanceModel = performanceModel;
	}

	@Override
	public List<Double> getAvailableMips(Vgpu vgpu, List<Vgpu> vgpuList) {
		return this.performanceModel.getAvailableMips(this, vgpu, vgpuList);
	}

}
