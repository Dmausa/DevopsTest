def buildJobName    = env.buildJobName
def testJobName     = env.testJobName
def uploadJobName   = env.uploadJobName

timestamps {
    def service1Tag
    def service2Tag
    stage('Build Docker Image-Service 1') {
    
            def buildObj1 = build job: buildJobName, parameters:      
                    [[\$class:'StringParameterValue', name: 'version', value: service1Version],
                    [\$class: 'StringParameterValue', name: 'serviceName', value: "Service1"]]
            service1Tag = buildObj1.getRawBuild().environment.get("SERVICE_TAG")
    }
    stage('Build Docker Image-Service 2') {
        
            def buildObj2 = build job: buildJobName, parameters: 
                    [[\$class: 'StringParameterValue', name: 'version', value: service2Version],
                    [\$class: 'StringParameterValue', name: 'serviceName', value: "Service2"]]
            service2Tag = buildObj2.getRawBuild().environment.get("SERVICE_TAG")
    }
    stage('Run Unit Test-Service1') {
        
        def buildObj3 = build job: testJobName, parameters: 
            [[\$class: 'StringParameterValue', name: 'serviceTag', value: service1Tag]]
    }
    stage('Run Unit Test-Service2') {
        
        def buildObj3 = build job: testJobName, parameters: 
            [[\$class: 'StringParameterValue', name: 'serviceTag', value: service2Tag]]
    }
    stage('Publish to Docker Hub') {
        
        def buildObj3 = build job: uploadJobName, parameters: 
            [[\$class: 'StringParameterValue', name: 'service1Tag', value: service1Tag],
            [\$class: 'StringParameterValue', name: 'service2Tag', value: service2Tag]]
    }
}