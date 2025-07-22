import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java")
    jacoco
    id("com.vanniktech.maven.publish") version "0.29.0"
}

jacoco {
    toolVersion = "0.8.13"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.1")

    testImplementation("org.apiguardian:apiguardian-api:1.1.2")
    testImplementation(platform("org.junit:junit-bom:5.11.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.hsqldb:hsqldb:2.7.3")
    testImplementation("org.postgresql:postgresql:42.7.3")
    testImplementation("org.testcontainers:postgresql:1.20.0")

    testImplementation("com.mysql:mysql-connector-j:9.0.0")
    testImplementation("org.testcontainers:mysql:1.20.0")

    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")
    testImplementation("org.testcontainers:mariadb:1.20.0")

    testImplementation("com.microsoft.sqlserver:mssql-jdbc:12.9.0.jre11-preview")
    testImplementation("org.testcontainers:mssqlserver:1.20.0")

    testImplementation("org.testcontainers:oracle-xe:1.20.0")
    testImplementation("com.oracle.database.jdbc:ojdbc11:23.4.0.24.05")
}

var exports = listOf(
    "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
    "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
    "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
    "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
)

tasks.withType<JavaCompile> {
    exports.forEach { v ->
        options.compilerArgs.add("--add-exports=${v}")
    }

    if (source().name == "compileTestJava")
        options.compilerArgs.add("--processor-path=${System.getProperty("java.io.tmpdir")}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Javadoc> {
    val opt = options as CoreJavadocOptions

    opt.addMultilineStringsOption("-add-exports").value = exports
    opt.addStringOption("-source", "21")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
    jvmArgs = listOf(
        "-noverify",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        csv.required.set(true)
        xml.required.set(true)
    }
}

mavenPublishing {

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("net.truej", "sql", "3.0.0-beta9")

    pom {
        name.set("TrueSql")
        description.set("The ultimate database connector for Java")
        url.set("https://github.com/pain64/true-sql")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
//                        id = "pain64"
//                        name = "Alex Gorodetskiy"
//                        email = "overln2@gmail.com"
//                    }
//                    developer {
//                        id = "dmitrygod"
//                        name = "Dmitry Gorodetskiy"
//                        email = "gorodeckiydimchik@gmail.com"
//                    }
                developer {
                    id = "pain64"
                    name = "Alex Gorodetskiy"
                    url = "https://github.com/pain64"
                }
                developer {
                    id = "dmitrygod"
                    name = "Dmitry Gorodetskiy"
                    url = "https://github.com/dmitrygod"
                }
            }
            scm {
                url = "https://github.com/pain64/true-sql"
                connection = "scm:git:git://github.com/pain64/true-sql.git"
                developerConnection = "scm:git:ssh://github.com/pain64/true-sql.git"
            }
        }
    }
}

//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            repositories {
//                maven {
//                    url = uri("https://central.sonatype.com/api/v1/publisher/deployments/download")
//
//                    println(project.properties["sonatypeUsername"].toString())
//                    println(project.properties["sonatypePassword"].toString())
//
//                    credentials {
//                        username = project.properties["sonatypeUsername"].toString()
//                        password = project.properties["sonatypePassword"].toString()
//                    }
//                }
//            }
//
//            pom {
//                name = "TrueSql"
//                description = "The ultimate database connector for Java"
//                url = "https://github.com/pain64/true-sql"
//                licenses {
//                    license {
//                        name = "The Apache License, Version 2.0"
//                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
//                    }
//                }
//                developers {
//                    developer {
//                        id = "pain64"
//                        name = "Alex Gorodetskiy"
//                        email = "overln2@gmail.com"
//                    }
//                    developer {
//                        id = "dmitrygod"
//                        name = "Dmitry Gorodetskiy"
//                        email = "gorodeckiydimchik@gmail.com"
//                    }
//                }
//                scm {
//                    url = "https://github.com/pain64/true-sql"
//                    connection = "scm:git:git://github.com/pain64/true-sql.git"
//                    developerConnection = "scm:git:ssh://github.com/pain64/true-sql.git"
//                }
//            }
//
//            groupId = "net.truej"
//            artifactId = "sql"
//            version = "0.0.1"
//
//            from(components["java"])
//        }
//    }
//}