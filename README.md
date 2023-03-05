This repo is for self learn.  
I modified the original logging to Slf4j logging which helps me for debugging :)

SMPPSim official website tutorial:  
http://www.seleniumsoftware.com/user-guide.htm#quick

**REQUIRED SOFTWARE:**
1. java environment java 11 or later.
2. maven 2.x or 3.x

**QUICK START**

How to run?  
- Standalone:
```
java -jar smppsim.jar conf/logback.xml conf/smppsim.props
```
- Using docker  
```
docker run --rm --name smpp-simulator  -p 5555:5555 -p 8989:8989 armindoantunes/smpp-simulator
```

How to change credentials?
All credential information is in the conf/smppsim.props
