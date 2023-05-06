First, generate game jar using standard methods (currently packr expects the jar to be in the "Eudaemon_jar2" file)
Then,

to generate windows executable, run (from the root directory):
java -jar packr\packr-all-4.0.0.jar packr\windows-config.json

if you want to rerun the command, you have to delete the "Eudaemon (Windows)" folder

to generate mac executable, run (from the root directory):
java -jar packr/packr-all-4.0.0.jar packr/mac-config.json

if you want to rerun the command, you have to delete the "Eudaemon (Mac)" folder