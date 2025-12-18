node {
    def masterPipeline = load('JenkinsMasterPipeline.groovy')
    masterPipeline.executePipeline()
}
