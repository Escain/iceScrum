iceScrum 7.57 — Apache Grails 7 fork
====================================

Fork of iceScrum 7.55 (Kagilum, AGPL — see license.txt) ported from Grails
2.5.6 / Java 8 to Apache Grails 7.1.2 / Java 17 / PostgreSQL 17, because the
original stack no longer runs on current Debian and its Maven repository
(repo.icescrum.org) is gone.

iceScrum-core is consumed as a source plugin from the sibling repository
../iceScrum-core (see settings.gradle) — clone both side by side and keep them
on matching branches.


Building
--------

    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew bootWar -x test

Produces build/libs/icescrum-7.57.war.

JAVA_HOME must point at a JDK 17 *JDK*: the build declares a Java 17 toolchain,
and a JRE (or a JDK 21 without the matching toolchain) fails with "does not
provide JAVA_COMPILER".

Running from source for development (H2 in memory by default):

    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew bootRun

Point development at a real database with ICESCRUM_DB_URL (plus optional
ICESCRUM_DB_USERNAME / ICESCRUM_DB_PASSWORD / ICESCRUM_DB_DBCREATE); see
grails-app/conf/application.groovy. In production those same variables override
the default jdbc:postgresql://localhost:5432/icescrum.


Deploying: unzip the WAR, do not run `java -jar`
------------------------------------------------

The WAR is an executable Spring Boot war, so `java -jar icescrum.war` works —
but it is markedly slower in normal use, and the difference is not in the boot
time you see in the log.

A Boot WAR keeps its ~200 dependencies as *nested jars* inside the archive.
Nothing can seek directly to a class in a nested jar: every miss scans the
enclosing archive entry by entry. Grails loads most classes lazily, on first
use of each feature, so the cost is paid all day long by whoever first opens
the backlog, the sprint plan, a report... Measured here: ~7 s for a first
request against a freshly booted server, and it recurs after every restart.
With a nightly reboot, users re-paid it every morning.

Unzipping the WAR and launching the extracted directory removes the nested-jar
lookups entirely — the same first request drops to 0.2–0.7 s (warm requests are
~20 ms either way), and boot goes from ~22 s to ~18 s. Disk cost is one extra
copy of the ~165 MB archive.

    cd /home/icescrum/icescrum
    sudo -u icescrum unzip -q icescrum-7.57.war -d app-new
    rm -rf app-old
    mv app app-old && mv app-new app     # instant swap; app-old is the rollback
    systemctl restart icescrum

Extract into a *new* directory and swap it in, rather than unzipping over the
live one: a failed or interrupted extraction would otherwise leave a
half-written deployment with nothing to roll back to. To roll back:

    mv app app-broken && mv app-old app && systemctl restart icescrum

The matching systemd ExecStart (note: WarLauncher, and the *directory* on the
classpath — not the .war):

    WorkingDirectory=/home/icescrum/icescrum
    ExecStart=/usr/bin/java -Xmx1024M -Dgrails.env=production \
        -cp /home/icescrum/icescrum/app \
        org.springframework.boot.loader.launch.WarLauncher \
        host=0.0.0.0 port=8080 context=/

-Dgrails.env=production is mandatory in this launch mode. Grails infers "war
deployment" from how it was started; launched via -cp it does not, and silently
falls back to the development environment — which means an in-memory H2
database instead of PostgreSQL, with no error in the log.

Do not use `java -Djarmode=tools -jar icescrum.war extract`. It produces a
CLI-style layout (classes at the jar root, dependencies via a Class-Path
manifest, no WEB-INF/classes) in which the app boots but registers no URL
mappings: / returns 500 and every page 404. Plain unzip preserves the
WEB-INF/classes + WEB-INF/lib layout WarLauncher expects.


Reverse proxy
-------------

Set the body limit on the proxy to match the application's upload limit
(grails.controllers.upload.* in grails-app/conf/application.yml, 100 MB here),
otherwise large attachments fail at the proxy with 413:

    client_max_body_size 100m;
