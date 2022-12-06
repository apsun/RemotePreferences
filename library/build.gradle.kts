plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 1
        targetSdk = 33
    }
}

val sourcesJar = tasks.register("sourcesJar", Jar::class) {
    from(android.sourceSets["main"].java.getSourceFiles())
    archiveClassifier.set("sources")
}

val javadoc = tasks.register("javadoc", Javadoc::class) {
    source = android.sourceSets["main"].java.getSourceFiles()
    classpath += project.files(android.bootClasspath)
}

val javadocJar = tasks.register("javadocJar", Jar::class) {
    dependsOn(javadoc)
    from(javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        afterEvaluate {
            create<MavenPublication>("release") {
                from(components["release"])

                artifact(sourcesJar)
                artifact(javadocJar)

                groupId = "com.crossbowffs.remotepreferences"
                artifactId = "remotepreferences"
                version = "0.8"

                pom {
                    packaging = "aar"
                    name.set("RemotePreferences")
                    description.set("A drop-in solution for inter-app access to SharedPreferences on Android.")
                    url.set("https://github.com/apsun/RemotePreferences")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            name.set("Andrew Sun")
                            email.set("andrew@crossbowffs.com")
                        }
                    }
                    scm {
                        url.set(pom.url.get())
                        connection.set("scm:git:${url.get()}.git")
                        developerConnection.set("scm:git:${url.get()}.git")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = project.findProperty("ossrhUsername") as String?
                password = project.findProperty("ossrhPassword") as String?
            }
        }
    }
}

signing {
    useGpgCmd()
    afterEvaluate {
        sign(publishing.publications["release"])
    }
}
