package workflow.jobAllocation;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import workflow.GpuJob;

import java.util.ArrayList;
import java.util.List;

public class BasicAllocation implements JobAllocationInterface{
    private List<GpuJob> jobs;

    private List<GpuJob> schedJobs;
    private List<?extends Host> hosts;
    @Override
    public void setJobs(List<GpuJob> list) {
        jobs = list;
        schedJobs = new ArrayList<>();
    }

    @Override
    public void setHosts(List list) {
        hosts = list;
    }

    @Override
    public List<GpuJob> getJobs() {
        return schedJobs;
    }

    @Override
    public void run() throws Exception {
        for(GpuJob job: jobs) {
            Log.printLine("job to schedule: " + job.getCloudletId());
            job.setVmId(hosts.get(0).getId());
            job.setHost(hosts.get(0));
            Log.printLine("finish schedule");
            schedJobs.add(job);
        }
    }
}
