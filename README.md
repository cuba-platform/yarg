YARG
====

[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Build Status](https://travis-ci.org/cuba-platform/yarg.svg?branch=master)](https://travis-ci.org/cuba-platform/yarg) [ ![Download](https://api.bintray.com/packages/cuba-platform/main/yarg/images/download.svg) ](https://bintray.com/cuba-platform/main/yarg/_latestVersion)

YARG is an open source reporting library for Java, developed by [Haulmont](http://www.haulmont.com/).

It is intended to be embedded into enterprise IT systems, thus it comes with no UI so that native UI of the target system can be used. Templates can be created in most common formats including MS Office (doc, docx, xls, xlsx, html, ftl, csv)  or a custom text format and filled with data loaded by sql, groovy or other means.

YARG is a mature and well-tested tool, already used in a number of Haulmont's solutions as part of [CUBA Platform](https://www.cuba-platform.com/YARG).

[Documentation](https://github.com/Haulmont/yarg/wiki)

### How to add dependency

Yarg versions are distributed using public Bintray Maven repository: http://dl.bintray.com/cuba-platform/main

You can find the complete list of versions here: https://bintray.com/cuba-platform/main/yarg

__Gradle:__
```
repositories {
    maven {
        url "http://dl.bintray.com/cuba-platform/main"
    }
}
...
dependencies {
    compile 'com.haulmont.yarg:yarg:2.0.1'
}
```

__Maven:__
```
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>bintray-cuba-platform-main</id>
        <name>bintray</name>
        <url>http://dl.bintray.com/cuba-platform/main</url>
    </repository>
</repositories>
...
<dependency>
    <groupId>com.haulmont.yarg</groupId>
    <artifactId>yarg</artifactId>
    <version>2.0.1</version>
    <type>pom</type>
</dependency>
```

### Samples

  * [Incomes](/core/modules/core/test/sample/incomes)
  * [Invoice](/core/modules/core/test/sample/invoice)
  * [Breakdown](/core/modules/core/test/sample/financedetails)


### Forums
* [Cuba Platform](https://www.cuba-platform.com/support/)
