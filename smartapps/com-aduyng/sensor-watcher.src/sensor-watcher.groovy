/**
 *  Notify Me
 *
 *  Copyright 2017 Duy Nguyen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Sensor Watcher",
    namespace: "com.aduyng",
    author: "Duy Nguyen <aduyng@gmail.com>",
    description: "Send push notification when a sensor stays in one state for certain time",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn@3x.png")


preferences {
	section("Sensor") {
		input("sensor", "capability.contactSensor", title: "Which sensor?", required: true)
        input(name: "sensorState", type: "enum", title: "What state?", options: ["open", "closed"])
		input(name: "durationInMinutes", type: "number", title: "For how long (minutes)?", required: true)
	}
	//section("Notification") {
	//	input("recipients", "contact", title: "Send notifications to", required: true)
	//}

}

def installed() {
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

unschedule();
	unsubscribe()
	initialize()
}

def initialize() {
	def sensorValue = sensor.currentValue("contact")
    state.sensorStateToSchedule = sensorState == 1 || sensorState == "open" ? "open" : "closed"
    
	log.debug "current sensor value $sensorValue, sensorState = $sensorState"
    log.debug "sensorState: $state.sensorStateToSchedule"
	setSchedule(sensor.currentValue("contact"))
	subscribe(sensor, "contact", switchChangeHandler)
}

def switchChangeHandler(evt){
    setSchedule(evt.value)
}

def setSchedule(value){
	log.debug "setSchedule $value == $state.sensorStateToSchedule"
	if( value == state.sensorStateToSchedule ){
    	startTimer();
    } else {
    	stopTimer();
    }
}

def startTimer(){
    log.debug "start the timer"
    runIn(60*durationInMinutes, sendNotification)
}

def stopTimer(){
    log.debug "stop the timer"
    unschedule();
}

def sendNotification(){
    def sensorValue = sensor.currentValue("contact");
    def message = "$sensor.displayName is $sensorValue for more than $durationInMinutes minute(s)"
  	sendPush(message)	
  	log.debug "sent: $message"
}