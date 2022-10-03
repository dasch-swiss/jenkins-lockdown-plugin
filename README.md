# Jenkins Lockdown Plugin

This Jenkins plugin allows users to lock down/disable pipelines with a custom message. The plugin can also show a customizable overview of all locked down pipelines on Jenkins' homepage (and also in Blue Ocean frontend).

### Usage
Once enabled for a specific job, go to the job's page and then press the Lockdown button in the left sidepanel. Click on the Lock down link, enter a message and click on the Lock down button to confirm.  
The lockdown can be undone in the same menu by pressing the Stop lockdown button.

### Settings
|Setting|Use|
|-|-|
|Job > Enable lockdowns|Enables lockdowns for this job.|
|Manage > Configure System > Use system message for lockdown messages|Allows the plugin to display a lockdown overview in the system message field on Jenkins' homepage. Existing system messages will not be overwritten.|
|Manage > Configure System > Lockdown message template|Template for the full lockdown overview message. A safe subset of HTML can be used if the OWASP Markup Formatter plugin is installed and enabled.|
|Manage > Configure System > Job message template|Template for a single job lockdown message which is used in the setting above.|

### Permissions
|Permission|Use|
|-|-|
|Read|Required for users to be able to view a pipeline's lockdown state and to access the plugin's API. Also required for being able to start and stop lockdowns.|
|Start|Allows users to start lockdowns.|
|Stop|Allows users to stop lockdowns.|

### Development
Starting a development Jenkins instance with this plugin: `mvn hpi:run`

Building the plugin: `mvn package`
