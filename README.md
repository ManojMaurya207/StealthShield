<img src="screenshots/app_logo.png" alt="Home Screen" height="300" width="300">
# StealthShield  
A comprehensive mobile theft security solution that protects your smartphone and helps you recover it in case of theft.

## 🚀 Features  
- **Fake Shutdown**: Simulates a power-off state while keeping the device operational in the background.  
- **Real-Time Location Tracking**: Continuously updates the device’s location every 5 seconds.  
- **Geofencing**: Add safe zones to avoid false alarms.  
- **SOS Alerts**: Notify emergency contacts with the device’s location via SMS.  
- **Customizable Safe Zones**: Add or remove safe zones with adjustable radius.  
- **Bottom Navigation**: Easily navigate through Home, Find My Device, and Profile sections.  

## 🛠️ Technologies Used  
- **Programming Language**: Kotlin  
- **UI Framework**: Jetpack Compose  
- **Database**: Firebase Firestore for real-time data storage  
- **APIs**: Google Maps SDK, Geocoding, and Places API  
- **Libraries**:  
  - Firebase Authentication  
  - Lottie for animations  
  - Coil for image loading  
  - Accompanist Navigation for animations  

## 📖 How It Works  
1. **Fake Shutdown**: When an unauthorized power-off attempt is made, the app captures a photo using the front camera and sends it along with the location to the owner's email.  
2. **Real-Time Tracking**: View the current location of your device on the integrated Google Map.  
3. **Geofencing**: Set safe zones, and the app will trigger alerts when the device leaves these zones.  
4. **Emergency Contacts**: Add trusted contacts who receive notifications and updates about the device's location.  


## ✨ Why StealthShield?  
StealthShield was developed out of a personal experience of losing a phone. The sense of helplessness during that time motivated me to create an app that ensures others don’t have to face similar challenges. StealthShield combines preventive and reactive measures for holistic security.  

## 🌟 Future Plans  
- Integration with theft-prone area alerts.  
- Intruder Captures.  

## 💻 Installation  
1. Clone the repository:  
   ```bash  
   git clone https://github.com/ManojMaurya207/StealthShield  
   ```  
2. Open the project in Android Studio.  
3. Add your Firebase configuration file (`google-services.json`) to the `app` folder.  
4. Build and run the app on your Android device.  

## 📜 License  
This project is licensed under the [MIT License](LICENSE).  

## 🙌 Contributions  
Contributions, issues, and feature requests are welcome! Feel free to fork the repo and submit a pull request.  
