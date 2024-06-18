/**
 * Copyright 2019-2020 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.com.wfc.cloudsim/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package api.util;


import gpu.GpuCloudlet;
import gpu.power.PowerGpuHost;

import java.io.File;
import java.util.List;



public final class ParseUtil {
    public ParseUtil(int userId) {

    }

    public List<PowerGpuHost> parseHostXml(File f) {
        List<PowerGpuHost> hostList = null;
        return hostList;
    }

    public List<GpuCloudlet> parseTaskXml(File f) {
        List<GpuCloudlet> taskList = null;
        return taskList;
    }

}
