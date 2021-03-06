grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenRepo "https://github.com/mercadopago/sdk-java/raw/master/snapshots"
        // uncomment these to enable remote dependency resolution from public Maven repositories
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        runtime
        // runtime 'mysql:mysql-connector-java:5.1.16'
        runtime 'commons-codec:commons-codec:1.9'
        runtime 'org.codehaus.jettison:jettison:1.3.3'
        runtime 'com.mercadopago:sdk:0.0.1'

        // excel
        runtime 'net.sourceforge.jexcelapi:jxl:2.6.12'

    }


    plugins {
        runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.7.1"
        runtime ":resources:1.1.6"
        compile ":rabbitmq:1.0.0"


        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:$grailsVersion"
    }

    grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
            xml: ['text/xml', 'application/xml'],
            text: 'text-plain',
            js: 'text/javascript',
            rss: 'application/rss+xml',
            atom: 'application/atom+xml',
            css: 'text/css',
            csv: 'text/csv',
            pdf: 'application/pdf',
            rtf: 'application/rtf',
            excel: 'application/vnd.ms-excel',
            ods: 'application/vnd.oasis.opendocument.spreadsheet',
            all: '*/*',
            json: ['application/json','text/json'],
            form: 'application/x-www-form-urlencoded',
            multipartForm: 'multipart/form-data'
    ]
}
