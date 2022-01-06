def vars = (new GroovyShell()).evaluate(readFileFromWorkspace('orchid-pipelines/update.sh/vars.groovy'))

for (repo in vars.repos) {
	def repoMeta = vars.repoMeta(repo)

	pipelineJob(repo) {
		logRotator { daysToKeep(4) }
		// TODO concurrentBuild(false)
		// see https://issues.jenkins-ci.org/browse/JENKINS-31832?focusedCommentId=343307&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-343307
		configure { it / 'properties' << 'org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty' { } }
		triggers {
			cron('H H/6 * * *')
		}
		definition {
			cpsScm {
				scm {
					github('lanen/orchid-pipelines')
					scriptPath(repoMeta['pipeline-script'])
				}
			}
		}
		configure {
			it / definition / lightweight(true)
		}
	}
}
