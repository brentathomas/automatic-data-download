# automatic-data-download
A utility to download trip data from the Automatic dashboard at https://dashboard.automatic.com/.

Automatic has announced that they are shutting down on May 28, 2020. You have until then
to get your data out!
https://automatic.com/customerfaq

I have never been able to export all my data from the Automatic dashboard successfully, so
I wrote this little app to pull the data out and save it as json files.

I will also be adding a transformation to convert the json files into a CSV format
to make it easier to import into spreadsheets for additional analysis and charting.


# Usage

## Build
Requires Java 8+ and maven.

Build using 'mvn clean package'.

## Configure

In src/main/scripts, modify 'download.properties' as appropriate. The defaults mostly work,
but you must add an access token value.

The bearer token can be retrieved after login on the Automatic dashboard.
* Login to Automatic dashboard at https://dashboard.automatic.com/
* Open Chrome Developer Tools.
* Go to Application tab.
* Under Local Storage, you'll find a value for accessToken.
* Copy and paste that value in the download.properties file under auth.bearer.token value.

Alternatively, from the dashboard page, execute this in the url box. When you paste it,
it may drop the javascript prefix, so you'll need to add it back manually.
* javascript:alert(localStorage.getItem('accessToken'));


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

