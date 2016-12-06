# Gradle Wrapper Initializer([中文说明](README_zh.md))
When build a project with gradle wrapper, it would try to download a gradle distribution from gradle.org . But it seems to be very slow. So, I wander whether could I use a previously downloaded gradle zip file? That's why I write this tool. I hope it will give a convenience.
## Use Instructions
### download the lastest version of this tool
```
    curl -o gradle-wrapper-initializer.tar.bz2 https://github.com/sauntor/gradle-wrapper-initializer/releases/download/v1.0.0/gradle-initializer-1.0.tar.bz2
    ```
    
    Or
    
```
    curl -o gradle-wrapper-initializer.tar.bz2 https://github.com/sauntor/gradle-wrapper-initializer/releases/download/v1.0.0/gradle-initializer-with-gradles-1.0.tar.bz2
    ```
    
### unpack the tool
### copy the gradle distribution zips you downloaded previous to $INITIALIZER_DIR/distrubtions
### or just run the initializer scripts with -d or --distributions-dir=
##### On Linux

```
    ./initializer.sh -d some_path_to_gradle_zips
    ```
    
    Or
    
```
    ./initializer.sh --distributions-dir=/some/path/to/gradle/zips
```
##### On Windows
```
    .\initializer.bat -d Driver:\path\to\gradle\zips
    ```
    Or
    
```
    .\initializer.bat -distributions-dir=Driver:\path\to\gradle\zips
    ```

