scenarios README

A single scenario is defined by a single <scenario>.properties file.
To setup one or more scenarios to run at the same time, create a folder under scenarios and place the scenario properties file there.  The scenario_template_folder is available as an example for setting up a new scenario folder and defining a scenario.
There a folders that contain the scenario properties file because the testing process will execute all scenarios for which there is a properties file found in the folder.   This will allow of grouping common testing scenarios that have the same "jar" dependencies.   This is needed because multiple versions of the same database driver (i.e., oracle) cannot be in the server at the same time.   At least not until JBoss AS 7 is released.
