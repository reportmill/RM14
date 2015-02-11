
Eclipse Build Instructions
--------------------------

This project is already an Eclipse project, so you should just be able to import it. However, to create from scratch follow the steps below.

1. Create New Java Project with this project directory (really just need src and lib).

2. Go to Project Properties -> Java Build Path -> Libraries and add all lib jars.

3. (Optional) Go to Project Properties-> Java Compiler -> Errors/Warnings and set:
	- Potential programming problems: Serializable class without serialVersionUID: Ignore
	- Potential programming problems: Incomplete ‘switch’ cases on enum: Ignore
	- Generic types -> Uncheck generic type operation: Ignore
	- Generic types -> Usage of a raw type: Ignore

4. Go to Run Configurations and add new Java Application configuration with main class:
	- ReportMill: com.reportmill.App
	
Jar Versions
---------------

	- poi-3.7.jar: Poi 3.7
