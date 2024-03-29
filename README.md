bladenightapp-server
====================

The server side of the Bladenight application.


This software is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this software.  If not, see <http://www.gnu.org/licenses/>.

Deploy App
    in Terminal run
        gradle clean
        gradle jar
        gradle shadowJar 
    this will create runnable file in build/libs/bladenightapp-server-0.6-SNAPSHOT-all.jar
    create PKCS12 file .p12 with create-certificate.sh

issues
    Execution failed for task ':shadowJar'. 
        > Unsupported class file major version 60
    move through all steps for deploy app. Important is 'gradle clean' on startup


Ideas for Applicationupdates
- Load Images and links from Server
- check gaps for police in train
- don't allow long time differences on train length/ from to head and train - head etas
- calculate time by trainlength
- seems using wrong speeddatas to update etas

Special Config to control parts of remote App in imagesAndLinks.json
```json
[
  {
    "key": "mainSponsorLogo", <--set default 1st logo-->
    "image": "https://bladenight.app/main_sponsor.png",
    "link": "",
    "text": ""
  },
  {
    "key": "secondLogo", <--set default 2nd logo-->
    "image": "https://bladenight.app/skatemunich.png",
    "link": "https://skatemunich.de",
    "text": ""
  },
  {
    "key": "startPoint",
    "image": "",
     "link": "https://bladenight-muenchen.de/blade-guards/#anmelden", <--set default link if you tap on startpoint at homepage-->
     "text": "Schwanthalerhöhe\nMünchen\n(Deutsches Verkehrsmuseum)\nBladeguards gesucht" <--set default text-->
  },
  {
    "key": "bladeguardLink",
    "image": "",
    "link": "https://bladenight-muenchen.de/blade-guards/#anmelden", <--set default link for bladeguard-->
    "text": "Bladeguard"
  },
  {
    "key": "defaultLatitude",
    "image": "",
    "link": "",
    "text": "48.13250913196827" <--set default app latitude-->
  },
  {
    "key": "defaultLongitude", 
    "image": "",
    "link": "",
    "text": "11.543837661522703" <--set default app longitude-->
  },
  {
    "key": "androidPlayStoreLink",
    "image": "",
    "link": "https://play.google.com/store/apps/details?id=de.bladenight.bladenight_app_flutter",
    "text": "BladenightApp Android"
  },
  {
    "key": "iOSAppStoreLink",
    "image": "",
    "link": "https://apps.apple.com/de/app/bladenight-vorab/id1629988473",
    "text": "BladenightApp iOS"
  },
  {
    "key": "liveMapLink",
    "image": "",
    "link": "https://bladenight-muenchen.de/bladenight-live-karte/",
    "text": "Live Karte"
  },
  {
    "key": "openStreetMap",
    "image": "", <--no function-->
    "link": "aHR0cHM6Ly97c30udGlsZS5vcGVuc3RyZWV0bWFwLm9yZy97en0ve3h9L3t5fS5wbmc= ", <-- base64encoded link to osm tileserver remove mark-->
    "text": "" <--on force openstreetmap on / off - force openstreetmap off / "" remove link and let the user choose-->
  }

]
```

