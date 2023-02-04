pipelineJob("Master-pipeline") {
	description()
	keepDependencies(false)
	parameters {
		stringParam("service1Version", "v1.0", "Defines Docker image tag, default is latest, for verisoning use vX.Y (v0.1)")
		stringParam("service2Version", "v1.0", "Defines Docker image tag, default is latest, for verisoning use vX.Y (v0.1)")
	}
    environmentVariables {
        env(buildJobName,   "Docker-Build")
        env(testJobName,    "Docker-Test")
        env(uploadJobName,  "Docker-Upload")
    }
	definition {
		cps {
            script(readFileFromWorkspace('JenkinsConf/master-pipeline.groovy'))	
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