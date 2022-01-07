properties([
	buildDiscarder(logRotator(numToKeepStr: '10')),
	disableConcurrentBuilds(),
	pipelineTriggers([
		cron('H H * * H'),
	]),
])

node {
	stage('Checkout') {
		checkout([
			$class: 'GitSCM',
			userRemoteConfigs: [
				[url: 'git@github.com:lanen/orchid-pipelines.git',
        credentialsId: 'orchid-pipeline-bot']
			],
			branches: [
				[name: '*/main'],
			],
			extensions: [
				[
					$class: 'CleanCheckout',
				],
				[
					$class: 'RelativeTargetDirectory',
					relativeTargetDir: 'orchid-pipelines',
				],
			],
			doGenerateSubmoduleConfigurations: false,
			submoduleCfg: [],
		])
	}

	stage('Generate') {
		jobDsl(
			lookupStrategy: 'SEED_JOB',
			removedJobAction: 'DELETE',
			removedViewAction: 'DELETE',
			targets: 'orchid-pipelines/update.sh/dsl.groovy',
		)
	}
}
