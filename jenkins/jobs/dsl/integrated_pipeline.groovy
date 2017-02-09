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
def logIntBuild = CartridgeHelper.getIntegratedJob(this, projectFolderName + '/Integrated_Build', variables + [
        'jobDescription': 'This job logs a new composite configuration (integration) of builds.',
        'triggerDownstreamJob': projectFolderName + '/Integrated_SIT_Deploy',
        'nextCopyArtifactsFromBuild': '${BUILD_NUMBER}',
    ]
)

def intSITDeploy = CartridgeHelper.getShellJob(this, projectFolderName + '/Integrated_SIT_Deploy', variables + [
        'copyArtifactsFromJob': projectFolderName + '/Integrated_Build',
        'triggerDownstreamJob': projectFolderName + '/SIT_Test',
        'nextCopyArtifactsFromBuild': '${B}',
        'jobDescription': 'This job deploys the integrated application to the SIT environment',
        'jobCommand': 'grep = Integrated_Build_${B}.txt | sed "s/\\([^=]\\+\\)=\\([0-9]\\+\\)/\\n\\nDeploying: \\1 version: \\2 (TODO...)\\n\\n/"'
    ]
)


def intSITTest = CartridgeHelper.getShellJob(this, projectFolderName + '/SIT_Test', variables + [
        'copyArtifactsFromJob': projectFolderName + '/Integrated_Build',
        'nextCopyArtifactsFromBuild': '${B}',
        'triggerDownstreamJob': projectFolderName + '/Publish',
        'jobDescription': 'This job runs automated testing in the SIT environment',
    ]
)

def intPublish = CartridgeHelper.getShellAuthJob(this, projectFolderName + '/Publish', variables + [
        'copyArtifactsFromJob': projectFolderName + '/Integrated_Build',
        'nextCopyArtifactsFromBuild': '${B}',
        'triggerDownstreamJob': projectFolderName + '/NA',
        'jobDescription': 'This job publishes this build to later pipelines',
        'jobCommand': 'FOLDER=`echo ' + projectFolderName + ''' | sed "s/\\//\\/job\\//g"`; 
                      |set +x
                      |echo TRIGGERING INTEGRATION BUILD TO PUBLISH THE PRODUCT OF THIS PIPELINE
                      |echo
                      |echo TODO RANDOM is not a good name times two
                      |docker exec jenkins curl -s -X POST ${USERNAME_JENKINS}:${PASSWORD_JENKINS}@localhost:8080/jenkins/job/${FOLDER}/job/${RANDOM}/buildWithParameters?COMPONENT_NAME=${RANDOM}\\&COMPONENT_BUILD_NUMBER=${B}''',
    ]
)


// Views
def rolePipelineView = CartridgeHelper.basePipelineView(
    this,
    projectFolderName + '/Integrated_Pipeline',
    projectFolderName + '/Integrated_Build',
    'Logs an integrated build and then provides a pipeline for it.'
)

