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
const LoginState = {
    LOGGED_OUT: 'logged_out',
    LOGGED_IN_NICKNAME_SET: 'logged_in_nickname_set',
    LOGGED_IN_NICKNAME_NOT_SET: 'logged_in_nickname_not_set'
}

class AuthData {
    constructor(loginState, userId, nickname, loginUrl, logoutUrl){
        this.loginState = loginState;
        this.userId = userId;
        this.nickname = nickname;
        this.loginUrl = loginUrl;
        this.logoutUrl = logoutUrl;
    }
}



const COMMENTS_SIZE = 5
const INITIAL_COMMENTS_SIZE = 10
const MAX_COMMENTS_SIZE = 20

var newestSent;
var oldestSent;
var commentsArray = [];

/**
 * 
 * @param {*} timestamp timestamp to be converted to date in text format
 * 
 */
function getLocalTime(timestamp){
    return new Date(timestamp); 
}

/**
 * 
 * @param {*} commentJson json comment object with properties {'author', 'timestamp', 'comment'}
 * 
 * Creates comment DOM element based passed json comment object.
 *  
 */

function createCommentElement(commentJson){
    const commentContainer = document.createElement('div');
    commentContainer.classList.add("comment-container");
    
    const author = document.createElement("h5");
    const date = document.createElement("h7");
    const comment = document.createElement("p");

    const authorText = document.createTextNode(commentJson.author); 
    const dateText = document.createTextNode(getLocalTime(commentJson.timestamp)); 
    const commentText = document.createTextNode(commentJson.comment);

    author.appendChild(authorText);
    date.appendChild(dateText);
    comment.appendChild(commentText);

    commentContainer.appendChild(author); 
    commentContainer.appendChild(date);
    commentContainer.appendChild(comment);

    return commentContainer;
}

/**
 * 
 * @param {*} parent parent DOM element that all child elements should be removed
 * 
 * Removes all child elements of passed DOM parent element 
 */

function removeAllChildren(parent){
    while (parent.firstChild) {
        parent.removeChild(parent.lastChild);
    }
}

/**
 * 
 * @param {*} comments list of comments that should be displayed
 * 
 * This function is used to initiate or refresh comments content, it erases current content and fills comments scroll view
 * with passed comments.
 * 
 */

function populateCommentsList(comments){
    const commentList = document.getElementById('comments');
    removeAllChildren(commentList);
    commentsArray = comments;
    let entry = null;
    for(let i in comments){
        entry = document.createElement('li');
        comment = createCommentElement(comments[i]);
        entry.appendChild(comment);
        commentList.insertBefore(entry, commentList.firstChild);

    }
}
/**
 * 
 * @param {*} comments array of comment objects to be added to commentsArray and to be displayed
 * @param {*} side indicated if comments should be added on the bottom of array(dispalyed on the bottom of scroll view) or on the top
 * 
 * Handles adding comments to 'comments' DOM element and reflects this changes in commentsArray
 * which consist of abstract comment objects, facilitates testing and obtaining information about 
 * current state. 
 * 
 * Usage:
 * Pass the comments to be added and side 'top' or 'bottom' to indicate on what side comments should be added
 */
function addToCommentsList(comments, side){
    const commentList = document.getElementById('comments');
    let entry = null;
    for(let i in comments){
        entry = document.createElement('li');
        comment = createCommentElement(comments[i]);
        entry.appendChild(comment);
        if(commentList.childNodes.length >= MAX_COMMENTS_SIZE ){
            switch (side){
                case "top":
                    commentList.insertBefore(entry, commentList.firstChild);
                    commentList.removeChild(commentList.lastChild);
                    commentsArray.push(comments[i]);
                    commentsArray.shift();
                    break;
                case "bottom":
                    commentList.appendChild(entry);
                    commentList.removeChild(commentList.firstChild);
                    commentsArray.unshift(comments[i]);
                    commentsArray.pop();
                    break;
            }
            
        }else{
            switch (side){
                case "top":
                    commentList.insertBefore(entry, commentList.firstChild);
                    commentsArray.push(comments[i]);
                    break;
                case "bottom":
                    commentList.appendChild(entry);
                    commentsArray.unshift(comments[i]);
                    break;
            }
        }
    }
}


/**
 * Fetches INITIAL_COMMENTS_SIZE most recent comments and displays them as content.
 */
function getNewestComments(){
    fetch(`/data?comments=newest&size=${INITIAL_COMMENTS_SIZE}`)
    .then(response => response.json())
    .then((comments) => {
        populateCommentsList(comments);
    });
}


/**
 * Fetches next COMMENTS_SIZE comments according to oldest currently displayed comment
 */
