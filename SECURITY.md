# Security Policy
- Input Validation Policy: The vulnerability does not prevent the use of malicious executable classes such as java.lang.Runtime,... leading to the possibility of executing arbitrary Java code
## Supported Versions

Currently I am using version 10.0.0.xx and I have not used higher versions but are likely to be affected.

| Version      | Supported          |
| -------      | ------------------ |
| < 10.0.x.x   | :white_check_mark: |

## Reporting a Vulnerability

### Submission title
- Code Injection Via .cda file
### Target
- \{url\}/pentaho/plugin/cda/api/previewQuery?path={path}
### Description
- First, I copied the example.cda file at the link below to the test user so as not to affect system data.
  
![image](https://github.com/pentaho/pentaho-platform/assets/81729607/04d4a381-6694-464c-8620-c18bcbd6c486)
- This is unedited data as you can see the data uses Scripting query to create the data table
  
![image](https://github.com/pentaho/pentaho-platform/assets/81729607/040f43a4-e65f-44a1-8958-a477ab5f1dd0)
- "These data sources allow you to create ad hoc result sets, such as a small table, for prototyping purposes using Beanshell scripts. These result sets are useful during the dashboard development phase for generating data for a dashboard’s components when real data is not yet available."
- "Using the Beanshell scripting language, we can define a data structure and then create a result set based on this same structure to use in a component. You will need to define the column names, column types, and the result set rows. "
- The default allowScriptEvaluation status is set to true, but to use the beanshell feature, the system administrator will turn this configuration off.
- The reason is that the system does not filter any dangerous functions or classes before giving it to the Beanshell to execute.
- Therefore I can insert any dangerous Java code into the system to attack remote access.
- Below is PoC.
  
  1, Edit example.cda with dangerous Java code
  
  ![image](https://github.com/pentaho/pentaho-platform/assets/81729607/dfb4a46a-81d4-4e5f-ac63-047227441ead)
  2, Click Preview and the code has been executed. Url : {url}/pentaho/plugin/cda/api/previewQuery?path=/home/test/sample.cda
  
  ![Untitled](https://github.com/pentaho/pentaho-platform/assets/81729607/a91a1b9c-e926-468b-b5ff-d3614b5e7327)
  3, Try with payload "pwd"
  
  ![image](https://github.com/pentaho/pentaho-platform/assets/81729607/e18d0f3e-00ba-44bb-88f1-7a1ca67c8bfa)
### Recommended
- Block dangerous Java classes that execute or use while-list trusted classes or functions.



  
  





