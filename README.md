# ESP-Nia - EEG Signal Processing for the OCZ Nia

This library provides the OCZ Nia implementation of the [ESP](http://mrstampy.github.io/ESP/) library classes. It is designed to be fast and efficient, using high performance Java libraries:

* [Disruptor](https://github.com/LMAX-Exchange/disruptor)
* [Javolution](http://javolution.org/)
* [Apache MINA](http://mina.apache.org/)
* [RxJava](https://github.com/Netflix/RxJava/)

## Release 2.1, 25-05-14

* Added Lab and Connection abstractions (explained below)</li>
* Implemented multi channel support</li>

## Release 2.0, 29-04-14

* Added [RxJava](https://github.com/Netflix/RxJava/) to the project
* Added ability to tune sample buffer size to more closely represent 1 second's worth of data
* Utilizing Disruptor in raw sample buffer
* Sample rate and sample size now mutable
* Sample rates above 1kHz allowed
* Reasonable results sampling into the 50kHz range
* Default sample rate increased to 200 Hz, default sample size to 2048

## Release 1.4, April 27 2014

* Bugfix on socket close
* Added ability to tune buffer size to more closely represent 1 seconds' worth of data

## Design Goals

ESP is designed to provide Java programs access to EEG device signals and provide the ability to process the signals for the purposes of the program. The ESP library itself is not an implementation, however there are several implementations for specific EEG devices:
	
* [ESP-Nia](http://mrstampy.github.io/ESP-Nia/)
* [ESP-ThinkGear](http://mrstampy.github.io/ESP-ThinkGear/)
* [ESP-OpenBCI (experimental)](http://mrstampy.github.io/ESP-OpenBCI/)
* [ESPLab (JavaFX 8.0 GUI for the ESP Lab interface)](http://mrstampy.github.io/ESPLab/)
	
ESP provides a common structure for device specific implementations.  This allows programs using the ESP libraries to cater for multiple devices from a single codebase.

## Primary Design Specifics

### [The MultiConnectSocket Interface](https://github.com/mrstampy/ESP/blob/master/ESP/src/com/github/mrstampy/esp/multiconnectionsocket/MultiConnectionSocket.java)

This interface defines the methods that must be implemented for a class	which provides access to EEG hardware. An [abstract superclass](https://github.com/mrstampy/ESP/blob/master/ESP/src/com/github/mrstampy/esp/multiconnectionsocket/AbstractMultiConnectionSocket.java) exists for ease of implementation.

### [The RawEspConnection Interface](https://github.com/mrstampy/ESP/blob/master/ESP/src/com/github/mrstampy/esp/dsp/lab/RawEspConnection.java)

RawEspConnection extends the MultiConnectSocket interface. Implementations are intended to encapsulate and control a MultiConnectSocket instance through the implementation of the common methods, as well as provide the current seconds' worth of samples on demand for direct use or further processing.  Signal processing methods exist to provide ease of processing the signal.  An [abstract superclass](https://github.com/mrstampy/ESP/blob/master/ESP/src/com/github/mrstampy/esp/dsp/lab/AbstractRawEspConnection.java) exists for ease of implementation.

### [The Lab Interface](https://github.com/mrstampy/ESP/blob/master/ESP/src/com/github/mrstampy/esp/dsp/lab/Lab.java)

While the ESP library provides DSP processing classes, how the signal is processed is left to programs using the library.  Lab implementations provide the ability to obtain the current seconds' worth of samples from the encapsulated RawEspConnection, process the signal and notify the program when a sample is ready for use.

A [default implementation](https://github.com/mrstampy/ESP/blob/master/ESP/src/com/github/mrstampy/esp/dsp/lab/DefaultLab.java)
exists and an [abstract superclass](https://github.com/mrstampy/ESP/blob/master/ESP/src/com/github/mrstampy/esp/dsp/lab/AbstractLab.java) provides ease
of custom lab implementation. 

## Usage

### Direct MultiConnectSocket Usage

This is the lowest level use case.  Programs using ESP library implementations in this manner will receive device-specific signals as they occur.  Each implementation of the ESP library's MultiConnectSocket has a device-specific listener mechanism to obtain the signal.  Pseudo code for usage appears as so:

		MultiConnectNiaSocket socket = new MultiConnectNiaSocket();
		
		// optional, common to all MultiConnectSocket implementations:
		// add a listener to receive connect/disconnect events
		socket.addConnectionEventListener(new ConnectionEventListener() {
			public void connectionEventPerformed(ConnectionEvent e) {
				doSomethingWith(e);
			}
		});
		
		socket.addListener(new NiaEventListener() {
			public void niaEventPerformed(NiaEvent event) {
				doSomethingWith(e);
			}
		});
		
		socket.start();

### Direct MultiConnectSocket Usage - Remote Connection

This functionality is from where the MultiConnectSocket derives its name.  Device specific implementations provide the ability to open a socket on a configurable port.  Such sockets facilitate a subscribe and publish of device signals. This allows separate processes and machines to [receive the device signals](https://github.com/mrstampy/ESP/blob/master/ESP/src/com/github/mrstampy/esp/multiconnectionsocket/AbstractSocketConnector.java) for their own purposes - signal recording, secondary processing, display etc.
			
		// on the host machine or process:
		// port is set via the system property 'socket.broadcaster.port', default '12345'
		MultiConnectNiaSocket socket = new MultiConnectNiaSocket(true);
		
		// on the remote process/machine:
		// port is set via the system property 'socket.broadcaster.port', default '12345'
		NiaSocketConnector connector = new NiaSocketConnector("host machine name or IP address");
		
		connector.addListener(new NiaEventListener() {
			public void niaEventPerformed(NiaEvent event) {
				doSomethingWith(e);
			}
		});
		
		connector.connect();
		connector.subscribe(....);
		
		// and back on the host machine or process:
		socket.start();

### RawEspConnection Usage

RawEspConnection implementations aggregate the current second's worth of data, insulating the program from device specific listeners.  The samples are intended to be queried periodically in a separate scheduled task or thread.  The period of querying is left to the program and is independent of sample rate.
	
		NiaConnection connection = new NiaConnection();
		
		// optional, common to all RawEspConnection implementations:
		// add a listener to receive connect/disconnect events
		connection.addConnectionEventListener(new ConnectionEventListener() {
			public void connectionEventPerformed(ConnectionEvent e) {
				doSomethingWith(e);
			}
		});
		
		connection.start();
		
		// in a separate scheduled periodic task..
		int channel = 1; // channel of interest 
		dealWithCurrentSecondOfSamples(connection.getCurrentFor(channel));

### Lab Usage

Lab implementations provide the ability to process the current seconds' worth of data from the RawEspConnection and notify any interested parties of its completion. Triggering of signal processing is intended to be executed periodically in a separate scheduled task or thread.  The period of triggering is left to the program and is independent of sample rate.

		NiaConnection connection = new NiaConnection();
		
		// optional, common to all RawEspConnection implementations:
		// add a listener to receive connect/disconnect events
		connection.addConnectionEventListener(new ConnectionEventListener() {
			public void connectionEventPerformed(ConnectionEvent e) {
				doSomethingWith(e);
			}
		});
		
		Lab lab = connection.getDefaultLab();
		
		int numBands = 40; // first 40 bands
		lab.setNumBands(numBands); // must be set
		
		int channel = 1; // channel of interest for multichannels
		lab.setChannel(channel); // required if > 1 channel
		
		lab.addSignalProcessedListener(new SignalProcessedListener() {
			public void signalProcessed(double[] processed) {
				doSomethingWith(processed);
			}
		});
		
		// other lab values set as appropriate for processing the signal
		
		connection.start();
		
		// in a separate scheduled periodic task..
		lab.triggerProcessing();


## Maven Dependency
       <dependency>
           <groupId>com.github.mrstampy</groupId>
           <artifactId>esp-nia</artifactId>
           <version>2.1</version>
       </dependency>

Usage of the library is straight-forward. One only needs to instantiate the MultiConnectNiaSocket, add a listener, start the socket and deal with the events as they occur.

## Multiple Connections

As implied by the name of the class, the MultiConnectNiaSocket is capable of not only processing Nia messages for a single application but also of broadcasting the messages to separate applications, even on separate machines and devices!

## Raw Signal Processing (as of version 1.3)

In the development of the [ESP-Nia](http://mrstampy.github.com/ESP-Nia/) implementation the base classes for raw signal processing were developed.  Multi connection socket implementations contain a raw data buffer which contains the current second's worth of data (for the OCZ Nia this translates to 3906 data points, for the ThinkGear devices this translates to 512 data points).

Additional classes have been added to assist with raw signal processing. The examples shown will work with the NeuroSky raw output however these classes can be used for any DSP work.

Additional functionality is described in the JavaDocs. This work is released under the GPL 3.0 license. No warranty of any kind is offered.

[ESP-Nia](http://mrstampy.github.io/ESP-Nia/) Copyright (C) 2014 Burton Alexander. 

OCZ Nia is a trademark of [OCZ](http://www.ocz.com)
