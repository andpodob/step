// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}





window.onload = function(){
    const popupBackground = document.getElementById("popup-background")

    const tiles = document.getElementsByClassName("tile");

    const openPopup = function(){
        popupBackground.style.display = "flex";
        const tileId = this.id;

        //cutting number from id
        const tileNumber = tileId.replace(/[^0-9]/g, '');
        
        //generating popup id
        const popupId = "popup-"+tileNumber;

        //showing popup
        const popup = document.getElementById(popupId);
        popup.style.display="flex";

        //aplying blure to content
        const content = document.getElementById("content");
        content.style.filter = "blur(4px)";
        content.style.webkitFilter = "blur(4px)";
    }

    window.onclick = function(event) {
        if (event.target == popupBackground) {
            //hiding the background
            popupBackground.style.display = "none";
            
            //hidding popup
            const popups = document.getElementsByClassName("popup");
            for(var i = 0; i < popups.length; i++){
                popups[i].style.display = "none";
            }
            
            //restoring blure property of content
            const content = document.getElementById("content");
            content.style.filter = "blur(0px)";
            content.style.webkitFilter = "blur(0px)";
        }
    }

    for(var i = 0; i < tiles.length; i++){
        tiles[i].addEventListener("click", openPopup, false);
    }
}