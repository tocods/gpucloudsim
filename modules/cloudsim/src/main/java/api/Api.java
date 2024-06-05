package api;

import api.service.Service;
import api.util.ParseUtil;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.gpu.GpuCloudlet;
import org.cloudbus.cloudsim.gpu.power.PowerGpuHost;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Api {
    /**
     * 输入文件解析器
     */
    private ParseUtil parser;

    private Result doSchema(File file) {
        Result ret = Result.success(null);
        return ret;
    }

    public void setParser(ParseUtil parser) {
        this.parser = parser;
    }


    /**
     *  设置集群中的物理节点
     */

    private List<PowerGpuHost> hostList;
    private void setHosts(List<PowerGpuHost> hosts) {
        hostList = new ArrayList<>(hosts);
    }

    private List<PowerGpuHost> getHosts() {
        return hostList;
    }

    private Result parseHosts(String path) throws ParameterException {
        Result ret;
        File f = new File(path);
        ret = doSchema(f);
        try {
            setHosts(parser.parseHostXml(f));
        }catch (Exception e){
            ret = Result.error(null, "解析物理节点输入文件失败");
        }
        return ret;
    }

    /**
     *  设置集群中的任务(pod、container...)
     */

    private List<GpuCloudlet> taskList;

    private void setTasks(List<GpuCloudlet> tasks) {
        taskList = new ArrayList<>(tasks);
    }

    private List<GpuCloudlet> getTasks() {
        return taskList;
    }

    private Result parseTasks(String path) throws ParameterException {
        Result ret;
        File f = new File(path);
        ret = doSchema(f);
        try {
            setTasks(parser.parseTaskXml(f));
        }catch (Exception e) {
            ret = Result.error(null, "解析任务输入文件失败");
        }
        return ret;
    }


    /**
     * 仿真
     */
    public Result start() {
        Service service = new Service(getHosts(), getTasks());
        try {
            service.start();
        }catch (Exception e) {
            return Result.error(e.getMessage(), "仿真失败");
        }
        return Result.success("仿真成功");
    }
}
