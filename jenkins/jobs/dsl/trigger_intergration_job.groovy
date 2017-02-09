import jenkins.jobs.dsl.base.CartridgeHelper

// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def variables = [
    projectNameKey          : (projectFolderName.toLowerCase().replace("/", "-")),
    buildSlave              : 'docker',
    sshAgentName            : 'adop-jenkins-master',
    logRotatorNum           : 10,
    logRotatorArtifactNum   : 3,
    logRotatorDays          : -1,
    logRotatorArtifactDays  : -1,
    projectFolderName       : projectFolderName,
    workspaceFolderName     : workspaceFolderName,
    absoluteJenkinsHome     : '/var/lib/docker/volumes/jenkins_home/_data',
    absoluteJenkinsSlaveHome: '/var/lib/docker/volumes/jenkins_slave_home/_data',
    absoluteWorkspace       : '${ABSOLUTE_JENKINS_SLAVE_HOME}/${JOB_NAME}/',
]

// Jobs
def intPublish = CartridgeHelper.getShellAuthJob(this, projectFolderName + '/Publish_Example', variables + [
        'nextCopyArtifactsFromBuild': '${B}',
        'triggerDownstreamJob': projectFolderName + '/NA',
        'jobDescription': 'This job publishes this build to later pipelines',
        'jobCommand': 'FOLDER=`echo ' + projectFolderName + ''' | sed "s/\\//\\/job\\//g"`; 
                      |set +x
                      |echo TRIGGERING INTEGRATION BUILD TO PUBLISH THE PRODUCT OF THIS PIPELINE
                      |echo
                      |echo TODO RANDOM is not a good name
                      |docker exec jenkins curl -s -X POST ${USERNAME_JENKINS}:${PASSWORD_JENKINS}@localhost:8080/jenkins/job/${FOLDER}/job/Integrated_Build/buildWithParameters?COMPONENT_NAME=${RANDOM}\\&COMPONENT_BUILD_NUMBER=${B}',
    ]
)


// Views
def rolePipelineView = CartridgeHelper.basePipelineView(
    this,
    projectFolderName + '/Integrated_Pipeline',
    projectFolderName + '/Integrated_Build',
    'Logs an integrated build and then provides a pipeline for it.'
)

