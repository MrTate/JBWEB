# JBWEB
Java port of [BWEB](https://github.com/Cmccrave/BWEB)

Current head is forked from [this commit](https://github.com/Cmccrave/BWEB/commit/a65a57115ef4b6161a477b2e38382b8d0b5b6c50) (v1.14.2)

#Differences between BWEB and JBWEB

* Uses [JavaJPS](https://github.com/MrTate/JavaJPS) library instead of using JPS from BWEB.
* OnStart must be called with the already initialized `Game` and `BWEM`
* Path::jpsPath was removed instead of converted since it was unused, and we are using JavaJPS

## Usage

**Maven**

Add JitPack as a repository:
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Add JBWEB to your dependencies in `<dependencies></dependencies>`:
```
<dependency>
    <groupId>com.github.MrTate</groupId>
    <artifactId>JBWEB</artifactId>
    <version>v1.0.0</version>
</dependency>
```

**Gradle**

Add JitPack as a repository:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add JBWEB as a dependency:
```
dependencies {
        implementation 'com.github.MrTate:JBWEB:v1.0.0'
}
```