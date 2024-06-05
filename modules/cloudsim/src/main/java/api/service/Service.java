package api.service;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.gpu.GpuCloudlet;
import org.cloudbus.cloudsim.gpu.GpuDatacenter;
import org.cloudbus.cloudsim.gpu.GpuVm;
import org.cloudbus.cloudsim.gpu.hardware_assisted.grid.GridGpuVmAllocationPolicyBreadthFirst;
import org.cloudbus.cloudsim.gpu.placement.GpuDatacenterBrokerEx;
import org.cloudbus.cloudsim.gpu.power.PowerGpuDatacenter;
import org.cloudbus.cloudsim.gpu.power.PowerGpuDatacenterBroker;
import org.cloudbus.cloudsim.gpu.power.PowerGpuHost;

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


    private PowerGpuDatacenter createDatacenter() {
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

        PowerGpuDatacenter datacenter = null;
        try {
            datacenter = new PowerGpuDatacenter("Datacenter", characteristics,
                    new GridGpuVmAllocationPolicyBreadthFirst(hosts), storageList, 20);
        } catch (Exception e) {
            return null;
        }
        return datacenter;
    }

    private PowerGpuDatacenterBroker createBroker() {
        PowerGpuDatacenterBroker broker = null;
        try {
            broker = new PowerGpuDatacenterBroker("Broker");
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

            GpuDatacenter datacenter = createDatacenter();
            assert datacenter != null;

            PowerGpuDatacenterBroker broker = createBroker();
            assert broker != null;
            broker.submitVmList(new ArrayList<>());
            broker.submitCloudletList(tasks);

            Log.disable();
            CloudSim.startSimulation();

            CloudSim.stopSimulation();
            Log.enable();

        } catch (Exception e) {
            Log.printLine("仿真出现错误");
        }
    }
}
