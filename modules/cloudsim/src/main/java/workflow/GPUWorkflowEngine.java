package workflow;

import gpu.GpuCloudlet;
import gpu.GpuDatacenterBroker;
import gpu.GpuTask;
import gpu.core.GpuCloudSimTags;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import workflow.jobAllocation.BasicAllocation;
import workflow.jobAllocation.JobAllocationInterface;
import workflow.taskCluster.BasicClustering;
import workflow.taskCluster.ClusteringInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GPUWorkflowEngine extends GpuDatacenterBroker {
    private ClusteringInterface clusteringInterface;

    private Map<Integer, Integer> host2Datacenter;

    private JobAllocationInterface jobAllocationInterface;
    /**
     * @param name 用于DEBUG
     * @see GpuDatacenterBroker
     */
    public GPUWorkflowEngine(String name) throws Exception {
        super(name);
        host2Datacenter = new HashMap<>();
        //initClusteringInterface();
        //initJobAllocationInterface();
    }

    /**
     * 设置聚类算法
     */
    public void initClusteringInterface() {
        clusteringInterface = new BasicClustering();
    }

    /**
     * 设置任务调度算法
     */
    public void initJobAllocationInterface(List<? extends Host> hosts) {
        jobAllocationInterface = new BasicAllocation();
        jobAllocationInterface.setHosts(hosts);
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            default:
                super.processOtherEvent(ev);
                break;
        }
    }

    /**
     * 这个函数基本就是GPUWorkflowEngine会第一个调用的函数，在这个函数里我们要开启任务的下发
     * @param ev a SimEvent object
     */
    @Override
    protected void processResourceCharacteristics(SimEvent ev) {
        Log.printLine("===============");
        DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);
        for(Host h: characteristics.getHostList()) {
            host2Datacenter.put(h.getId(), characteristics.getId());
        }
        send(characteristics.getId(), CloudSim.getMinTimeBetweenEvents(), GpuCloudSimTags.VGPU_DATACENTER_EVENT);
        // 完成了Datacenter和Engine之间的初始化同步
        if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
            setDatacenterRequestedIdsList(new ArrayList<Integer>());
            try {
                // 不需要原本的创建虚拟机这一步骤，直接分发任务
                doTaskDeliver();
            }catch (Exception e) {
                Log.printLine(e.getMessage());
            }
        }
    }

    /**
     * {@link api.service.Service} 将任务队列上传至此
     * @param list 任务队列
     */
    @Override
    public void submitCloudletList(List<? extends Cloudlet> list) {
        boolean ifStart = getCloudletList().isEmpty();
        // 将任务队列聚类后加入到 CloudletList 中
        getCloudletList().addAll(doTaskCluster(list));
        if(ifStart)
            return;
        try {
            doTaskDeliver();
        }catch (Exception e) {
            Log.printLine(e.getMessage());
        }
    }

    private List<GpuJob> doSchedule(List<GpuJob> jobs) throws Exception {
        jobAllocationInterface.setJobs(jobs);
        jobAllocationInterface.run();
        return jobAllocationInterface.getJobs();
    }

    /**
     * 下发任务给 {@link GPUWorkflowDatacenter}
     * 触发时机：
     * 1. 运行开始时
     * 2. 有执行完成的任务返回
     * 3. 有新上传的任务
     */
    protected void doTaskDeliver() throws Exception {
        List<GpuJob> tasks2Submit = new ArrayList<>();
        for(Cloudlet c: getCloudletList()) {
            GpuJob job = (GpuJob) c;
            Log.printLine("job id: " + job.getCloudletId());
            boolean ifAllParentFinish = true;
            for(Cloudlet parent: job.getParent()){
                if(!getCloudletReceivedList().contains(parent)){
                    ifAllParentFinish = false;
                    break;
                }
            }
            Log.printLine(ifAllParentFinish);
            if(ifAllParentFinish)
                tasks2Submit.add(job);
        }
        getCloudletList().removeAll(tasks2Submit);
        List<GpuJob> jobs = doSchedule(tasks2Submit);
        Log.printLine("jobs: " + jobs.size() + " " + jobs.get(0).getVmId());
        for(GpuJob job: jobs) {
            int datacenterId = host2Datacenter.get(job.getVmId());
            Log.printLine("datacenter: " + datacenterId);
            sendNow(datacenterId, CloudSimTags.CLOUDLET_SUBMIT, job);
            getCloudletSubmittedList().add(job);
            cloudletsSubmitted++;
        }
    }

    /**
     * 对任务进行聚类 {@link ClusteringInterface}
     */
    protected List<?extends Cloudlet> doTaskCluster(List<?extends Cloudlet> list) {
        clusteringInterface.setTaskList(list);
        clusteringInterface.run();
        return clusteringInterface.getJobList();
    }

    /**
     * 判断仿真是否结束
     * 判断标准：任务全部执行完成
     * @return True 如果任务全部执行完成
     */
    protected boolean ifFinish() {
        return getCloudletList().isEmpty() && cloudletsSubmitted == 0;
    }

    /**
     * 仿真结束
     */
    private void doComplete() {
        // 这个函数的作用是清除数据中心的所有虚拟机，但我们的仿真不存在虚拟机
        clearDatacenters();
        // 通知数据中心仿真结束
        finishExecution();
    }

    /**
     * 仿真未结束，执行下一步操作
     */
    private void doNext() {

    }

    /**
     * {@link GPUWorkflowDatacenter} 会将执行完成的任务传回，此函数将被调用
     * @param ev a SimEvent object
     */
    @Override
    protected void processCloudletReturn(SimEvent ev) {
        GpuCloudlet task = (GpuCloudlet) ev.getData();
        getCloudletReceivedList().add(task);
        cloudletsSubmitted--;
        if(ifFinish()) {
            // 任务全部执行完成，仿真结束
            doComplete();
        }else {
            // 还有任务尚未执行完成
            doNext();
        }
    }
}
