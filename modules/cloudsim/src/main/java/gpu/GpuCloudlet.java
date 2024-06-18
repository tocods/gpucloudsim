package gpu;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;

/**
 * To represent an application with both host and device execution
 * requirements, GpuCloudlet extends {@link Cloudlet} to include a
 * {@link GpuTask}.
 * 
 * @author Ahmad Siavashi
 * 
 */
public class GpuCloudlet extends Cloudlet {

	/**
	 * A tag associated with the GpuCloudlet. A tag can be used to describe the
	 * application.
	 */
	private String tag;

	/**
	 * The GPU part of the application.
	 */
	private GpuTask gpuTask;

	private List<Cloudlet> children;

	private List<Cloudlet> parent;

	/**
	 * Create a GpuCloudlet. {@link Cloudlet} represents the host portion of the
	 * application while {@link GpuTask} represents the device portion.
	 * 
	 * @param gpuCloudletId       gpuCloudlet id
	 * @param cloudletLength      length of the host portion
	 * @param pesNumber           number of threads
	 * @param cloudletFileSize    size of the application
	 * @param cloudletOutputSize  size of the application when executed
	 * @param utilizationModelCpu CPU utilization model of host portion
	 * @param utilizationModelRam RAM utilization model of host portion
	 * @param utilizationModelBw  BW utilization model of host portion
	 * @param gpuTask             the device portion of the application
	 */
	public GpuCloudlet(int gpuCloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, GpuTask gpuTask) {
		super(gpuCloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw);
		setGpuTask(gpuTask);
		initParent();
		initChildren();
	}

	/**
	 * Create a GpuCloudlet. {@link Cloudlet} represents the host portion of the
	 * application while {@link GpuTask} represents the device portion.
	 * 
	 * @param cloudletId       gpuCloudlet id
	 * @param cloudletLength      length of the host portion
	 * @param pesNumber           number of threads
	 * @param cloudletFileSize    size of the application
	 * @param cloudletOutputSize  size of the application when executed
	 * @param utilizationModelCpu CPU utilization model of host portion
	 * @param utilizationModelRam RAM utilization model of host portion
	 * @param utilizationModelBw  BW utilization model of host portion
	 * @param gpuTask             the device portion of the application
	 * @param record
	 * @param fileList
	 */
	public GpuCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, GpuTask gpuTask, boolean record, List<String> fileList) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw, record, fileList);
		setGpuTask(gpuTask);
		initParent();
		initChildren();
	}

	/**
	 * Create a GpuCloudlet. {@link Cloudlet} represents the host portion of the
	 * application while {@link GpuTask} represents the device portion.
	 * 
	 * @param cloudletId       gpuCloudlet id
	 * @param cloudletLength      length of the host portion
	 * @param pesNumber           number of threads
	 * @param cloudletFileSize    size of the application
	 * @param cloudletOutputSize  size of the application when executed
	 * @param utilizationModelCpu CPU utilization model of host portion
	 * @param utilizationModelRam RAM utilization model of host portion
	 * @param utilizationModelBw  BW utilization model of host portion
	 * @param gpuTask             the device portion of the application
	 * @param fileList
	 */
	public GpuCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, GpuTask gpuTask, List<String> fileList) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw, fileList);
		setGpuTask(gpuTask);
		initParent();
		initChildren();
	}

	/**
	 * Create a GpuCloudlet. {@link Cloudlet} represents the host portion of the
	 * application while {@link GpuTask} represents the device portion.
	 * 
	 * @param cloudletId       gpuCloudlet id
	 * @param cloudletLength      length of the host portion
	 * @param pesNumber           number of threads
	 * @param cloudletFileSize    size of the application
	 * @param cloudletOutputSize  size of the application when executed
	 * @param utilizationModelCpu CPU utilization model of host portion
	 * @param utilizationModelRam RAM utilization model of host portion
	 * @param utilizationModelBw  BW utilization model of host portion
	 * @param gpuTask             the device portion of the application
	 * @param record
	 */
	public GpuCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, GpuTask gpuTask, boolean record) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw, record);
		setGpuTask(gpuTask);
		initChildren();
		initParent();
	}

	/**
	 * Create a GpuCloudlet. {@link Cloudlet} represents the host portion of the
	 * application.
	 * 
	 * @param cloudletId       gpuCloudlet id
	 * @param cloudletLength      length of the host portion
	 * @param pesNumber           number of threads
	 * @param cloudletFileSize    size of the application
	 * @param cloudletOutputSize  size of the application when executed
	 * @param utilizationModelCpu CPU utilization model of host portion
	 * @param utilizationModelRam RAM utilization model of host portion
	 * @param utilizationModelBw  BW utilization model of host portion
	 * @param record
	 */
	public GpuCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, boolean record) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw, record);
		initChildren();
		initParent();
	}

	/**
	 * 初始化父任务列表
	 */
	private void initParent() {
		this.parent = new ArrayList<>();
	}

	/**
	 * 初始化子任务列表
	 */
	private void initChildren() {
		this.children = new ArrayList<>();
	}

	/**
	 * 增加父任务
	 * @param p 父任务
	 */
	public void addParent(Cloudlet p) {
		this.parent.add(p);
	}

	/**
	 * 增加子任务
	 * @param c 子任务
	 */
	public void addChild(GpuCloudlet c) {
		this.children.add(c);
	}


	/**
	 * 获得子任务队列
	 * @return
	 */
	public List<Cloudlet> getChildren() {
		return children;
	}

	/**
	 * 获得父任务队列
	 * @return
	 */
	public List<Cloudlet> getParent() {
		return parent;
	}

	/**
	 * @return the device portion
	 */
	public GpuTask getGpuTask() {
		return gpuTask;
	}

	/**
	 * @param gpuTask the device portion
	 */
	protected void setGpuTask(GpuTask gpuTask) {
		this.gpuTask = gpuTask;
		if (gpuTask != null && gpuTask.getCloudlet() == null) {
			gpuTask.setCloudlet(this);
		}
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

}
