plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.crossbowffs.remotepreferences"
    compileSdk = 34

    defaultConfig {
        minSdk = 1
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        afterEvaluate {
            create<MavenPublication>("release") {
                from(components["release"])

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
