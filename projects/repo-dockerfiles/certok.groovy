freeStyleJob('certok') {
    displayName('certok')
    description('Build Dockerfiles in genuinetools/certok.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/certok')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/certok', 'Docker Hub: jess/certok', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/certok', 'Docker Hub: jessfraz/certok', 'notepad.png')
            link('https://r.j3ss.co/repo/certok/tags', 'Registry: r.j3ss.co/certok', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/certok.git')
            }
            branches('*/master', '*/tags/*')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/certok:latest .')
        shell('docker tag r.j3ss.co/certok:latest jess/certok:latest')
        shell('docker tag r.j3ss.co/certok:latest jessfraz/certok:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/certok:latest')
        shell('docker push --disable-content-trust=false jess/certok:latest')
        shell('docker push --disable-content-trust=false jessfraz/certok:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/certok:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/certok:$tag || true; docker tag r.j3ss.co/certok:$tag jess/certok:$tag || true; docker push --disable-content-trust=false jess/certok:$tag || true; done')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
        retryBuild {
            retryLimit(2)
            fixedDelay(15)
        }

        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }

        wsCleanup()
    }
}
