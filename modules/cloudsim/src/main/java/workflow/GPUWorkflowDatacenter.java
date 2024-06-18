package workflow;

import gpu.*;
import gpu.core.GpuCloudSimTags;
import gpu.power.PowerGpuDatacenter;
import gpu.power.PowerGpuHost;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;

public class GPUWorkflowDatacenter extends PowerGpuDatacenter {

    public GPUWorkflowDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        super.processOtherEvent(ev);
        switch (ev.getTag()) {
            case WorkflowSimTags.WOKFLOW_DATACENTER_EVENT:
                schedule(getId(), getSchedulingInterval(), GpuCloudSimTags.GPU_VM_DATACENTER_POWER_EVENT);
                break;
            case CloudSimTags.END_OF_SIMULATION:
                shutdownEntity();
                break;
        }
    }

    //*********************************************************************************
    //*                         以下内容关于GPU上任务执行                                 *
    //*********************************************************************************

    private Pgpu getPgpuOfTask(GpuTask task) {
        GpuCloudlet cl = task.getCloudlet();
        int hostId = cl.getVmId();
        GpuHost host = null;
        for(Host h: getHostList()) {
            if(h.getId() == hostId){
                host = (GpuHost) h;
                break;
            }
        }
        assert host != null;
        return host.getVideoCardAllocationPolicy().getPgpu(0);
    }

    @Override
    protected void processGpuTaskSubmit(SimEvent ev){
        Log.printLine("processGpuTaskSubmit");
        updateGpuTaskProcessing();

        try {
            GpuTask gt = (GpuTask) ev.getData();
            Log.printLine("gpu task id: " + gt.getTaskId());
            gt.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(),
                    getCharacteristics().getCostPerBw());

            Pgpu gpu = getPgpuOfTask(gt);

            if(gpu == null) {
                return;
            }
            GpuTaskScheduler scheduler = gpu.getGpuTaskScheduler();

            double estimatedFinishTime = scheduler.taskSubmit(gt);

            // if this task is in the exec queue
            if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
                send(getId(), estimatedFinishTime, GpuCloudSimTags.VGPU_DATACENTER_EVENT);
            }

        } catch (ClassCastException c) {
            Log.printLine(getName() + ".processGpuTaskSubmit(): " + "ClassCastException error.");
            c.printStackTrace();
        } catch (Exception e) {
            Log.printLine(getName() + ".processGpuTaskSubmit(): " + "Exception error.");
            e.printStackTrace();
            System.exit(-1);
        }

        checkGpuTaskCompletion();
    }

    @Override
    protected void updateGpuTaskProcessing() {
        if (CloudSim.clock() < 0.111
                || CloudSim.clock() > geGpuTasktLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
            double smallerTime = Double.MAX_VALUE;
            // 遍历物理节点
            for (Host h: getHostList()) {
                GpuHost host = (GpuHost) h;
                // 物理节点更新任务状态
                double time = host.updatePgpuProcessing(CloudSim.clock());
                // 我们关心的是任务何时完成
                if (time < smallerTime) {
                    smallerTime = time;
                }
            }
            // 保证时间间隔小于最小值
            if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01) {
                smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01;
            }
            if (smallerTime != Double.MAX_VALUE) {
                schedule(getId(), (smallerTime - CloudSim.clock()), GpuCloudSimTags.VGPU_DATACENTER_EVENT);
            }
            setGpuTaskLastProcessTime(CloudSim.clock());
        }
    }

    @Override
    protected void checkGpuTaskCompletion() {
        for (Host h: getHostList()) {
            GpuHost host = (GpuHost) h;
            List<ResGpuTask> finishTask = host.checkGpuTaskCompletion();
            for(ResGpuTask resGpuTask: finishTask) {
                try {
                    sendNow(getId(), GpuCloudSimTags.GPU_MEMORY_TRANSFER, resGpuTask.getGpuTask());
                } catch (Exception e) {
                    Log.printLine(e.getMessage());
                    CloudSim.abruptallyTerminate();
                }
            }
        }
    }

    @Override
    protected void notifyGpuTaskCompletion(GpuTask gt) {
        Host host = null;
        for(Host h: getHostList()) {
            if(h.getId() == gt.getCloudlet().getVmId()) {
                host = h;
                break;
            }
        }
        assert host != null;
        ((GpuHost)host).notifyGpuTaskCompletion(gt);
    }

    @Override
    protected void processGpuCloudletReturn(SimEvent ev) {
        Log.printLine("processGpuCloudletReturn");
        GpuCloudlet cloudlet = (GpuCloudlet) ev.getData();
        sendNow(cloudlet.getUserId(), CloudSimTags.CLOUDLET_RETURN, cloudlet);
        Log.printLine("try to notify");
        notifyGpuTaskCompletion(cloudlet.getGpuTask());
    }

    //*********************************************************************************
    //*                         以下内容关于CPU上任务执行                                 *
    //*********************************************************************************

    @Override
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {
        updateCloudletProcessing();
        try {
            // gets the Cloudlet object
            Cloudlet cl = (Cloudlet) ev.getData();

            // checks whether this Cloudlet has finished or not
            if (cl.isFinished()) {
                String name = CloudSim.getEntityName(cl.getUserId());
                Log.printConcatLine(getName(), ": Warning - Cloudlet #", cl.getCloudletId(), " owned by ", name,
                        " is already completed/finished.");
                Log.printLine("Therefore, it is not being executed again");
                Log.printLine();

                // NOTE: If a Cloudlet has finished, then it won't be processed.
                // So, if ack is required, this method sends back a result.
                // If ack is not required, this method don't send back a result.
                // Hence, this might cause CloudSim to be hanged since waiting
                // for this Cloudlet back.
                if (ack) {
                    int[] data = new int[3];
                    data[0] = getId();
                    data[1] = cl.getCloudletId();
                    data[2] = CloudSimTags.FALSE;

                    // unique tag = operation tag
                    int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
                    sendNow(cl.getUserId(), tag, data);
                }

                sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

                return;
            }

            // process this Cloudlet to this CloudResource
            cl.setResourceParameter(
                    getId(), getCharacteristics().getCostPerSecond(),
                    getCharacteristics().getCostPerBw());

            int hostId = cl.getVmId();

            // time to transfer the files
            //double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());

            Host host = null;
            for(Host h: getHostList()) {
                if(h.getId() == hostId) {
                    host = h;
                    break;
                }
            }
            assert host != null;
            double estimatedFinishTime = host.submitJob((GpuJob) cl);

            // 任务成功放入执行队列中
            if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
                //estimatedFinishTime += fileTransferTime;
                send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
            }

            if (ack) {
                int[] data = new int[3];
                data[0] = getId();
                data[1] = cl.getCloudletId();
                data[2] = CloudSimTags.TRUE;

                // unique tag = operation tag
                int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
                sendNow(cl.getUserId(), tag, data);
            }
        } catch (ClassCastException c) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
            c.printStackTrace();
        } catch (Exception e) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
            e.printStackTrace();
        }

        checkCloudletCompletion();
    }

    @Override
    protected void updateCloudletProcessing() {
        if (CloudSim.clock() < 0.111 || CloudSim.clock() > getLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
            double smallerTime = Double.MAX_VALUE;
            // 遍历每一个物理节点
            for (Host host: getHostList()) {
                // 物理节点更新任务状态
                double time = host.updateJobsProcessing(CloudSim.clock());
                // 我们关心的是任务何时完成
                if (time < smallerTime) {
                    smallerTime = time;
                }
            }
            // 保证时间间隔小于最小值
            if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01) {
                smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01;
            }
            if (smallerTime != Double.MAX_VALUE) {
                schedule(getId(), (smallerTime - CloudSim.clock()), CloudSimTags.VM_DATACENTER_EVENT);
            }
            setLastProcessTime(CloudSim.clock());
        }
    }

    @Override
    protected void checkCloudletCompletion() {
        for (Host host: getHostList()) {
            while (host.isFinishedCloudlets()) {
                Cloudlet cl = host.getNextFinishedCloudlet();
                if (cl != null) {
                    sendNow(getId(), GpuCloudSimTags.GPU_MEMORY_TRANSFER, cl);
                }
            }
        }
        for (Host h: getHostList()) {
            GpuHost host = (GpuHost) h;
            while (host.hasGpuTask()) {
                GpuTask gt = host.getNextGpuTask();
                sendNow(getId(), GpuCloudSimTags.GPU_MEMORY_TRANSFER, gt);
            }
        }
    }
}
