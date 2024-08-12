let adInterval;
let adTimeout;
let adData;
const time=7 * 60 * 1000;

function fetchAd() {
    fetch(`${SERVER_IP}/p/pub/datos`) 
        .then(response => response.json())
        .then(data => {
            adData = data;
            displayAd();
        })
        .catch(error => console.error('Error fetching ad:', error));
}

function displayAd() {
    const adContainer = document.getElementById('adContainer');
    const adContent = document.getElementById('adContent');
    const closeAdButton = document.getElementById('closeAd');

    adContainer.style.display = 'flex'; 
    adContent.innerHTML = ''; 
    const url=`${SERVER_IP}/p/pub/archivo/${encodeURIComponent(adData.img_vid)}`;
    
    if (adData.img_vid.endsWith('.mp4')) {
        const videoElement = document.createElement('video');
        videoElement.src = url;
        videoElement.controls = false;
        videoElement.autoplay = true;
        adContent.appendChild(videoElement);

        videoElement.onended = waitAndCloseAd;
    } else {
        
        const imgElement = document.createElement('img');
        imgElement.src = url;
        adContent.appendChild(imgElement);

        adTimeout = setTimeout(closeAd, 60000);
        
    }

    setTimeout(() => {
        closeAdButton.style.display = 'block';
    }, 9000);
}

function waitAndCloseAd(){
    setTimeout(closeAd,5000);
}

function closeAd() {
    const adContainer = document.getElementById('adContainer');
    const closeAdButton = document.getElementById('closeAd');

    adContainer.style.display = 'none';
    closeAdButton.style.display = 'none';

    clearTimeout(adTimeout);
}

function initAdCycle() {
    fetchAd();  
    adInterval = setInterval(fetchAd, time);
}

document.getElementById('closeAd').addEventListener('click', closeAd);

setTimeout(initAdCycle,time/2);


