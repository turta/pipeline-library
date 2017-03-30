#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def proceedMessage = """Version ${config.version} has now been deployed to the ${config.environment} environment at:
${config.console}/kubernetes/pods?environment=${config.environment}

Would you like to promote version ${config.version} to the Production namespace?
"""

    hubotApprove message: proceedMessage, room: config.room
    def id = approveRequestedEvent(app: "${env.JOB_NAME}", environment: config.environment)

    try {
        input id: 'Proceed', message: "\n${proceedMessage}"
    } catch (err) {
        approveReceivedEvent(id: id, approved: false)
        throw err
    }
    approveReceivedEvent(id: id, approved: true)
}
