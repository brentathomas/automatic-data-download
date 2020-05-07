# automatic-data-download
A utility to download trip data from the Automatic dashboard at https://dashboard.automatic.com/.

# Usage

## Build
Requires Java 8+ and maven.

Build using 'mvn clean package'.

## Configure

In src/main/scripts, modify 'download.properties' as appropriate. The defaults mostly work,
but you must add an access token value.



## Run

Change directory to src/main/scripts.

On Windows:
* Open cmd window.
* cd to src/main/scripts.
* Run the download.bat file.


On Mac:
* Open Terminal window.
* cd to src/main/scripts.
* Copy the command from the download.bat file.
* Paste command into Terminal window and hit enter.

Output files will be written to target/output.