function nextComments(){
    fetch(`/data?comments=next&oldest=${commentsArray[0].timestamp}&newest=${commentsArray[commentsArray.length-1].timestamp}&size=${COMMENTS_SIZE}`)
    .then(response => response.json())
    .then((comments) => {
        if(comments.length > 0){
            addToCommentsList(comments.reverse(), "bottom");
        }
    });
}

/**
 * Fetches previous COMMENTS_SIZE comments according to newest currently displayed comment
 */
function prevComments(){
    fetch(`/data?comments=prev&oldest=${commentsArray[0].timestamp}&newest=${commentsArray[commentsArray.length-1].timestamp}&size=${COMMENTS_SIZE}`)
    .then(response => response.json())
    .then((comments) => {
        if(comments.length >  0){
            addToCommentsList(comments, "top");
        }
    });
}

function handleAuthData(authData){
    let loginState = LoginState.LOGGED_OUT;
    const nickname = authData.nickname;
    const userId = authData.userId;
    const loginUrl = authData.loginUrl;
    const logoutUrl = authData.logoutUrl;

    if(typeof authData.isUserLoggedIn !== 'undefined' && authData.isUserLoggedIn == false){
        loginState = LoginState.LOGGED_OUT;
    }else{
        if(typeof AuthData.nickname !== 'undefined'){
            loginState = LoginState.LOGGED_IN_NICKNAME_SET;
        }else{
            loginState = LoginState.LOGGED_IN_NICKNAME_NOT_SET;
        }
    }

    return new AuthData(loginState, userId, nickname, loginUrl, logoutUrl);
}

/**
 * Fetches auth data and then creates AuthData object that can be used to update frontend.
 * Function returns Promise to enable chaining it with functions that are using AuthData object
 * e.g manageAuthFrontEnd()
 */
function fetchAuthData(){
    return fetch('/auth')
    .then(response => response.json())
    .then((authData) => {
        return handleAuthData(authData); ;
    });
}

/**
 * Manages displying and hiding DOM elements connected to authentication based on AuthData that stores state of current user authentication
 */
function manageAuthFrontEnd(authData){
    const loginMessage = document.getElementById('login-message');
    const loginButton = document.getElementById('login-button');
    const setNicknameButton = document.getElementById('set-nickname-button');
    const changeNicknameButton = document.getElementById('change-nickname-button');
    const logoutButton = document.getElementById('logout-button');
    const commentForm = document.getElementById('comment-form');
    switch(authData.loginState){
        case LoginState.LOGGED_OUT:
            loginMessage.innerHTML = 'In order to leave a comment you need to authenticate.';
            loginButton.style.display = 'block';
            logoutButton.style.display = 'none';
            setNicknameButton.style.display = 'none';
            changeNicknameButton.style.display = 'none';
            commentForm.style.display = 'none';
            break;
        case LoginState.LOGGED_IN_NICKNAME_SET:
            loginMessage.innerHTML = `Hi ${authData.nickname}`;
            loginButton.style.display = 'none';
            logoutButton.style.display = 'block';
            setNicknameButton.style.display = 'none';
            changeNicknameButton.style.display = 'block';
            commentForm.style.display = 'block';
            break;
        case LoginState.LOGGED_IN_NICKNAME_NOT_SET:
            loginMessage.innerHTML = `Please set Your nick name in order to leave a comment.`;
            loginButton.style.display = 'none';
            logoutButton.style.display = 'block';
            setNicknameButton.style.display = 'block';
            changeNicknameButton.style.display = 'none';
            commentForm.style.display = 'none';
            break;
    }
}

window.onload = function(){
    getNewestComments();
    fetchAuthData().then((authData) => this.manageAuthFrontEnd(authData));
    const popupBackground = document.getElementById("popup-background")

    const tiles = document.getElementsByClassName("tile");

    const openPopup = function(){
        popupBackground.style.display = "flex";
        const tileId = this.id;

        const tileNumber = tileId.replace(/[^0-9]/g, '');
        
        const popupId = "popup-"+tileNumber;

        const popup = document.getElementById(popupId);
        popup.style.display="flex";
        const content = document.getElementById("content");
        content.style.filter = "blur(4px)";
        content.style.webkitFilter = "blur(4px)";
    }

    window.onclick = function(event) {
        if (event.target == popupBackground) {
            popupBackground.style.display = "none";
            const popups = document.getElementsByClassName("popup");
            for(var i = 0; i < popups.length; i++){
                popups[i].style.display = "none";
            }
            
            const content = document.getElementById("content");
            content.style.filter = "blur(0px)";
            content.style.webkitFilter = "blur(0px)";
        }
    }

    for(var i = 0; i < tiles.length; i++){
        tiles[i].addEventListener("click", openPopup, false);
    }
}
