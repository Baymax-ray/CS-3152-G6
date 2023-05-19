First, generate game jar using standard methods 
(currently the config expects the jar to be in the "Eudaemon_jar" folder, 
which is where "Build Artifacts" should put the jar)

Then,

\
to generate windows executable, run (from the root directory "Eudaemon"):

`java -jar packr\packr-all-4.0.0.jar packr\windows-config.json`

if you want to rerun the command, you have to delete the "packr/out/Eudaemon (Windows)" folder.


\
to generate mac executable, run (from the root directory "Eudaemon"):

`java -jar packr/packr-all-4.0.0.jar packr/mac-config.json`

if you want to rerun the command, you have to delete the "packr/out/Eudaemon (Mac)" folder.