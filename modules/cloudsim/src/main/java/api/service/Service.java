package api.service;

import gpu.*;
import gpu.allocation.VideoCardAllocationPolicy;
import gpu.allocation.VideoCardAllocationPolicyNull;
import gpu.hardware_assisted.grid.*;
import gpu.performance.models.PerformanceModel;
import gpu.performance.models.PerformanceModelGpuConstant;
import gpu.power.PowerVideoCard;
import gpu.power.models.GpuHostPowerModelLinear;
import gpu.power.models.VideoCardPowerModel;
import gpu.provisioners.GpuBwProvisionerShared;
import gpu.provisioners.GpuGddramProvisionerSimple;
import gpu.provisioners.VideoCardBwProvisioner;
import gpu.provisioners.VideoCardBwProvisionerShared;
import gpu.selection.PgpuSelectionPolicy;
import gpu.selection.PgpuSelectionPolicyNull;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import gpu.power.PowerGpuDatacenter;
import gpu.power.PowerGpuDatacenterBroker;
import gpu.power.PowerGpuHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import workflow.GPUWorkflowDatacenter;
import workflow.GPUWorkflowEngine;
import workflow.WorkflowSimTags;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class Service {
    private List<PowerGpuHost> hosts;
    private List<GpuCloudlet> tasks;

    public Service(List<PowerGpuHost> hosts, List<GpuCloudlet> tasks) {
        this.hosts = new ArrayList<>(hosts);
        this.tasks = new ArrayList<>(tasks);
    }


    private GPUWorkflowDatacenter createDatacenter() {
        String arch = "x86";
        // 操作系统
        String os = "Linux";
        // vmm
        String vmm = "Horizen";
        // time zone this resource located (Tehran)
        double time_zone = +3.5;
        // the cost of using processing in this resource
        double cost = 0.0;
        // the cost of using memory in this resource
        double costPerMem = 0.00;
        // the cost of using storage in this resource
        double costPerStorage = 0.000;
        // the cost of using bw in this resource
        double costPerBw = 0.0;
        // we are not adding SAN devices by now
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hosts, time_zone,
                cost, costPerMem, costPerStorage, costPerBw);

        GPUWorkflowDatacenter datacenter = null;
        try {
            datacenter = new GPUWorkflowDatacenter("Datacenter", characteristics,
                    new GridGpuVmAllocationPolicyBreadthFirst(hosts), storageList, 20);
        } catch (Exception e) {
            return null;
        }
        return datacenter;
    }

    private GPUWorkflowEngine createBroker() {
        GPUWorkflowEngine broker = null;
        try {
            broker = new GPUWorkflowEngine("Broker");
        } catch (Exception e) {
            return null;
        }
        return broker;
    }

    public void start(){
        Log.printLine("开始仿真");
        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = true;
            CloudSim.init(num_user, calendar, trace_flag);

            GPUWorkflowDatacenter datacenter = createDatacenter();
            assert datacenter != null;

            GPUWorkflowEngine broker = createBroker();
            assert broker != null;
            broker.submitVmList(new ArrayList<>());
            broker.initClusteringInterface();
            broker.initJobAllocationInterface(hosts);
            int brokerId = broker.getId();
            for(GpuCloudlet t: tasks) {
                t.setUserId(brokerId);
                t.setVmId(hosts.get(0).getId());
            }
            broker.submitCloudletList(tasks);

            //Log.disable();
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
            //Log.enable();

        } catch (Exception e) {
            Log.printLine("仿真出现错误");
        }
    }

    //*********************************************************************************
    //*                         以下内容用于调试 Service                                 *
    //*********************************************************************************

    public static void main(String[] args) {
        Service s = new Service(createHosts(), createTasks());
        s.start();
    }
    private static GpuCloudlet createGpuCloudlet(int gpuCloudletId, int gpuTaskId, int brokerId) {
        // Cloudlet properties
        long length = (long) (400 * GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3_PE_MIPS);
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel cpuUtilizationModel = new UtilizationModelFull();
        UtilizationModel ramUtilizationModel = new UtilizationModelFull();
        UtilizationModel bwUtilizationModel = new UtilizationModelFull();

        // GpuTask properties
        long taskLength = (long) (GridVideoCardTags.NVIDIA_K1_CARD_PE_MIPS * 150);
        long taskInputSize = 128;
        long taskOutputSize = 128;
        long requestedGddramSize = 4 * 1024;
        int numberOfBlocks = 2;
        UtilizationModel gpuUtilizationModel = new UtilizationModelFull();
        UtilizationModel gddramUtilizationModel = new UtilizationModelFull();
        UtilizationModel gddramBwUtilizationModel = new UtilizationModelFull();

        GpuTask gpuTask = new GpuTask(gpuTaskId, taskLength, numberOfBlocks, taskInputSize, taskOutputSize,
                requestedGddramSize, gpuUtilizationModel, gddramUtilizationModel, gddramBwUtilizationModel);

        GpuCloudlet gpuCloudlet = new GpuCloudlet(gpuCloudletId, length, pesNumber, fileSize, outputSize,
                cpuUtilizationModel, ramUtilizationModel, bwUtilizationModel, gpuTask, false);

        gpuCloudlet.setUserId(brokerId);
        return gpuCloudlet;
    }
    public static List<GpuCloudlet> createTasks() {
        List<GpuCloudlet> ret = new ArrayList<>();
        ret.add(createGpuCloudlet(0, 0, 0));
        return ret;
    }

    public static List<PowerGpuHost> createHosts() {
        List<PowerGpuHost> hostList = new ArrayList<>();

        /* Create 2 hosts, one is GPU-equipped */

        // Number of host's video cards
        int numVideoCards = GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3_NUM_VIDEO_CARDS;
        // To hold video cards
        List<VideoCard> videoCards = new ArrayList<VideoCard>(numVideoCards);
        for (int videoCardId = 0; videoCardId < numVideoCards; videoCardId++) {
            List<Pgpu> pgpus = new ArrayList<Pgpu>();
            // Adding an NVIDIA K1 Card
            double mips = GridVideoCardTags.NVIDIA_K1_CARD_PE_MIPS;
            int gddram = GridVideoCardTags.NVIDIA_K1_CARD_GPU_MEM;
            long bw = GridVideoCardTags.NVIDIA_K1_CARD_BW_PER_BUS;
            for (int pgpuId = 0; pgpuId < GridVideoCardTags.NVIDIA_K1_CARD_GPUS; pgpuId++) {
                List<Pe> pes = new ArrayList<Pe>();
                for (int peId = 0; peId < GridVideoCardTags.NVIDIA_K1_CARD_GPU_PES; peId++) {
                    pes.add(new Pe(peId, new PeProvisionerSimple(mips)));
                }
                Pgpu p = new Pgpu(pgpuId, GridVideoCardTags.NVIDIA_K1_GPU_TYPE, pes,
                        new GpuGddramProvisionerSimple(gddram), new GpuBwProvisionerShared(bw));
                p.setGpuTaskScheduler(new GpuTaskSchedulerLeftover());
                pgpus.add(p);
            }
            // Pgpu selection policy
            PgpuSelectionPolicy pgpuSelectionPolicy = new PgpuSelectionPolicyNull();
            // Performance Model
            double performanceLoss = 0.1;
            PerformanceModel<VgpuScheduler, Vgpu> performanceModel = new PerformanceModelGpuConstant(performanceLoss);
            // Scheduler
            GridPerformanceVgpuSchedulerFairShare vgpuScheduler = new GridPerformanceVgpuSchedulerFairShare(
                    GridVideoCardTags.NVIDIA_K1_CARD, pgpus, pgpuSelectionPolicy, performanceModel);
            // PCI Express Bus Bw Provisioner
            VideoCardBwProvisioner videoCardBwProvisioner = new VideoCardBwProvisionerShared(BusTags.PCI_E_3_X16_BW);
            // Video Card Power Model
            VideoCardPowerModel videoCardPowerModel = new GridVideoCardPowerModelK1(false);
            // Create a video card
            PowerVideoCard videoCard = new PowerVideoCard(videoCardId, GridVideoCardTags.NVIDIA_K1_CARD, vgpuScheduler,
                    videoCardBwProvisioner, videoCardPowerModel);
            videoCards.add(videoCard);
        }

        // Create a host
        int hostId = 0;

        // A Machine contains one or more PEs or CPUs/Cores.
        List<Pe> peList = new ArrayList<Pe>();

        // PE's MIPS power
        double mips = GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3_PE_MIPS;

        for (int peId = 0; peId < GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3_NUM_PES; peId++) {
            // Create PEs and add these into a list.
            peList.add(new Pe(0, new PeProvisionerSimple(mips)));
        }

        // Create Host with its id and list of PEs and add them to the list of machines
        // host memory (MB)
        int ram = GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3_RAM;
        // host storage
        long storage = GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3_STORAGE;
        // host BW
        int bw = GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3_BW;
        // Set VM Scheduler
        VmScheduler vmScheduler = new VmSchedulerTimeShared(peList);
        // Host Power Model
        double hostMaxPower = 200;
        double hostStaticPowerPercent = 0.70;
        PowerModel powerModel = new GpuHostPowerModelLinear(hostMaxPower, hostStaticPowerPercent);
        // Video Card Selection Policy
        VideoCardAllocationPolicy videoCardAllocationPolicy = new VideoCardAllocationPolicyNull(videoCards);

        PowerGpuHost newHost = new PowerGpuHost(hostId, GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3,
                new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, vmScheduler,
                videoCardAllocationPolicy, powerModel);
        newHost.setCloudletScheduler(new GpuCloudletSchedulerTimeShared());
        hostList.add(newHost);
        return hostList;
    }
}
