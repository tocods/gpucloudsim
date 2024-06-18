package workflow.taskCluster;

import gpu.GpuCloudlet;
import gpu.GpuTask;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import workflow.GpuJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicClustering implements ClusteringInterface {

    /**
     * The task list.
     */
    private List<? extends Cloudlet> taskList;
    /**
     * The tasks list.
     */
    private final List<GpuJob> jobList;
    /**
     * maps from task to its tasks.
     */
    private final Map mTask2Job;


    /**
     * The root task.
     */
    private GpuCloudlet root;
    /**
     * the id index.
     */
    private int idIndex;



    /**
     * Initialize a BasicClustering object
     */
    public BasicClustering() {
        this.jobList = new ArrayList<>();
        this.taskList = new ArrayList<>();
        this.mTask2Job = new HashMap<>();
        this.idIndex = 0;
        this.root = null;
    }

    /**
     * Sets the task list
     *
     * @param list task list
     */
    @Override
    public void setTaskList(List<? extends Cloudlet> list) {
        this.taskList = list;
    }

    /**
     * Gets the tasks list
     *
     * @return tasks list
     */
    @Override
    public final List<GpuJob> getJobList() {
        return this.jobList;
    }

    /**
     * Gets the task list
     *
     * @return task list
     */
    @Override
    public final List<? extends Cloudlet> getTaskList() {
        return this.taskList;
    }

    /**
     * Gets the map from task to tasks
     *
     * @return map
     */
    public final Map getTask2Job() {
        return this.mTask2Job;
    }

    /**
     * The main function of BasicClustering
     */
    @Override
    public void run() {
        getTask2Job().clear();
        for (Cloudlet cl : getTaskList()) {
            GpuCloudlet task = (GpuCloudlet)cl;
            List<GpuTask> list = new ArrayList<>();
            list.add(task.getGpuTask());
            GpuJob job = addTasks2Job(list);
            job.setVmId(task.getVmId());
            getTask2Job().put(task, job);
        }
        /**
         * Handle the dependencies issue.
         */
        updateDependencies();

    }

    /**
     * Add a task to a new tasks
     *
     * @param task the task
     * @return tasks the newly created tasks
     */
    protected final GpuJob addTasks2Job(GpuTask task) {
        List<GpuTask> tasks = new ArrayList<>();
        tasks.add(task);
        return addTasks2Job(tasks);
    }

    /**
     * Add a list of task to a new tasks
     *
     * @param taskList the task list
     * @return tasks the newly created tasks
     */
    protected GpuJob addTasks2Job(List<GpuTask> taskList) {
        if (taskList != null && !taskList.isEmpty()) {
            int length = 0;
            int userId = 0;
            GpuJob job = new GpuJob(idIndex, length, null);
            for (GpuTask task : taskList) {
                length += task.getCloudlet().getCloudletLength();
                userId = task.getCloudlet().getUserId();
                job.addGpuTask(task);
                getTask2Job().put(task, job);
            }
            job.setCloudletLength(length);
            job.setUserId(userId);
            idIndex++;
            getJobList().add(job);
            return job;
        }

        return null;
    }

    /**
     * Update the dependency issues between tasks/jobs
     */
    protected final void updateDependencies() {
        for (Cloudlet cl : getTaskList()) {
            GpuCloudlet task = (GpuCloudlet) cl;
            GpuJob job = (GpuJob) getTask2Job().get(task.getGpuTask());
            Log.printLine(job.getCloudletId() + " is job of task " + task.getCloudletId());
            for (GpuTask parentTask : task.getGpuTask().getParent()) {
                GpuJob parentJob = (GpuJob) getTask2Job().get(parentTask);
                if (!job.getParent().contains(parentJob) && parentJob != job) {
                    job.addParent(parentJob);
                }
            }
            for (GpuTask childTask : task.getGpuTask().getChildren()) {
                GpuJob childJob = (GpuJob) getTask2Job().get(childTask);
                if (!job.getChildren().contains(childJob) && childJob != job) {
                    job.addChild(childJob);
                }
            }
        }
        getTask2Job().clear();
        getTaskList().clear();
    }

}
