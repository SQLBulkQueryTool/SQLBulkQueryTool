scenarios README

A single scenario is defined by a single <scenario>.properties file.  
One or more scenarios can be run at one time, in sequential order.
When creating a scenario file, make a copy of the scenario.properties.template in the scenarios folder and name it {name}.properties.

**** Run single scenario ****

To run a single scenario, make a copy of the scenario.properties.template in the scenarios folder and name it {name}.properties.
Edit the properties according to the template.
When executing ./run.sh   pass in the <scenario>.properties file name

A single scenario can be created and placed in the scenarios directory. To setup where a group of scenarios are run at one time,
create a folder under the scenarios folder and place the scenario .properties file there.  And 
To setup one or more scenarios to run at the same time, create a folder under scenarios and place the scenario properties file there.


**** Run multiple scenarios ****

To run multiple scenarios in one execution, in sequential order. then do the following:
-  create a new folder under the scenarios folder
-  create one scenario properties file for each scenario to run in the group
-  when executing ./run.sh  pass in the folder that contains the scenario files
