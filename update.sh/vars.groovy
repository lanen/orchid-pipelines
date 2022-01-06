// https://github.com/jenkinsci/pipeline-examples/blob/666e5e3f8104efd090e698aa9b5bc09dd6bf5997/docs/BEST_PRACTICES.md#groovy-gotchas
// tl;dr, iterating over Maps in pipeline groovy is pretty broken in real-world use
def defaultRepoMeta = [
	['url', 'git@github.com:lanen/%%REPO%%.git'],
	['oi-fork', 'git@github.com:docker-library-bot/official-images.git'],
	['pipeline-script', 'update.sh/target-pipeline.groovy'],
	['env', '.+_VERSION'], // gawk regex, anchored
	['otherEnvs', []],
	['branch-base', 'master'], // branch to check out from
	['branch-push', 'master'], // branch to push to
	['update-script', './update.sh'],
]
def rawReposData = [
	['busybox', [
		'env': 'BUSYBOX_VERSION',
		'update-script': 'true', // TODO determine if more can/should be done here
	]],
	['cassandra', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['docker', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['drupal', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['gcc', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['ghost', [
		'env': 'GHOST_VERSION',
		'otherEnvs': [
			['ghost-cli', 'GHOST_CLI_VERSION'],
		],
	]],
	['golang', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['haproxy', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['httpd', [
		'env': 'HTTPD_VERSION',
		'otherEnvs': [
			['nghttp2', 'NGHTTP2_VERSION'],
			['openssl', 'OPENSSL_VERSION'],
		],
	]],
	['julia', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['memcached', [
		'env': 'MEMCACHED_VERSION',
	]],
	['mongo', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['mysql', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['openjdk', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['php', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['postgres', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['pypy', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['python', [
		'env': 'PYTHON_VERSION',
		'otherEnvs': [
			['pip', 'PYTHON_PIP_VERSION'],
			['setuptools', 'PYTHON_SETUPTOOLS_VERSION'],
			['wheel', 'PYTHON_WHEEL_VERSION'],
		],
	]],
	['rabbitmq', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['redis', [
		'env': 'REDIS_VERSION',
	]],
	['redmine', [
		'env': 'REDMINE_VERSION',
		'otherEnvs': [
			['passenger', 'PASSENGER_VERSION'],
		],
	]],
	['ruby', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['tomcat', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['wordpress', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],

	// Elastic images (specialized FROM tags)
	['elasticsearch', [
		'env': 'ELASTICSEARCH_VERSION',
		'from': 'docker.elastic.co/elasticsearch/elasticsearch',
	]],
	['logstash', [
		'env': 'LOGSTASH_VERSION',
		'from': 'docker.elastic.co/logstash/logstash',
	]],
	['kibana', [
		'env': 'KIBANA_VERSION',
		'from': 'docker.elastic.co/kibana/kibana',
	]],

	// versionless
	['buildpack-deps', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
	]],
	['hello-world', [
		'update-script': 'true',
	]],

	// tianon
	['bash', [
		'pipeline-script': 'update.sh/versions-pipeline.groovy',
		'url': 'git@github.com:tianon/docker-bash.git',
	]],
	['cirros', [
		'url': 'git@github.com:tianon/docker-brew-cirros.git',
		'update-script': 'true',
	]],
	['irssi', [
		'url': 'git@github.com:jessfraz/irssi.git',
		'env': 'IRSSI_VERSION',
	]],

	// TimWolla
	['adminer', [
		'url': 'git@github.com:TimWolla/docker-adminer.git',
		'env': 'ADMINER_VERSION',
		'branch-push': 'docker-library-bot',
	]],

	// pierreozoux
	['matomo', [
		'url': 'git@github.com:matomo-org/docker.git',
		'env': 'MATOMO_VERSION',
	]],

	// paultag
	['hylang', [
		'url': 'git@github.com:hylang/docker-hylang.git',
		'env': 'HY_VERSION',
	]],

	// knickers
	['mongo-express', [
		'url': 'git@github.com:mongo-express/mongo-express-docker.git',
		'env': 'MONGO_EXPRESS',
	]],

	// tilosp
	['nextcloud', [
		'url': 'https://github.com/nextcloud/docker.git',
		'update-script': 'true', // "update.sh" is handled via GitHub Actions
	]],
]

// list of repos: ["celery", "wordpress", ...]
repos = []

// map of repo metadata: ["celery": ["url": "...", ...], ...]
reposMeta = [:]
def repoMeta(repo) {
	return reposMeta[repo]
}

for (int i = 0; i < rawReposData.size(); ++i) {
	def repo = rawReposData[i][0]
	def repoMeta = rawReposData[i][1]

	// apply "defaultRepoMeta" for missing bits
	//   wouldn't it be grand if we could just use "map1 + map2" here??
	//   dat Jenkins sandbox...
	for (int j = 0; j < defaultRepoMeta.size(); ++j) {
		def key = defaultRepoMeta[j][0]
		def val = defaultRepoMeta[j][1]
		if (repoMeta[key] == null) {
			repoMeta[key] = val
		}
	}

	repoMeta['url'] = repoMeta['url'].replaceAll('%%REPO%%', repo)

	repos << repo
	reposMeta[repo] = repoMeta
}

// return "this" (for use via "load" in Jenkins pipeline, for example)
this
