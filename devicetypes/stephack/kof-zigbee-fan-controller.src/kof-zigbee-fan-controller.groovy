/**
 *  King Of Fans Zigbee Fan Controller
 *
 *  Copyright 2017 Stephan Hackett
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
metadata {
	definition (name: "KOF Zigbee Fan Controller", namespace: "stephack", author: "Stephan Hackett") {
		capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"       
        capability "Light"
        capability "Sensor" 
        capability "Polling"
        capability "Health Check"
   
        command "fanOne"
        command "fanTwo"
        command "fanThree"
        command "fanFour"
        command "fanAuto"
        command "lightOn"
        command "lightOff"
        command "lightLevel"
        
        attribute "fanMode", "string"
        attribute "lightBrightness", "number"    
        attribute "lastFanMode", "string"        
      
	fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0202", outClusters: "0003, 0019", model: "HDC52EastwindFan"
    }
    
    preferences {
    	page(name: "childToRebuild", title: "This does not display on DTH preference page")
            section("section") {
            	//input(name: "label1", type: "text", title: "Enter new label for Fan Speed Low", description: "Changes the default name")
            	//input(name: "label2", type: "text", title: "Enter new label for Fan Speed Med")
            	//input(name: "label3", type: "text", title: "Enter new label for Fan Speed Med-Hi")
            	input(name: "clearChildren", type: "bool", title: "Delete all child devices?")                      
       }
    }
    
    tiles(scale: 2) {    	
	multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4) {        	
		tileAttribute ("fanMode", key: "PRIMARY_CONTROL") {			
			attributeState "fanFour", label:"HIGH", action:"off", icon:"st.Lighting.light24", backgroundColor:"#33ff00", nextState: "turningOff"
			attributeState "fanThree", label:"MED-HI", action:"off", icon:"st.Lighting.light24", backgroundColor:"#33cc00", nextState: "turningOff"
			attributeState "fanTwo", label:"MED", action:"off", icon:"st.Lighting.light24", backgroundColor:"#339900", nextState: "turningOff"
			attributeState "fanOne", label:"LOW", action:"off", icon:"st.Lighting.light24", backgroundColor:"#336600", nextState: "turningOff"
			attributeState "fanAuto", label:"BREEZE", action:"off", icon:"st.Lighting.light24", backgroundColor:"#00A0DC", nextState: "turningOff"
        	attributeState "fanOff", label:"FAN OFF", action:"on", icon:"st.Lighting.light24", backgroundColor:"#ffffff", nextState: "turningOn"
			attributeState "turningOn", action:"on", label:"TURNING ON", icon:"st.Lighting.light24", backgroundColor:"#2179b8", nextState: "turningOn"
			attributeState "turningOff", action:"off", label:"TURNING OFF", icon:"st.Lighting.light24", backgroundColor:"#2179b8", nextState: "turningOff"
        }  
        tileAttribute ("lightBrightness", key: "SLIDER_CONTROL") {
			attributeState "lightBrightness", action:"lightLevel"
		}         
	} 
	standardTile("fanLight", "lightMode", width: 4, height: 1, decoration: "") {
		state "Off", label:"Light Off", action: "lightOn", icon:"https://raw.githubusercontent.com/dcoffing/SmartThingsPublic/master/devicetypes/dcoffing/hampton-bay-universal-ceiling-fan-light-controller.src/Light175xfinal.png", backgroundColor: "#ffffff", nextState:"turningOn"
		state "On", label:"Light On", action: "lightOff", icon:"https://raw.githubusercontent.com/dcoffing/SmartThingsPublic/master/devicetypes/dcoffing/hampton-bay-universal-ceiling-fan-light-controller.src/Light175xfinal.png", backgroundColor: "#79b821", nextState:"turningOff"
		state "turningOn",  label:"Turning On", icon:"https://raw.githubusercontent.com/dcoffing/SmartThingsPublic/master/devicetypes/dcoffing/hampton-bay-universal-ceiling-fan-light-controller.src/Light175xfinal.png", backgroundColor:"#79b821", nextState: "turningOff"
		state "turningOff", label:"Turning Off",icon:"https://raw.githubusercontent.com/dcoffing/SmartThingsPublic/master/devicetypes/dcoffing/hampton-bay-universal-ceiling-fan-light-controller.src/Light175xfinal.png", backgroundColor:"#ffffff", nextState: "turningOn" 
	}  	
	standardTile("fanOff", "fanMode", width:2, height:2) {
        state "default", label:"FAN OFF",action: "off", icon:"https://raw.githubusercontent.com/dcoffing/SmartThingsPublic/master/devicetypes/dcoffing/hampton-bay-universal-ceiling-fan-light-controller.src/OnOff175xfinal.png", backgroundColor: "#ffffff", nextState: "turningOff"
		state "fanOff", label:"FAN OFF", action: "off", icon:"https://raw.githubusercontent.com/dcoffing/SmartThingsPublic/master/devicetypes/dcoffing/hampton-bay-universal-ceiling-fan-light-controller.src/OnOff175xfinal.png", backgroundColor: "#79b821", nextState: "turningOff"
        state "turningOff", label:"ADJUST", action: "", icon:"st.Home.home30", backgroundColor: "#2179b8"
    }  
   	standardTile("refresh", "refresh", decoration: "flat", width: 2, height: 2) {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
  	
    valueTile("blank", "blank"){ }
    
    childDeviceTiles("fanSpeeds")
    //childDeviceTile("fanM", "fanMode1", width: 6, height: 1)
    
        
	main(["switch"])
        
	details(["switch", "fanSpeeds", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
		log.debug "Parse description $description"           
        def event = zigbee.getEvent(description)
    	if (event) {
        	log.info "ENTER LIGHT"
            //Don't know what this part of the parse is for
        	if (event.name == "power") {
            	log.info "LIGHT - SPECIAL POWER"
                event.value = (event.value as Integer) / 10                
                sendEvent(event)
        	}
        	else {
            	log.info "LIGHT - SEND EVENT"
                log.info event
            	//find light child device
                def childDevice = getChildDevices()?.find {
        				it.device.deviceNetworkId == "${device.deviceNetworkId}-6" 
                }
                //send light events to light child device and update lightBrightness attribute
                childDevice.sendEvent(event)
                if(event.value != "on" && event.value != "off") sendEvent(name: "lightBrightness", value: event.value)
        	}        
    	}
		else {
        	log.info "ENTER FAN"
			def map = [:]
			if (description?.startsWith("read attr -")) {
            	log.info "FAN - READ"
				def descMap = zigbee.parseDescriptionAsMap(description)
				// Fan Control Cluster Attribute Read Response               
                log.info descMap
				if (descMap.cluster == "0202" && descMap.attrId == "0000") {
                	log.info "FAN - READ - MODE"
					map.name = "fanMode"
					map.value = getFanModeMap()[descMap.value]
				} 
			}	// End of Read Attribute Response
			def result = null          
            if (map) {
            	log.info "FAN - CREATE EVENT"                
				result = createEvent(map)                
			}
			log.debug "Parse returned $map"          	          
            log.info "EXIT FAN and PARSE"
			return result 
    	}
        
        log.info "EXIT PARSE"        
}

def getFanModeMap() { 
	[
    "00":"fanOff",
    "01":"fanOne",
    "02":"fanTwo",
    "03":"fanThree",
	"04":"fanFour",
	"06":"fanAuto"
	]
}

def getFanName() { 
	[  
    1:"LOW",
    2:"MEDIUM",
    3:"MEDIUM HIGH",
	4:"HIGH",
    5:"BREEZE",
    6:"LAMP"   
	]
}

def getFanNameAbbr() { 
	[  
    1:"LOW",
    2:"MED",
    3:"MED-HI",
	4:"HI",
    5:"BREEZE",
    6:"LAMP"   
	]
}

def installed() {
	log.info "INSTALLED RUN"
	initialize()	
}

def updated() {
	log.info "UPADATE RUN"
	initialize()
}

def initialize() {
	log.info "START INITIALIZE"
	//app.label==app.name?app.updateLabel(defaultLabel()):app.updateLabel(app.label)
	// Save the device label for updates by updated()
	state.oldLabel = device.label
	// Add child devices for all five fan modes   
    if(clearChildren) {
    	deleteChildren()        
    }
    else {
		createFanChild()
    	createLightChild()
    }
    log.info "END INITIALIZE"
}

def createFanChild() {
	for(i in 1..5) {
   		log.info "create FanChild Loop ${i}"
    	def childDevice = getChildDevices()?.find {
        	it.device.deviceNetworkId == "${device.deviceNetworkId}-${i}"
    	}                 
        if (!childDevice) {        
        	childDevice = addChildDevice("KOF Zigbee Fan Controller - Fan Speed Child Device", "${device.deviceNetworkId}-${i}", null,[completedSetup: true, label: "${device.displayName} ${getFanName()[i]}", isComponent: true, componentName: "fanMode${i}", componentLabel: "Fan Speed ${getFanNameAbbr()[i]}"])
        	response(refresh() + configure())
           	log.info "Creating child fan mode ${childDevice}"  
		}
       	else {
        	log.info "Child already exists"          
		}
	}
}

def createLightChild() {
	log.info "create LightChild"
	def childDevice = getChildDevices()?.find {
        	it.device.deviceNetworkId == "${device.deviceNetworkId}-6"
    }
    if (!childDevice) {  
		childDevice = addChildDevice("KOF Zigbee Fan Controller - Light Child Device", "${device.deviceNetworkId}-6", null,[completedSetup: true, label: "${device.displayName} ${getFanName()[6]}", isComponent: true, componentName: "fanLight", componentLabel: "${getFanNameAbbr()[6]}"])
        response(refresh() + configure())
        log.info "Creating child light ${childDevice}" 
    }
	else {
        log.info "Child already exists"          
	}	
}

def deleteChildren() {
	log.info "delete  all Child"
    for (i in 1..6) {
    	log.info "delete children Loop ${i}"
    	def childDevice = getChildDevices()?.find {
        	it.device.deviceNetworkId == "${device.deviceNetworkId}-${i}"
    	}
		if (childDevice) {        
    		childDevice = deleteChildDevice("${device.deviceNetworkId}-${i}")
        	//childDevice.refresh()
        	response(refresh() + configure())
        	log.info "Deleting child ${childDevice}"
        }
	}
}

def configure() {
	log.info "Configuring Reporting and Bindings."
	def cmd = 
    [
	  //Set long poll interval
	  "raw 0x0020 {11 00 02 02 00 00 00}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  //Bindings for Fan Control
      "zdo bind 0x${device.deviceNetworkId} 1 1 0x006 {${device.zigbeeId}} {}", "delay 100",
      "zdo bind 0x${device.deviceNetworkId} 1 1 0x008 {${device.zigbeeId}} {}", "delay 100",
	  "zdo bind 0x${device.deviceNetworkId} 1 1 0x202 {${device.zigbeeId}} {}", "delay 100",
	  //Fan Control - Configure Report
      "zcl global send-me-a-report 0x006 0 0x10 1 300 {}", "delay 100",
       "send 0x${device.deviceNetworkId} 1 1", "delay 100",
      "zcl global send-me-a-report 0x008 0 0x20 1 300 {}", "delay 100",
       "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  "zcl global send-me-a-report 0x202 0 0x30 1 300 {}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100",
	  //Update values
      "st rattr 0x${device.deviceNetworkId} 1 0x006 0", "delay 100",
      "st rattr 0x${device.deviceNetworkId} 1 0x008 0", "delay 100",
	  "st rattr 0x${device.deviceNetworkId} 1 0x202 0", "delay 100",
	 //Set long poll interval
	  "raw 0x0020 {11 00 02 1C 00 00 00}", "delay 100",
	  "send 0x${device.deviceNetworkId} 1 1", "delay 100"
	]
    return cmd + refresh()
}

def on() {
	log.info "FAN ON RUN"
    //resumes previous fanspeed
	def lastFan =  device.currentValue("lastFanMode")
	return "$lastFan"()
    
}

def off() {
	log.info "FAN OFF RUN"
    //save fanspeed before turning off so it can be resumed when turned back on
	def fanNow = device.currentValue("fanMode")
    //do not save lastfanmode if fan is currently off
    if(fanNow != "fanOff") sendEvent("name":"lastFanMode", "value":fanNow)  
	log.info "${device.deviceNetworkId}"
	def cmds=[
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {00}"
    ]
    log.info "Turning fan Off"    
    return cmds
}

def lightOn()  {
	log.info "LIGHTON RUN"
	zigbee.on()
}

def lightOff() {
	log.info "LIGHTOFF RUN"
	zigbee.off()
}

def lightLevel(val) {
	log.info "LIGHTLEVEL RUN"    
    zigbee.setLevel(val) + (val?.toInteger() > 0 ? zigbee.on() : []) 
}

def fanAuto() {
	log.info "FAN AUTO RUN"
    def cmds=[
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {06}"
    ]
    log.info "Turning On Breeze mode"    
    return cmds    
}

def fanOne() {
	log.info "FAN 1 RUN"
    def cmds=[
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {01}"
    ]
    log.info "Setting fan speed to One"
    return cmds    
}

def fanTwo() { 
	log.info "FAN 2 RUN"
    def cmds=[
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {02}"
    ]
    log.info "Setting fan speed to Two"
    return cmds    
}

def fanThree() {
	log.info "FAN 3 RUN"
    def cmds=[
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {03}"
    ]
    log.info "Setting fan speed to Three"
    return cmds    
}

def fanFour() {
	log.info "FAN 4 RUN"
    def cmds=[
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {04}"
    ]
    log.info "Setting fan speed to Four"    
    return cmds    
}

def ping() {
	log.info "PING RUN"
    return zigbee.onOffRefresh()
}

def refresh() {
	log.info "REFRESH RUN"
    zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.readAttribute(0x0202, 0x0000)
}