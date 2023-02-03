pipelineJob("Master-pipeline") {
	description()
	keepDependencies(false)
	parameters {
		stringParam("service1Version", "v0.0", "Defines Docker image tag, default is latest, for verisoning use vX.Y (v0.1)")
		stringParam("service2Version", "v0.0", "Defines Docker image tag, default is latest, for verisoning use vX.Y (v0.1)")
	}
	definition {
		cpsScm {
            """timestamps {
                def service1Tag
                def service2Tag
                stage('Build Docker Image-Service 1') {
                
                        def buildObj1 = build job: 'Docker-Build', parameters:      
                                [[\$class:'StringParameterValue', name: 'version', value: service1Version],
                                [\$class: 'StringParameterValue', name: 'serviceName', value: "Service1"]]
                        service1Tag = buildObj1.getRawBuild().environment.get("SERVICE_TAG")
                }
                stage('Build Docker Image-Service 2') {
                    
                        def buildObj2 = build job: 'Docker-Build', parameters: 
                                [[\$class: 'StringParameterValue', name: 'version', value: service2Version],
                                [\$class: 'StringParameterValue', name: 'serviceName', value: "Service2"]]
                        service2Tag = buildObj2.getRawBuild().environment.get("SERVICE_TAG")
                }
                stage('Run Unit Test-Service1') {
                    
                    def buildObj3 = build job: 'Docker-Test', parameters: 
                        [[\$class: 'StringParameterValue', name: 'serviceTag', value: service1Tag]]
                }
                stage('Run Unit Test-Service2') {
                    
                    def buildObj3 = build job: 'Docker-Test', parameters: 
                        [[\$class: 'StringParameterValue', name: 'serviceTag', value: service2Tag]]
                }
                stage('Publish to Docker Hub') {
                    
                    def buildObj3 = build job: 'Docker-Upload', parameters: 
                        [[\$class: 'StringParameterValue', name: 'service1Tag', value: service1Tag],
                        [\$class: 'StringParameterValue', name: 'service2Tag', value: service2Tag]]
                }
            }"""		
        }
	}
	disabled(false)
	configure {
		it / 'properties' / 'com.coravy.hudson.plugins.github.GithubProjectProperty' {
			'projectUrl'('https://github.com/Dmausa/DevopsTest/')
			displayName("DevopsTest")
		}
		it / 'properties' / 'com.sonyericsson.rebuild.RebuildSettings' {
			'autoRebuild'('false')
			'rebuildDisabled'('false')
		}
	}
}

job('Docker-Build') {

    parameters {
        stringParam('version', 'v1.0', 'Version of docker image')
        stringParam('serviceName', '', 'Define folder name containing Dockerfile (Serivce1 or Serive2)')
    }
    steps {
        shell('''cd $WORKSPACE/$serviceName
                LOWERCASE_SERVICE_NAME=$(echo $serviceName | tr '[:upper:]' '[:lower:]')
                docker build -f Dockerfile -t $LOWERCASE_SERVICE_NAME:$version .
                touch $WORKSPACE/prop.env
                echo SERVICE_TAG=$LOWERCASE_SERVICE_NAME:$version >> $WORKSPACE/prop.env
        ''')
        envInjectBuilder {
            propertiesFilePath('$WORKSPACE/prop.env') 
        }
    }
}

job('Docker-Test') {

    parameters {
        stringParam('serviceTag',   '', 'ex. service1:v1.0')
    }
    steps {
        shell('''docker stop testService || true
                docker rm -f "testService" || true
                docker container ls --all
                docker run -d -p 8081:8080 --name testService $serviceTag
                nc -zv localhost 8081
                docker stop testService
        ''')
    }
}

job('Docker-Upload') {

    description("Note:Jenkins user must be logged in to Docekerhub")
    parameters {
        stringParam('service1Tag',   '', 'ex. service1:v1.0')
        stringParam('service2Tag',   '', 'ex. service2:v1.0')
    }
    steps {
        shell('''docker tag $service1Tag dmausa/$service1Tag
                docker push dmausa/$service1Tag
                docker tag $service2Tag dmausa/$service2Tag
                docker push dmausa/$service2Tag
        ''')
    }
}