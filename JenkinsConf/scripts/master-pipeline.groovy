def buildJobName        = env.buildJobName
def testJobName         = env.testJobName
def uploadJobName       = env.uploadJobName
def deployJobName       = env.deployJobName
def additional_param_1  = env.additional_param_1

timestamps {
    def service1Tag
    def service2Tag
    stage('Build Docker Image-Service 1') {
    
            def buildObj1 = build job: buildJobName, parameters:      
                    [[$class:'StringParameterValue', name: 'version', value: service1Version],
                    [$class: 'StringParameterValue', name: 'serviceName', value: "Service1"]]
            service1Tag = buildObj1.getRawBuild().environment.get("SERVICE_TAG")
    }
    stage('Build Docker Image-Service 2') {
        
            def buildObj2 = build job: buildJobName, parameters: 
                    [[$class: 'StringParameterValue', name: 'version', value: service2Version],
                    [$class: 'StringParameterValue', name: 'serviceName', value: "Service2"]]
            service2Tag = buildObj2.getRawBuild().environment.get("SERVICE_TAG")
    }
    stage('Run Unit Test-Service1') {
        
        def buildObj3 = build job: testJobName, parameters: 
            [[$class: 'StringParameterValue', name: 'serviceTag', value: service1Tag]]
    }
    stage('Run Unit Test-Service2') {
        
        def buildObj4 = build job: testJobName, parameters: 
            [[$class: 'StringParameterValue', name: 'serviceTag', value: service2Tag]]
    }
    stage('Publish to Docker Hub') {
        
        def buildObj5 = build job: uploadJobName, parameters: 
            [[$class: 'StringParameterValue',  name: 'service1Tag', value: service1Tag],
            [$class: 'StringParameterValue',   name: 'service2Tag', value: service2Tag]]
    }

    stage('Deploy images') {

        if (deployServices){
                def buildObj6 = build job: deployJobName, propagate: false, parameters: 
                [[$class: 'StringParameterValue',      name: 'version_service1',       value: service1Tag.substring( service1Tag.indexOf(":v") +1 )],
                [$class: 'StringParameterValue',       name: 'version_service2',       value: service2Tag.substring( service2Tag.indexOf(":v") +1 )],
                [$class: 'StringParameterValue',       name: 'additional_param_1',     value: additional_param_1]]
        }else {
                println "Deploy skipped"
        }
    }
}