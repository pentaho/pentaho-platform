package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JobParamsAdapter extends XmlAdapter<JobParams, Map<String, Serializable>>{

  public JobParams marshal(Map<String, Serializable> v) throws Exception {
    ArrayList<JobParam> params = new ArrayList<JobParam>();
    for (Map.Entry<String, Serializable> entry : v.entrySet()) {
      JobParam jobParam = new JobParam();
      jobParam.name = entry.getKey();
      jobParam.value = entry.getValue().toString();
      params.add(jobParam);
    }
    JobParams jobParams = new JobParams();
    jobParams.jobParams = params.toArray(new JobParam[0]);
    return jobParams;
  }

  public Map<String, Serializable> unmarshal(JobParams v) throws Exception {
    HashMap<String, Serializable> paramMap = new HashMap<String, Serializable>();
    for (JobParam jobParam : v.jobParams) {
      paramMap.put(jobParam.name, jobParam.value);
    }
    return paramMap;
  }

}
