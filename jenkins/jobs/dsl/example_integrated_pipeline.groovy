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
        'manualTrigger': 'true',
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
        'triggerDownstreamJob': projectFolderName + '/Integrated_PPE_Deploy',
        'jobDescription': 'This job runs automated testing in the SIT environment',
    ]
)


def intPPEDeploy = CartridgeHelper.getShellJob(this, projectFolderName + '/Integrated_PPE_Deploy', variables + [
        'copyArtifactsFromJob': projectFolderName + '/Integrated_Build',
        'nextCopyArtifactsFromBuild': '${B}',
        'triggerDownstreamJob': projectFolderName + '/PPE_Test',
        'jobDescription': 'This job deploys the integrated application to the PPE environment',
    ]
)


def intPPETest = CartridgeHelper.getShellJob(this, projectFolderName + '/PPE_Test', variables + [
        'copyArtifactsFromJob': projectFolderName + '/Integrated_Build',
        'nextCopyArtifactsFromBuild': '${B}',
        'triggerDownstreamJob': projectFolderName + '/Integrated_Prod_Deploy',
        'jobDescription': 'This job runs automated testing in the PPE environment',
    ]
)


def intProdDeploy = CartridgeHelper.getShellJob(this, projectFolderName + '/Integrated_Prod_Deploy', variables + [
        'copyArtifactsFromJob': projectFolderName + '/Integrated_Build',
        'nextCopyArtifactsFromBuild': '${B}',
        'triggerDownstreamJob': projectFolderName + '/Prod_Test',
        'jobDescription': 'This job deploys the integrated application to the Prod environment',
    ]
)


def intProdTest = CartridgeHelper.getShellJob(this, projectFolderName + '/Prod_Test', variables + [
        'copyArtifactsFromJob': projectFolderName + '/Integrated_Build',
        'nextCopyArtifactsFromBuild': '${B}',
        'triggerDownstreamJob': projectFolderName + '/NA',
        'jobDescription': 'This job runs automated testing in the Prod environment',
    ]
)



// Views
def rolePipelineView = CartridgeHelper.basePipelineView(
    this,
    projectFolderName + '/Integrated_Pipeline',
    projectFolderName + '/Integrated_Build',
    'Logs an integrated build and then provides a pipeline for it.'
)

