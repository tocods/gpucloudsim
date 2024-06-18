package workflow;

import gpu.GpuCloudlet;
import gpu.GpuTask;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;

public class GpuJob extends GpuCloudlet {
    private List<GpuTask> tasks;

    private Host host;


    public GpuJob(int gpuCloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw, GpuTask gpuTask) {
        super(gpuCloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw, gpuTask);
        host = null;
        tasks = new ArrayList<>();
    }

    public GpuJob(int jobId, long jobLength, GpuTask t) {
        super(jobId, jobLength, 1, 0, 0, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(), t);
        host = null;
        tasks = new ArrayList<>();
    }

    @Override
    protected void setGpuTask(GpuTask gpuTask) {
    }

    public void addGpuTask(GpuTask gpuTask) {
        tasks.add(gpuTask);
    }

    public List<GpuTask> getTasks() {
        return tasks;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public GpuTask getGpuTask() {
        return super.getGpuTask();
    }
}
